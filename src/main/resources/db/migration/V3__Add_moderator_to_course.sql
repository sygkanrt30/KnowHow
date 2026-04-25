ALTER TABLE course
    ADD COLUMN moderator_id BIGINT REFERENCES app_user (id);

CREATE TABLE moderator_load
(
    id                    BIGSERIAL PRIMARY KEY,
    moderator_id          BIGINT  NOT NULL UNIQUE REFERENCES app_user (id),
    courses_in_moderation INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE moderation_review
(
    id               BIGSERIAL PRIMARY KEY,
    moderator_id     BIGINT    NOT NULL REFERENCES app_user (id),
    course_id        BIGINT    NOT NULL REFERENCES course (id),
    approved         BOOLEAN   NOT NULL,
    rejection_reason TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_moderation_review_course_id ON moderation_review (course_id);
CREATE INDEX idx_moderation_review_moderator_id ON moderation_review (moderator_id);
