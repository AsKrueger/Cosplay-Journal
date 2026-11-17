-- Amistades tipo Facebook, bloqueos y denuncias.
-- Las solicitudes solo se gestionan con funciones: así se validan bloqueos y duplicados.

create table public.friendships (
    requester_id uuid not null references public.profiles (id) on delete cascade,
    addressee_id uuid not null references public.profiles (id) on delete cascade,
    status       text not null default 'PENDING' check (status in ('PENDING', 'ACCEPTED')),
    created_at   timestamptz not null default now(),
    responded_at timestamptz,
    primary key (requester_id, addressee_id),
    check (requester_id <> addressee_id)
);
-- Una sola relación por pareja, la pida quien la pida.
create unique index friendships_pair_idx on public.friendships
    (least(requester_id, addressee_id), greatest(requester_id, addressee_id));
create index friendships_addressee_idx on public.friendships (addressee_id);

create table public.blocks (
    blocker_id uuid not null references public.profiles (id) on delete cascade,
    blocked_id uuid not null references public.profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (blocker_id, blocked_id),
    check (blocker_id <> blocked_id)
);

create table public.reports (
    id          bigint generated always as identity primary key,
    reporter_id uuid not null references public.profiles (id) on delete cascade,
    reported_id uuid not null references public.profiles (id) on delete cascade,
    reason      text not null check (char_length(reason) between 1 and 1000),
    created_at  timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- Funciones de consulta usadas por las políticas
-- ---------------------------------------------------------------------------
create function public.are_friends(a uuid, b uuid) returns boolean
    language sql stable security definer set search_path = public as
$$
    select exists (
        select 1 from public.friendships f
        where f.status = 'ACCEPTED'
          and ((f.requester_id = a and f.addressee_id = b) or (f.requester_id = b and f.addressee_id = a))
    );
$$;

create function public.is_blocked_between(a uuid, b uuid) returns boolean
    language sql stable security definer set search_path = public as
$$
    select exists (
        select 1 from public.blocks
        where (blocker_id = a and blocked_id = b) or (blocker_id = b and blocked_id = a)
    );
$$;

-- Hay alguna relación (amistad o solicitud) entre los dos.
create function public.has_relation(a uuid, b uuid) returns boolean
    language sql stable security definer set search_path = public as
$$
    select exists (
        select 1 from public.friendships f
        where (f.requester_id = a and f.addressee_id = b) or (f.requester_id = b and f.addressee_id = a)
    );
$$;

-- ---------------------------------------------------------------------------
-- Operaciones
-- ---------------------------------------------------------------------------
create function public.require_user() returns uuid
    language plpgsql stable as
$$
declare uid uuid := auth.uid();
begin
    if uid is null then
        raise exception 'not_authenticated' using errcode = '28000';
    end if;
    return uid;
end;
$$;

-- Envía una solicitud. Si el otro ya te la había enviado, queda aceptada.
-- Devuelve el estado resultante: PENDING o ACCEPTED.
create function public.send_friend_request(p_target uuid) returns text
    language plpgsql security definer set search_path = public as
$$
declare
    me uuid := public.require_user();
    existing public.friendships;
begin
    if p_target = me then
        raise exception 'cannot_friend_self';
    end if;
    if not exists (select 1 from public.profiles where id = p_target)
       or public.is_blocked_between(me, p_target) then
        raise exception 'user_not_found';
    end if;

    select * into existing from public.friendships
    where (requester_id = me and addressee_id = p_target) or (requester_id = p_target and addressee_id = me);

    if not found then
        insert into public.friendships (requester_id, addressee_id) values (me, p_target);
        return 'PENDING';
    end if;

    if existing.status = 'PENDING' and existing.addressee_id = me then
        update public.friendships set status = 'ACCEPTED', responded_at = now()
        where requester_id = existing.requester_id and addressee_id = existing.addressee_id;
        return 'ACCEPTED';
    end if;
    return existing.status;
end;
$$;

create function public.send_friend_request_by_code(p_code text) returns text
    language plpgsql security definer set search_path = public as
$$
declare target uuid;
begin
    select id into target from public.profiles where friend_code = upper(trim(p_code));
    if target is null then
        raise exception 'user_not_found';
    end if;
    return public.send_friend_request(target);
end;
$$;

-- Acepta o rechaza una solicitud recibida.
create function public.respond_friend_request(p_requester uuid, p_accept boolean) returns void
    language plpgsql security definer set search_path = public as
$$
declare me uuid := public.require_user();
begin
    if p_accept then
        update public.friendships set status = 'ACCEPTED', responded_at = now()
        where requester_id = p_requester and addressee_id = me and status = 'PENDING';
    else
        delete from public.friendships
        where requester_id = p_requester and addressee_id = me and status = 'PENDING';
    end if;
    if not found then
        raise exception 'request_not_found';
    end if;
end;
$$;

-- Cancela una solicitud enviada o elimina una amistad.
create function public.remove_friend(p_other uuid) returns void
    language plpgsql security definer set search_path = public as
$$
declare me uuid := public.require_user();
begin
    delete from public.friendships
    where (requester_id = me and addressee_id = p_other) or (requester_id = p_other and addressee_id = me);
end;
$$;

-- Bloquear elimina la amistad y cualquier solicitud.
create function public.block_user(p_target uuid) returns void
    language plpgsql security definer set search_path = public as
$$
declare me uuid := public.require_user();
begin
    if p_target = me then
        raise exception 'cannot_block_self';
    end if;
    insert into public.blocks (blocker_id, blocked_id) values (me, p_target) on conflict do nothing;
    perform public.remove_friend(p_target);
end;
$$;

-- Busca por nickname (mínimo 3 letras) o por código de amigo exacto.
-- Solo devuelve el nickname y el avatar a quien ya es amigo.
create function public.search_profiles(p_query text)
    returns table (id uuid, nickname text, avatar_path text, relation text)
    language plpgsql stable security definer set search_path = public as
$$
declare
    me uuid := public.require_user();
    q text := trim(p_query);
begin
    if char_length(q) < 3 then
        return;
    end if;
    return query
    select p.id,
           p.nickname,
           case when public.are_friends(me, p.id) then p.avatar_path end,
           coalesce((select case
                                when f.status = 'ACCEPTED' then 'FRIEND'
                                when f.requester_id = me then 'SENT'
                                else 'RECEIVED' end
                     from public.friendships f
                     where (f.requester_id = me and f.addressee_id = p.id)
                        or (f.requester_id = p.id and f.addressee_id = me)), 'NONE')
    from public.profiles p
    where p.id <> me
      and not public.is_blocked_between(me, p.id)
      and (lower(p.nickname) like lower(replace(replace(q, '%', ''), '_', '\_')) || '%'
           or p.friend_code = upper(q))
    order by lower(p.nickname)
    limit 20;
end;
$$;

-- ---------------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------------
alter table public.friendships enable row level security;
alter table public.blocks      enable row level security;
alter table public.reports     enable row level security;

-- Solo lectura directa; las escrituras pasan por las funciones.
create policy friendships_select_own on public.friendships
    for select to authenticated using (auth.uid() in (requester_id, addressee_id));

create policy blocks_select_own on public.blocks
    for select to authenticated using (blocker_id = auth.uid());
create policy blocks_delete_own on public.blocks
    for delete to authenticated using (blocker_id = auth.uid());

create policy reports_insert_own on public.reports
    for insert to authenticated with check (reporter_id = auth.uid());

-- Los perfiles de amigos y de quien tiene una solicitud contigo son visibles.
create policy profiles_select_related on public.profiles
    for select to authenticated
    using (public.has_relation(auth.uid(), id) and not public.is_blocked_between(auth.uid(), id));

-- Los amigos ven a qué eventos vas.
create policy attendance_select_friends on public.attendance
    for select to authenticated using (public.are_friends(auth.uid(), user_id));
