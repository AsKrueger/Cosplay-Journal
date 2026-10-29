-- Galería personal: fotos del usuario etiquetadas como evento, cosplay o grupal.
CREATE TABLE gallery_photo (
    id VARCHAR(100) PRIMARY KEY,
    owner_id VARCHAR(100) NOT NULL,
    tag VARCHAR(20) NOT NULL,
    event_id VARCHAR(100),
    url VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_gallery_photo_owner FOREIGN KEY (owner_id) REFERENCES user_account(id)
);

CREATE INDEX idx_gallery_photo_owner_tag ON gallery_photo(owner_id, tag);
