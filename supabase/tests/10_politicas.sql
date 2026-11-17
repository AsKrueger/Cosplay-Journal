-- Tests de las reglas de privacidad. Cualquier fallo aborta con ON_ERROR_STOP.
\set QUIET on

create schema tests;
grant usage on schema tests to anon, authenticated;

create function tests.ok(cond boolean, msg text) returns void language plpgsql as
$$ begin
    if cond is not true then raise exception 'FALLO: %', msg; end if;
    raise notice 'ok - %', msg;
end $$;

create function tests.throws(stmt text, msg text) returns void language plpgsql as
$$ begin
    begin
        execute stmt;
    exception when others then
        raise notice 'ok - % (%)', msg, sqlerrm;
        return;
    end;
    raise exception 'FALLO: % (no dio error)', msg;
end $$;
grant execute on all functions in schema tests to anon, authenticated;

-- Usuarios: ana, bea, carla y dani
insert into auth.users (id, raw_user_meta_data) values
    ('00000000-0000-0000-0000-00000000000a', '{"nickname":"ana"}'),
    ('00000000-0000-0000-0000-00000000000b', '{"nickname":"bea"}'),
    ('00000000-0000-0000-0000-00000000000c', '{"nickname":"carla"}'),
    ('00000000-0000-0000-0000-00000000000d', '{}');
insert into public.events (slug, name, start_date, end_date, city)
values ('salon-1', 'Salón 1', '2026-12-01', '2026-12-02', 'Madrid');

select tests.ok((select count(*) = 4 from public.profiles), 'se crea un perfil por usuario');
select tests.ok((select nickname like 'user_%' from public.profiles
                 where id = '00000000-0000-0000-0000-00000000000d'), 'nickname por defecto sin metadatos');
select tests.throws($$insert into auth.users (raw_user_meta_data) values ('{"nickname":"ANA"}')$$,
                    'nickname duplicado sin distinguir mayúsculas');

-- ---------------------------------------------------------------- anónimo
set role anon;
select tests.ok((select count(*) = 1 from public.events), 'anónimo ve los eventos');
select tests.ok((select count(*) = 0 from public.profiles), 'anónimo no ve perfiles');
select tests.ok(public.nickname_available('nuevo'), 'anónimo comprueba nickname libre');
select tests.ok(not public.nickname_available('Bea'), 'nickname ocupado');
select tests.throws($$select public.send_friend_request('00000000-0000-0000-0000-00000000000b')$$,
                    'anónimo no puede enviar solicitudes');
insert into public.feedback (message) values ('Me encanta');
select tests.throws($$insert into public.feedback (message, processed) values ('x', true)$$,
                    'no se puede marcar un comentario como procesado');
reset role;

-- ---------------------------------------------------------------- ana
set role authenticated;
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000a', false);

select tests.ok((select count(*) = 1 from public.profiles), 'sin amigos solo ves tu perfil');
select tests.ok((select count(*) = 0 from public.search_profiles('be')), 'la búsqueda exige 3 letras');
select tests.ok((select relation = 'NONE' and avatar_path is null from public.search_profiles('bea')),
                'la búsqueda no enseña el avatar de un desconocido');
select tests.throws($$select public.get_or_create_direct('00000000-0000-0000-0000-00000000000b')$$,
                    'no se puede abrir chat con un desconocido');
select tests.throws($$insert into public.friendships values
                      ('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b', 'ACCEPTED')$$,
                    'no se puede insertar una amistad aceptada directamente');
select tests.throws($$update public.profiles set friend_code = 'HACK' where id = auth.uid()$$,
                    'no se puede cambiar el código de amigo');
update public.profiles set nickname = 'ana_cos' where id = auth.uid();

select tests.ok(public.send_friend_request('00000000-0000-0000-0000-00000000000b') = 'PENDING', 'ana pide amistad a bea');
select tests.ok(public.send_friend_request('00000000-0000-0000-0000-00000000000c') = 'PENDING', 'ana pide amistad a carla');
insert into public.attendance (user_id, event_id, status) values (auth.uid(), 1, 'GOING');
insert into public.favorites (user_id, event_id) values (auth.uid(), 1);
select tests.throws($$insert into public.attendance (user_id, event_id, status)
                      values ('00000000-0000-0000-0000-00000000000b', 1, 'GOING')$$,
                    'no se escribe la asistencia de otro');

-- ---------------------------------------------------------------- bea acepta
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000b', false);
select tests.ok((select count(*) = 0 from public.attendance), 'con solicitud pendiente no ves su asistencia');
select tests.ok((select count(*) = 2 from public.profiles), 'ves el perfil de quien te envía solicitud');
select public.respond_friend_request('00000000-0000-0000-0000-00000000000a', true);
select tests.ok(public.are_friends('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b'),
                'ana y bea son amigas');
select tests.ok((select count(*) = 1 from public.attendance), 'una amiga ve tu asistencia');
select tests.ok((select count(*) = 0 from public.favorites), 'los favoritos son privados');

-- Chat privado
select set_config('tests.direct', public.get_or_create_direct('00000000-0000-0000-0000-00000000000a')::text, false);
select tests.ok(public.get_or_create_direct('00000000-0000-0000-0000-00000000000a')::text = current_setting('tests.direct'),
                'el chat privado no se duplica');
insert into public.messages (conversation_id, sender_id, body)
values (current_setting('tests.direct')::uuid, auth.uid(), 'Hola!');
select tests.throws($$insert into public.messages (conversation_id, sender_id, body)
                      values (current_setting('tests.direct')::uuid, '00000000-0000-0000-0000-00000000000a', 'suplantación')$$,
                    'no se envía un mensaje en nombre de otro');

-- ---------------------------------------------------------------- carla (pendiente)
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000c', false);
select tests.ok((select count(*) = 0 from public.messages), 'quien no es miembro no lee el chat');
select tests.ok((select count(*) = 0 from public.conversations), 'quien no es miembro no ve la conversación');
select tests.throws($$insert into public.messages (conversation_id, sender_id, body)
                      values (current_setting('tests.direct')::uuid, auth.uid(), 'intrusa')$$,
                    'quien no es miembro no escribe');
select tests.ok(public.send_friend_request('00000000-0000-0000-0000-00000000000a') = 'ACCEPTED',
                'pedir amistad a quien ya te la pidió la acepta');

-- ---------------------------------------------------------------- ana: grupo y unread
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000a', false);
select tests.ok((select unread = 1 from public.my_conversations() where kind = 'DIRECT'), 'un mensaje sin leer');
select public.mark_read(current_setting('tests.direct')::uuid);
select tests.ok((select unread = 0 from public.my_conversations() where kind = 'DIRECT'), 'leído tras mark_read');
select tests.throws($$select public.create_group('Grupal', array['00000000-0000-0000-0000-00000000000d']::uuid[])$$,
                    'no se añade a un desconocido a un grupo');
select set_config('tests.group', public.create_group('Grupal JoJo',
    array['00000000-0000-0000-0000-00000000000b', '00000000-0000-0000-0000-00000000000c']::uuid[], 1)::text, false);
insert into public.messages (conversation_id, sender_id, body)
values (current_setting('tests.group')::uuid, auth.uid(), 'Ensayo el sábado');

-- Escaparate y storage
insert into public.showcase_photos (user_id, image_path) values (auth.uid(), auth.uid() || '/1.webp');
insert into storage.objects (bucket_id, name) values ('showcase', auth.uid() || '/1.webp');
select tests.throws($$insert into storage.objects (bucket_id, name)
                      values ('showcase', '00000000-0000-0000-0000-00000000000b/x.webp')$$,
                    'no se sube a la carpeta de otro');
insert into storage.objects (bucket_id, name) values ('chat', current_setting('tests.group') || '/f.webp');

-- ---------------------------------------------------------------- dani (desconocido)
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000d', false);
select tests.ok((select count(*) = 0 from public.showcase_photos), 'un desconocido no ve el escaparate');
select tests.ok((select count(*) = 0 from storage.objects), 'un desconocido no ve los archivos');
select tests.ok((select count(*) = 0 from public.attendance), 'un desconocido no ve la asistencia');

-- ---------------------------------------------------------------- bea bloquea a ana
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000b', false);
select tests.ok((select count(*) = 1 from public.showcase_photos), 'una amiga ve el escaparate');
select tests.ok((select count(*) = 2 from storage.objects), 'una amiga ve las fotos del escaparate y del grupo');
select public.block_user('00000000-0000-0000-0000-00000000000a');
select tests.ok(not public.are_friends('00000000-0000-0000-0000-00000000000a', '00000000-0000-0000-0000-00000000000b'),
                'bloquear elimina la amistad');
select tests.throws($$insert into public.messages (conversation_id, sender_id, body)
                      values (current_setting('tests.direct')::uuid, auth.uid(), 'sigues ahí?')$$,
                    'tras el bloqueo no se escribe por privado');
select tests.ok((select count(*) = 0 from public.showcase_photos), 'tras el bloqueo no se ve el escaparate');
insert into public.reports (reporter_id, reported_id, reason)
values (auth.uid(), '00000000-0000-0000-0000-00000000000a', 'Spam');

select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000a', false);
select tests.throws($$select public.send_friend_request('00000000-0000-0000-0000-00000000000b')$$,
                    'la bloqueada no puede volver a pedir amistad');
select tests.ok((select count(*) = 0 from public.search_profiles('bea')), 'la bloqueada no encuentra a quien la bloqueó');
select tests.ok((select count(*) = 0 from public.reports), 'las denuncias no se leen desde la app');

-- ---------------------------------------------------------------- borrar cuenta
select set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-00000000000c', false);
select public.delete_my_account();
reset role;
select tests.ok((select count(*) = 0 from public.profiles where nickname = 'carla'), 'borrar la cuenta elimina el perfil');
select tests.ok((select count(*) = 2 from public.conversation_members
                 where conversation_id = current_setting('tests.group')::uuid), 'y sale de sus grupos');

\echo 'Todos los tests de políticas han pasado'
