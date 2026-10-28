-- Foto de perfil pública del usuario (URL). El almacenamiento del fichero se decidirá por separado.
ALTER TABLE user_account ADD COLUMN avatar_url VARCHAR(500);
