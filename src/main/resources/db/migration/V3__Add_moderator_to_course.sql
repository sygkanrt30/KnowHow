ALTER TABLE course ADD COLUMN IF NOT EXISTS moderator_id BIGINT REFERENCES app_user(id);

CREATE TABLE IF NOT EXISTS moderator_load (
                    id BIGSERIAL PRIMARY KEY,
                    moderator_id BIGINT NOT NULL UNIQUE REFERENCES app_user(id),
    courses_in_moderation INTEGER NOT NULL DEFAULT 0
    );

INSERT INTO moderator_load (moderator_id, courses_in_moderation)
SELECT id, 0 FROM app_user WHERE role = 'MODERATOR'
    ON CONFLICT (moderator_id) DO NOTHING;