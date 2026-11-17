-- Escaparate de fotos del perfil, almacenamiento de imágenes y borrado de cuenta.

create table public.showcase_photos (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid not null references public.profiles (id) on delete cascade,
    image_path text not null,
    caption    text check (caption is null or char_length(caption) <= 140),
    event_id   bigint references public.events (id) on delete set null,
    position   int not null default 0,
    created_at timestamptz not null default now()
);
create index showcase_photos_user_idx on public.showcase_photos (user_id, position);

-- Máximo 12 fotos por usuario para no agotar el almacenamiento gratuito.
create function public.limit_showcase() returns trigger
    language plpgsql as
$$
begin
    if (select count(*) from public.showcase_photos where user_id = new.user_id) >= 12 then
        raise exception 'showcase_full';
    end if;
    return new;
end;
$$;
create trigger showcase_limit before insert on public.showcase_photos
    for each row execute function public.limit_showcase();

alter table public.showcase_photos enable row level security;

create policy showcase_select on public.showcase_photos
    for select to authenticated
    using (user_id = auth.uid() or public.are_friends(auth.uid(), user_id));
create policy showcase_write_own on public.showcase_photos
    for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

-- ---------------------------------------------------------------------------
-- Storage: buckets privados. Rutas: avatars/{user}/x.webp, showcase/{user}/x.webp,
-- chat/{conversation}/x.webp
-- ---------------------------------------------------------------------------
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('avatars',  'avatars',  false, 512000, array['image/webp', 'image/jpeg']),
       ('showcase', 'showcase', false, 512000, array['image/webp', 'image/jpeg']),
       ('chat',     'chat',     false, 512000, array['image/webp', 'image/jpeg'])
on conflict (id) do nothing;

-- Primera carpeta de la ruta convertida a uuid (null si no es un uuid).
create function public.path_owner(p_name text) returns uuid
    language plpgsql immutable as
$$
begin
    return split_part(p_name, '/', 1)::uuid;
exception when invalid_text_representation then
    return null;
end;
$$;

create policy storage_profile_read on storage.objects
    for select to authenticated
    using (bucket_id in ('avatars', 'showcase')
           and (public.path_owner(name) = auth.uid()
                or public.are_friends(auth.uid(), public.path_owner(name))
                or (bucket_id = 'avatars' and public.has_relation(auth.uid(), public.path_owner(name)))));

create policy storage_profile_write on storage.objects
    for insert to authenticated
    with check (bucket_id in ('avatars', 'showcase') and public.path_owner(name) = auth.uid());

create policy storage_profile_delete on storage.objects
    for delete to authenticated
    using (bucket_id in ('avatars', 'showcase') and public.path_owner(name) = auth.uid());

create policy storage_chat_read on storage.objects
    for select to authenticated
    using (bucket_id = 'chat' and public.is_member(public.path_owner(name)));

create policy storage_chat_write on storage.objects
    for insert to authenticated
    with check (bucket_id = 'chat' and public.can_post(public.path_owner(name)));

-- ---------------------------------------------------------------------------
-- Borrar la cuenta (obligatorio en Google Play). La app borra antes sus archivos.
-- ---------------------------------------------------------------------------
create function public.delete_my_account() returns void
    language plpgsql security definer set search_path = public as
$$
declare me uuid := public.require_user();
begin
    delete from auth.users where id = me;
end;
$$;

-- Las funciones no se pueden llamar sin sesión, salvo comprobar un nickname.
revoke execute on all functions in schema public from public, anon;
grant execute on all functions in schema public to authenticated;
grant execute on function public.nickname_available(text) to anon;
