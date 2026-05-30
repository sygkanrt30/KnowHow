CREATE TYPE usr_role AS ENUM ('USER', 'MODERATOR', 'ADMIN');
CREATE TYPE status AS ENUM ('NOT_ACCEPTED', 'ON_MODERATION', 'PASSED_MODERATION');

CREATE OR REPLACE FUNCTION is_not_empty_string(string VARCHAR)
    RETURNS BOOLEAN AS
$$
BEGIN
    RETURN string <> '';
END ;
$$ LANGUAGE plpgsql;


CREATE TABLE IF NOT EXISTS balance
(
    id           BIGSERIAL PRIMARY KEY,
    coins        BIGINT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    c_updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS balance_history
(
    id            BIGSERIAL PRIMARY KEY,
    balance_id    BIGINT      NOT NULL,
    change_amount VARCHAR(20) NOT NULL,
    old_coins     BIGINT      NOT NULL,
    new_coins     BIGINT      NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_balance_history_balance
        FOREIGN KEY (balance_id)
            REFERENCES balance (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_contacts
(
    id                   BIGSERIAL PRIMARY KEY,
    email                VARCHAR(150) NOT NULL UNIQUE,
    email_verified       BOOLEAN      NOT NULL DEFAULT FALSE,
    tg_username          VARCHAR(50) UNIQUE,
    tg_username_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    primary_contact      VARCHAR(25)  NOT NULL DEFAULT 'EMAIL',
    created_at           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_check
        CHECK (LENGTH(email) >= 5 AND is_not_empty_string(email)),
    CONSTRAINT tg_username_check
        CHECK ( LENGTH(tg_username) >= 5 AND is_not_empty_string(tg_username))
);

CREATE TABLE IF NOT EXISTS app_user
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(150) NOT NULL,
    role       usr_role     NOT NULL DEFAULT 'USER',
    level      INTEGER      NOT NULL DEFAULT 1,
    balance_id BIGINT UNIQUE,
    contact_id BIGINT       NOT NULL UNIQUE,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT username_check
        CHECK (LENGTH(username) >= 3 AND is_not_empty_string(username)),
    CONSTRAINT balance_fk
        FOREIGN KEY (balance_id)
            REFERENCES balance (id)
            ON DELETE CASCADE,
    CONSTRAINT contact_fk
        FOREIGN KEY (contact_id)
            REFERENCES user_contacts (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS business_details
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100)  NOT NULL UNIQUE,
    description TEXT          NOT NULL,
    course_text TEXT          NOT NULL,
    price       BIGINT        NOT NULL,
    tags        VARCHAR(50)[] NOT NULL DEFAULT '{}',
    rating      NUMERIC(2, 1)          DEFAULT 0.0,
    created_at  TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT title_check CHECK (is_not_empty_string(title))
);

CREATE TABLE IF NOT EXISTS course
(
    id                  BIGSERIAL PRIMARY KEY,
    status              status  NOT NULL,
    not_for_sale        BOOLEAN NOT NULL DEFAULT FALSE,
    business_details_id BIGINT  NOT NULL REFERENCES business_details (id) ON DELETE CASCADE,
    user_id             BIGINT  REFERENCES app_user (id) ON DELETE SET NULL,
    moderator_id        BIGINT REFERENCES app_user (id),
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

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

CREATE TABLE IF NOT EXISTS rating
(
    id         BIGSERIAL PRIMARY KEY,
    grade      INT NOT NULL,
    course_id  BIGINT REFERENCES course (id),
    user_id    BIGINT REFERENCES app_user (id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT grade_check CHECK (grade < 6 AND grade > 0)
);

CREATE TABLE IF NOT EXISTS purchased_course
(
    course_id  BIGINT REFERENCES course (id),
    user_id    BIGINT REFERENCES app_user (id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT purchased_course_pk
        PRIMARY KEY (course_id, user_id)
);

CREATE TABLE deactivated_token
(
    id         UUID PRIMARY KEY,
    keep_until TIMESTAMP NOT NULL CHECK (keep_until > NOW())
);


CREATE OR REPLACE FUNCTION recalc_course_rating()
    RETURNS TRIGGER AS
$$
DECLARE
    avg_rating NUMERIC(2, 1);
BEGIN
    SELECT COALESCE(ROUND(AVG(grade), 1), 0.0)
    INTO avg_rating
    FROM rating
    WHERE course_id = COALESCE(NEW.course_id, OLD.course_id);

    UPDATE business_details
    SET rating = avg_rating
    WHERE id = COALESCE(NEW.course_id, OLD.course_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_recalc_course_rating
    AFTER INSERT OR UPDATE OR DELETE
    ON rating
    FOR EACH ROW
EXECUTE FUNCTION recalc_course_rating();


CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.c_updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language plpgsql;

CREATE TRIGGER update_balance_updated_at_column
    BEFORE UPDATE
    ON balance
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE OR REPLACE FUNCTION log_balance_change()
    RETURNS TRIGGER AS
$$
DECLARE
    change_amount VARCHAR(20);
    difference    BIGINT;
    old_coins_val BIGINT;
BEGIN
    IF TG_OP = 'INSERT' THEN
        old_coins_val := 0;
        difference := NEW.coins - old_coins_val;
    ELSE
        old_coins_val := OLD.coins;
        difference := NEW.coins - old_coins_val;
    END IF;

    IF difference > 0 THEN
        change_amount := '+' || difference::VARCHAR;
    ELSIF difference < 0 THEN
        change_amount := difference::VARCHAR;
    ELSE
        change_amount := '0';
    END IF;

    IF difference != 0 THEN
        INSERT INTO balance_history (balance_id, change_amount, old_coins, new_coins, created_at)
        VALUES (NEW.id, change_amount, old_coins_val, NEW.coins, CURRENT_TIMESTAMP);
    END IF;

    IF TG_OP = 'UPDATE' THEN
        NEW.c_updated_at := CURRENT_TIMESTAMP;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_log_balance_change
    AFTER INSERT OR UPDATE OF coins
    ON balance
    FOR EACH ROW
EXECUTE FUNCTION log_balance_change();


CREATE OR REPLACE FUNCTION recalculate_teacher_level()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE app_user
    SET level = (SELECT COALESCE(ROUND(AVG(b.rating)), 1)
                 FROM course c
                          JOIN business_details b ON c.business_details_id = b.id
                 WHERE c.user_id = app_user.id
                   AND b.rating > 0)
    WHERE id = (SELECT user_id
                FROM course
                WHERE id = NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_recalculate_teacher_level
    AFTER UPDATE OF rating
    ON business_details
    FOR EACH ROW
EXECUTE FUNCTION recalculate_teacher_level();

CREATE OR REPLACE FUNCTION auto_add_moderator_to_load()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.role = 'MODERATOR' THEN
        INSERT INTO moderator_load (moderator_id, courses_in_moderation)
        VALUES (NEW.id, 0)
        ON CONFLICT (moderator_id) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_auto_add_moderator
    AFTER INSERT
    ON app_user
    FOR EACH ROW
EXECUTE FUNCTION auto_add_moderator_to_load();


CREATE OR REPLACE FUNCTION change_flag_not_for_sale_if_author_is_null()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE course
    SET not_for_sale = TRUE
    WHERE OLD.not_for_sale = FALSE
      AND NEW.user_id IS NULL;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_change_flag_not_for_sale
    AFTER UPDATE OF user_id
    ON course
    FOR EACH ROW
EXECUTE FUNCTION change_flag_not_for_sale_if_author_is_null();

CREATE INDEX IF NOT EXISTS idx_app_user_username ON app_user (username);
CREATE INDEX IF NOT EXISTS idx_app_user_balance_id ON app_user (balance_id);


CREATE INDEX IF NOT EXISTS idx_balance_coins ON balance (coins);
CREATE INDEX IF NOT EXISTS idx_balance_coins_updated ON balance (coins, c_updated_at);


CREATE INDEX IF NOT EXISTS idx_balance_history_balance_id ON balance_history (balance_id);
CREATE INDEX IF NOT EXISTS idx_balance_history_change_amount ON balance_history (change_amount);
CREATE INDEX IF NOT EXISTS idx_balance_history_old_new_coins ON balance_history (old_coins, new_coins);


CREATE INDEX IF NOT EXISTS idx_course_title ON business_details (title);
CREATE INDEX IF NOT EXISTS idx_course_status ON course (status);
CREATE INDEX IF NOT EXISTS idx_course_user_id ON course (user_id);
CREATE INDEX IF NOT EXISTS idx_course_search_gin
    ON business_details USING GIN (to_tsvector('russian', title || ' ' || COALESCE(description, '')));
CREATE INDEX IF NOT EXISTS idx_course_tags_gin ON business_details USING GIN (tags);
CREATE INDEX IF NOT EXISTS idx_course_status_rating ON business_details (rating);
CREATE INDEX IF NOT EXISTS idx_course_user_status ON course (user_id, status);


CREATE INDEX IF NOT EXISTS idx_purchased_course_course_id ON purchased_course (course_id);
CREATE INDEX IF NOT EXISTS idx_purchased_course_user_id ON purchased_course (user_id);
CREATE INDEX IF NOT EXISTS idx_purchased_course_lookup ON purchased_course (user_id, course_id);


CREATE INDEX IF NOT EXISTS idx_deactivated_token_keep_until ON deactivated_token (keep_until);
CREATE INDEX IF NOT EXISTS idx_deactivated_token_id ON deactivated_token (id);
CREATE INDEX IF NOT EXISTS idx_deactivated_token_valid ON deactivated_token (id, keep_until);


CREATE INDEX IF NOT EXISTS idx_app_user_admin_moderator ON app_user (id, username)
    WHERE role IN ('MODERATOR');
CREATE INDEX IF NOT EXISTS idx_course_title_prefix ON business_details (title varchar_pattern_ops);

CREATE INDEX idx_moderation_review_course_id ON moderation_review (course_id);
CREATE INDEX idx_moderation_review_moderator_id ON moderation_review (moderator_id);