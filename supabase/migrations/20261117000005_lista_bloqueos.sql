-- Lista de usuarios que has bloqueado, con su nickname para poder desbloquearlos.
-- Las políticas ocultan el perfil de un bloqueado, así que se expone solo a quien bloqueó.
create function public.my_blocks()
    returns table (blocked_id uuid, nickname text, created_at timestamptz)
    language sql stable security definer set search_path = public as
$$
    select b.blocked_id, p.nickname, b.created_at
    from public.blocks b
    join public.profiles p on p.id = b.blocked_id
    where b.blocker_id = auth.uid()
    order by b.created_at desc;
$$;

revoke execute on function public.my_blocks() from public, anon;
grant execute on function public.my_blocks() to authenticated;
