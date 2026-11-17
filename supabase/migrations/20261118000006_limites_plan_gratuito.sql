-- Límites para no salir nunca del plan gratuito de Supabase (500 MB de base, 1 GB de archivos,
-- 5 GB de tráfico al mes) y mantenimiento que ejecuta el cron diario de GitHub.

-- ---------------------------------------------------------------------------
-- 1. updated_at de un evento solo cambia si cambian sus datos, y lo pone la base.
--    El importador reescribe todos los salones cada día; así la app puede pedir
--    únicamente los posteriores a su última sincronización (updated_at > marca).
--    clock_timestamp() da una hora distinta a cada fila y siempre creciente entre
--    lotes, de modo que ningún cambio queda por detrás de la marca de la app.
-- ---------------------------------------------------------------------------
create function public.events_keep_updated_at() returns trigger
    language plpgsql as
$$
begin
    if tg_op = 'UPDATE' and (to_jsonb(new) - 'updated_at') = (to_jsonb(old) - 'updated_at') then
        new.updated_at := old.updated_at;
    else
        new.updated_at := clock_timestamp();
    end if;
    return new;
end;
$$;
create trigger events_keep_updated_at before insert or update on public.events
    for each row execute function public.events_keep_updated_at();
create index events_updated_at_idx on public.events (updated_at);

-- ---------------------------------------------------------------------------
-- 2. Límites de ritmo contra spam (error 'rate_limited', que la app traduce).
-- ---------------------------------------------------------------------------
create function public.limit_messages() returns trigger
    language plpgsql security definer set search_path = public as
$$
begin
    if (select count(*) from public.messages
        where sender_id = new.sender_id and created_at > now() - interval '1 minute') >= 20
       or (select count(*) from public.messages
           where sender_id = new.sender_id and created_at > now() - interval '1 day') >= 1500 then
        raise exception 'rate_limited';
    end if;
    return new;
end;
$$;
create index messages_sender_idx on public.messages (sender_id, created_at);
create trigger messages_rate before insert on public.messages
    for each row execute function public.limit_messages();

create function public.limit_friend_requests() returns trigger
    language plpgsql security definer set search_path = public as
$$
begin
    if (select count(*) from public.friendships
        where requester_id = new.requester_id and created_at > now() - interval '1 day') >= 30 then
        raise exception 'rate_limited';
    end if;
    return new;
end;
$$;
create index friendships_requester_created_idx on public.friendships (requester_id, created_at);
create trigger friendships_rate before insert on public.friendships
    for each row execute function public.limit_friend_requests();

-- Comentarios: 5 al día por usuario; sin sesión, 30 al día entre todos.
create function public.limit_feedback() returns trigger
    language plpgsql security definer set search_path = public as
$$
begin
    if (new.user_id is not null and
        (select count(*) from public.feedback
         where user_id = new.user_id and created_at > now() - interval '1 day') >= 5)
       or (new.user_id is null and
           (select count(*) from public.feedback
            where user_id is null and created_at > now() - interval '1 day') >= 30) then
        raise exception 'rate_limited';
    end if;
    return new;
end;
$$;
create index feedback_user_created_idx on public.feedback (user_id, created_at);
create trigger feedback_rate before insert on public.feedback
    for each row execute function public.limit_feedback();

create function public.limit_reports() returns trigger
    language plpgsql security definer set search_path = public as
$$
begin
    if (select count(*) from public.reports
        where reporter_id = new.reporter_id and created_at > now() - interval '1 day') >= 20 then
        raise exception 'rate_limited';
    end if;
    return new;
end;
$$;
create trigger reports_rate before insert on public.reports
    for each row execute function public.limit_reports();

create function public.limit_groups() returns trigger
    language plpgsql security definer set search_path = public as
$$
begin
    if new.kind = 'GROUP' and
       (select count(*) from public.conversations
        where created_by = new.created_by and kind = 'GROUP' and created_at > now() - interval '1 day') >= 10 then
        raise exception 'rate_limited';
    end if;
    return new;
end;
$$;
create trigger conversations_rate before insert on public.conversations
    for each row execute function public.limit_groups();

-- ---------------------------------------------------------------------------
-- 3. Archivos que sobran. Solo la service key (cron diario) puede llamarla;
--    el borrado real se hace con la API de Storage, que también quita el fichero.
--    - fotos del chat con más de p_chat_days días (los mensajes siguen ahí)
--    - archivos que ya no usa nadie (subida a medias, cuenta borrada, avatar cambiado)
-- ---------------------------------------------------------------------------
create function public.storage_purge_candidates(p_chat_days int default 90)
    returns table (bucket text, path text)
    language sql stable security definer set search_path = public, storage as
$$
    select o.bucket_id, o.name
    from storage.objects o
    where o.created_at < now() - interval '1 day'
      and (
        (o.bucket_id = 'chat' and (o.created_at < now() - make_interval(days => p_chat_days)
                                   or not exists (select 1 from public.messages m where m.image_path = o.name)))
        or (o.bucket_id = 'showcase' and not exists (select 1 from public.showcase_photos s where s.image_path = o.name))
        or (o.bucket_id = 'avatars' and not exists (select 1 from public.profiles p where p.avatar_path = o.name))
      )
    order by o.created_at
    limit 1000;
$$;
revoke execute on function public.storage_purge_candidates(int) from public, anon, authenticated;
grant execute on function public.storage_purge_candidates(int) to service_role;

-- Nuevas funciones de disparador: nadie las llama directamente.
revoke execute on function public.events_keep_updated_at(), public.limit_messages(), public.limit_friend_requests(),
    public.limit_feedback(), public.limit_reports(), public.limit_groups() from public, anon, authenticated;
