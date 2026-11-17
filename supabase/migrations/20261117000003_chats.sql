-- Chats privados entre amigos y grupos para grupales.

create table public.conversations (
    id         uuid primary key default gen_random_uuid(),
    kind       text not null check (kind in ('DIRECT', 'GROUP')),
    title      text check (title is null or char_length(title) between 1 and 60),
    event_id   bigint references public.events (id) on delete set null,
    direct_key text unique, -- "menor_uuid:mayor_uuid" para no duplicar chats privados
    created_by uuid references public.profiles (id) on delete set null,
    created_at timestamptz not null default now(),
    check ((kind = 'DIRECT') = (direct_key is not null)),
    check (kind = 'DIRECT' or title is not null)
);

create table public.conversation_members (
    conversation_id uuid not null references public.conversations (id) on delete cascade,
    user_id         uuid not null references public.profiles (id) on delete cascade,
    role            text not null default 'MEMBER' check (role in ('OWNER', 'MEMBER')),
    joined_at       timestamptz not null default now(),
    last_read_at    timestamptz not null default now(),
    primary key (conversation_id, user_id)
);
create index conversation_members_user_idx on public.conversation_members (user_id);

create table public.messages (
    id              bigint generated always as identity primary key,
    conversation_id uuid not null references public.conversations (id) on delete cascade,
    sender_id       uuid references public.profiles (id) on delete set null,
    body            text check (body is null or char_length(body) between 1 and 2000),
    image_path      text,
    created_at      timestamptz not null default now(),
    check (body is not null or image_path is not null)
);
create index messages_conversation_idx on public.messages (conversation_id, created_at desc);

-- ---------------------------------------------------------------------------
-- Reglas
-- ---------------------------------------------------------------------------
create function public.is_member(p_conversation uuid, p_user uuid default auth.uid()) returns boolean
    language sql stable security definer set search_path = public as
$$
    select exists (select 1 from public.conversation_members
                   where conversation_id = p_conversation and user_id = p_user);
$$;

-- Se puede escribir si eres miembro y, en un chat privado, si seguís siendo amigos.
create function public.can_post(p_conversation uuid, p_user uuid default auth.uid()) returns boolean
    language sql stable security definer set search_path = public as
$$
    select public.is_member(p_conversation, p_user)
       and (
           (select kind from public.conversations where id = p_conversation) = 'GROUP'
           or exists (select 1 from public.conversation_members m
                      where m.conversation_id = p_conversation and m.user_id <> p_user
                        and public.are_friends(p_user, m.user_id))
       );
$$;

-- Devuelve el chat privado con un amigo, creándolo si no existe.
create function public.get_or_create_direct(p_friend uuid) returns uuid
    language plpgsql security definer set search_path = public as
$$
declare
    me uuid := public.require_user();
    k text;
    conv uuid;
begin
    if not public.are_friends(me, p_friend) then
        raise exception 'not_friends';
    end if;
    k := least(me, p_friend)::text || ':' || greatest(me, p_friend)::text;
    select id into conv from public.conversations where direct_key = k;
    if conv is null then
        insert into public.conversations (kind, direct_key, created_by)
        values ('DIRECT', k, me) returning id into conv;
        insert into public.conversation_members (conversation_id, user_id, role)
        values (conv, me, 'OWNER'), (conv, p_friend, 'MEMBER');
    end if;
    return conv;
end;
$$;

-- Crea un grupo con amigos del creador.
create function public.create_group(p_title text, p_members uuid[], p_event bigint default null) returns uuid
    language plpgsql security definer set search_path = public as
$$
declare
    me uuid := public.require_user();
    member uuid;
    conv uuid;
begin
    insert into public.conversations (kind, title, event_id, created_by)
    values ('GROUP', trim(p_title), p_event, me) returning id into conv;
    insert into public.conversation_members (conversation_id, user_id, role) values (conv, me, 'OWNER');
    foreach member in array coalesce(p_members, '{}') loop
        if member <> me then
            if not public.are_friends(me, member) then
                raise exception 'not_friends';
            end if;
            insert into public.conversation_members (conversation_id, user_id)
            values (conv, member) on conflict do nothing;
        end if;
    end loop;
    return conv;
end;
$$;

-- Un miembro del grupo puede añadir a un amigo suyo.
create function public.add_group_member(p_conversation uuid, p_user uuid) returns void
    language plpgsql security definer set search_path = public as
$$
declare me uuid := public.require_user();
begin
    if (select kind from public.conversations where id = p_conversation) is distinct from 'GROUP'
       or not public.is_member(p_conversation, me) then
        raise exception 'conversation_not_found';
    end if;
    if not public.are_friends(me, p_user) then
        raise exception 'not_friends';
    end if;
    insert into public.conversation_members (conversation_id, user_id)
    values (p_conversation, p_user) on conflict do nothing;
end;
$$;

create function public.leave_conversation(p_conversation uuid) returns void
    language plpgsql security definer set search_path = public as
$$
declare me uuid := public.require_user();
begin
    delete from public.conversation_members where conversation_id = p_conversation and user_id = me;
    -- El último en salir borra el grupo
    delete from public.conversations c
    where c.id = p_conversation
      and not exists (select 1 from public.conversation_members m where m.conversation_id = c.id);
end;
$$;

create function public.mark_read(p_conversation uuid) returns void
    language sql security definer set search_path = public as
$$
    update public.conversation_members set last_read_at = now()
    where conversation_id = p_conversation and user_id = auth.uid();
$$;

-- Lista de chats del usuario con el último mensaje y los no leídos.
create function public.my_conversations()
    returns table (
        id uuid, kind text, title text, event_id bigint,
        other_user_id uuid, other_nickname text, other_avatar_path text,
        last_body text, last_has_image boolean, last_at timestamptz, unread bigint
    )
    language sql stable security definer set search_path = public as
$$
    select c.id, c.kind, c.title, c.event_id,
           o.user_id, p.nickname, p.avatar_path,
           lm.body, lm.image_path is not null, coalesce(lm.created_at, c.created_at),
           (select count(*) from public.messages x
            where x.conversation_id = c.id and x.created_at > me.last_read_at
              and x.sender_id is distinct from me.user_id)
    from public.conversation_members me
    join public.conversations c on c.id = me.conversation_id
    left join lateral (select m.user_id from public.conversation_members m
                       where c.kind = 'DIRECT' and m.conversation_id = c.id and m.user_id <> me.user_id
                       limit 1) o on true
    left join public.profiles p on p.id = o.user_id
    left join lateral (select body, image_path, created_at from public.messages
                       where conversation_id = c.id order by created_at desc, id desc limit 1) lm on true
    where me.user_id = auth.uid()
    order by coalesce(lm.created_at, c.created_at) desc;
$$;

-- ---------------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------------
alter table public.conversations        enable row level security;
alter table public.conversation_members enable row level security;
alter table public.messages             enable row level security;

create policy conversations_select_member on public.conversations
    for select to authenticated using (public.is_member(id));
create policy conversations_update_owner on public.conversations
    for update to authenticated
    using (kind = 'GROUP' and exists (select 1 from public.conversation_members m
                                      where m.conversation_id = conversations.id and m.user_id = auth.uid() and m.role = 'OWNER'));
revoke update on public.conversations from anon, authenticated;
grant update (title, event_id) on public.conversations to authenticated;

create policy members_select_member on public.conversation_members
    for select to authenticated using (public.is_member(conversation_id));

create policy messages_select_member on public.messages
    for select to authenticated using (public.is_member(conversation_id));
create policy messages_insert_member on public.messages
    for insert to authenticated
    with check (sender_id = auth.uid() and public.can_post(conversation_id));
create policy messages_delete_own on public.messages
    for delete to authenticated using (sender_id = auth.uid());

-- Los miembros de un chat pueden ver el nickname y avatar de los demás miembros.
create policy profiles_select_chat_mates on public.profiles
    for select to authenticated
    using (exists (select 1 from public.conversation_members a
                   join public.conversation_members b on a.conversation_id = b.conversation_id
                   where a.user_id = auth.uid() and b.user_id = profiles.id));

-- Mensajes en tiempo real
alter publication supabase_realtime add table public.messages;
