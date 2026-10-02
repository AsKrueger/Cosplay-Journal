-- Resource Ownership Schema Expansion

-- 1. Add owner_id to cosplay table
ALTER TABLE cosplay ADD COLUMN owner_id VARCHAR(100);

-- Foreign key constraint for cosplay owner
ALTER TABLE cosplay ADD CONSTRAINT fk_cosplay_owner
    FOREIGN KEY (owner_id) REFERENCES user_account(id) ON DELETE CASCADE;

CREATE INDEX idx_cosplay_owner ON cosplay(owner_id);

-- 2. Add creator_id to participation table
ALTER TABLE participation ADD COLUMN creator_id VARCHAR(100);

-- Foreign key constraint for participation creator
ALTER TABLE participation ADD CONSTRAINT fk_participation_creator
    FOREIGN KEY (creator_id) REFERENCES user_account(id) ON DELETE CASCADE;

CREATE INDEX idx_participation_creator ON participation(creator_id);

-- 3. Foreign key constraint for photo uploader
ALTER TABLE photo ADD CONSTRAINT fk_photo_uploader
    FOREIGN KEY (uploaded_by_user_id) REFERENCES user_account(id) ON DELETE SET NULL;

CREATE INDEX idx_photo_uploader ON photo(uploaded_by_user_id);
