-- Esquema base: perfiles, eventos, asistencia, favoritos, comentarios y anuncios.

-- ---------------------------------------------------------------------------
-- Perfiles
-- ---------------------------------------------------------------------------
create table public.profiles (
    id          uuid primary key references auth.users (id) on delete cascade,
    nickname    text not null
                check (char_length(nickname) between 3 and 24 and nickname ~ '^[A-Za-z0-9_.]+$'),
    avatar_path text,
    friend_code text not null unique
                default upper(substr(md5(gen_random_uuid()::text), 1, 8)),
    created_at  timestamptz not null default now()
);
create unique index profiles_nickname_lower_idx on public.profiles (lower(nickname));

-- Crea el perfil al registrarse. El nickname llega en los metadatos del registro.
create function public.handle_new_user() returns trigger
    language plpgsql security definer set search_path = public as
$$
begin
    insert into public.profiles (id, nickname)
    values (new.id,
            coalesce(nullif(new.raw_user_meta_data ->> 'nickname', ''),
                     'user_' || substr(replace(new.id::text, '-', ''), 1, 8)));
    return new;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- ¿Está libre un nickname? Se consulta antes de registrarse.
create function public.nickname_available(p_nickname text) returns boolean
    language sql stable security definer set search_path = public as
$$
    select not exists (select 1 from public.profiles where lower(nickname) = lower(p_nickname));
$$;

-- ---------------------------------------------------------------------------
-- Eventos (los escribe el importador con la service key, que salta RLS)
-- ---------------------------------------------------------------------------
create table public.events (
    id            bigint generated always as identity primary key,
    slug          text not null unique,
    name          text not null,
    start_date    date not null,
    end_date      date not null,
    venue         text,
    address       text,
    city          text,
    province_code char(2),
    price         text,
    image_url     text,
    website       text,
    source        text not null default 'LISTADOMANGA',
    updated_at    timestamptz not null default now(),
    check (end_date >= start_date)
);
create index events_start_date_idx on public.events (start_date);

-- ---------------------------------------------------------------------------
-- Asistencia y favoritos
-- ---------------------------------------------------------------------------
create table public.attendance (
    user_id    uuid not null references public.profiles (id) on delete cascade,
    event_id   bigint not null references public.events (id) on delete cascade,
    status     text not null check (status in ('GOING', 'NOT_GOING', 'WENT', 'MISSED')),
    updated_at timestamptz not null default now(),
    primary key (user_id, event_id)
);

create table public.favorites (
    user_id    uuid not null references public.profiles (id) on delete cascade,
    event_id   bigint not null references public.events (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (user_id, event_id)
);

-- ---------------------------------------------------------------------------
-- Comentarios para el autor y anuncios
-- ---------------------------------------------------------------------------
create table public.feedback (
    id          bigint generated always as identity primary key,
    user_id     uuid references public.profiles (id) on delete set null,
    message     text not null check (char_length(message) between 1 and 4000),
    app_version text,
    locale      text,
    processed   boolean not null default false,
    created_at  timestamptz not null default now()
);

create table public.ads (
    id         bigint generated always as identity primary key,
    title      text not null,
    image_url  text not null,
    target_url text not null,
    starts_at  timestamptz not null default now(),
    ends_at    timestamptz,
    active     boolean not null default true
);

-- ---------------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------------
alter table public.profiles   enable row level security;
alter table public.events     enable row level security;
alter table public.attendance enable row level security;
alter table public.favorites  enable row level security;
alter table public.feedback   enable row level security;
alter table public.ads        enable row level security;

-- El perfil propio se lee y se edita (solo nickname y avatar; id y código no cambian).
create policy profiles_select_self on public.profiles
    for select to authenticated using (id = auth.uid());
create policy profiles_update_self on public.profiles
    for update to authenticated using (id = auth.uid()) with check (id = auth.uid());
revoke update on public.profiles from anon, authenticated;
grant update (nickname, avatar_path) on public.profiles to authenticated;

create policy events_select_all on public.events
    for select to anon, authenticated using (true);

create policy attendance_own on public.attendance
    for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy favorites_own on public.favorites
    for all to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

-- Cualquiera puede enviar comentarios; nadie los lee desde la app.
create policy feedback_insert on public.feedback
    for insert to anon, authenticated
    with check (user_id is null or user_id = auth.uid());
revoke insert, update, delete on public.feedback from anon, authenticated;
grant insert (user_id, message, app_version, locale) on public.feedback to anon, authenticated;

create policy ads_select_active on public.ads
    for select to anon, authenticated
    using (active and starts_at <= now() and (ends_at is null or ends_at > now()));
