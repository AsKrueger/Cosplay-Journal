-- Un cosplay pasa a ser solo nombre y foto: se retiran descripción, personaje, serie y estado.
ALTER TABLE cosplay DROP COLUMN description;
ALTER TABLE cosplay DROP COLUMN character_name;
ALTER TABLE cosplay DROP COLUMN origin_series;
ALTER TABLE cosplay DROP COLUMN status;
ALTER TABLE cosplay ADD COLUMN photo_url VARCHAR(500);
