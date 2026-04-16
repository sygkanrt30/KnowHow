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

CREATE TABLE IF NOT EXISTS app_user
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(150) NOT NULL,
    email      VARCHAR(50)  NOT NULL UNIQUE,
    role       usr_role  DEFAULT 'USER',
    balance_id BIGINT       NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT username_check
        CHECK (LENGTH(username) >= 3 AND is_not_empty_string(username)),
    CONSTRAINT email_check
        CHECK (LENGTH(email) >= 5 AND is_not_empty_string(email)),
    CONSTRAINT balance_fk
        FOREIGN KEY (balance_id)
            REFERENCES balance (id)
            ON DELETE CASCADE

);

CREATE TABLE IF NOT EXISTS course
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(100)  NOT NULL UNIQUE,
    description      TEXT          NOT NULL,
    status           status        NOT NULL,
    course_text      TEXT          NOT NULL,
    price            BIGINT        NOT NULL,
    tags             VARCHAR(50)[] NOT NULL DEFAULT '{}',
    moderation_score INT                    DEFAULT 0,
    rating           NUMERIC(2, 1) NOT NULL DEFAULT 0.0,
    user_id          BIGINT        NOT NULL REFERENCES app_user (id),
    created_at       TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT title_check CHECK (is_not_empty_string(title))
);

CREATE TABLE IF NOT EXISTS rating
(
    id         BIGSERIAL PRIMARY KEY,
    grade      SMALLINT NOT NULL,
    course_id  BIGINT REFERENCES course (id),
    user_id    BIGINT   NOT NULL REFERENCES app_user (id),
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

    UPDATE course
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


CREATE OR REPLACE FUNCTION update_status_of_course()
    RETURNS TRIGGER AS
$$
DECLARE
    current_moderation_score INT;
BEGIN
    SELECT moderation_score
    INTO current_moderation_score
    FROM course
    WHERE id = NEW.id;

    IF current_moderation_score = 3 THEN
        UPDATE course
        SET status = 'PASSED_MODERATION'
        WHERE id = NEW.id;
    END IF;

    RETURN NEW;
END;
$$ language plpgsql;

CREATE TRIGGER update_status_of_course
    AFTER UPDATE
        OF moderation_score
    ON course
    FOR EACH ROW
EXECUTE FUNCTION update_status_of_course();


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

CREATE INDEX IF NOT EXISTS idx_app_user_username ON app_user (username);
CREATE INDEX IF NOT EXISTS idx_app_user_balance_id ON app_user (balance_id);


CREATE INDEX IF NOT EXISTS idx_balance_coins ON balance (coins);
CREATE INDEX IF NOT EXISTS idx_balance_coins_updated ON balance (coins, c_updated_at);


CREATE INDEX IF NOT EXISTS idx_balance_history_balance_id ON balance_history (balance_id);
CREATE INDEX IF NOT EXISTS idx_balance_history_change_amount ON balance_history (change_amount);
CREATE INDEX IF NOT EXISTS idx_balance_history_old_new_coins ON balance_history (old_coins, new_coins);


CREATE INDEX IF NOT EXISTS idx_course_title ON course (title);
CREATE INDEX IF NOT EXISTS idx_course_status ON course (status);
CREATE INDEX IF NOT EXISTS idx_course_user_id ON course (user_id);
CREATE INDEX IF NOT EXISTS idx_course_rating ON course (rating DESC);
CREATE INDEX IF NOT EXISTS idx_course_moderation_score ON course (moderation_score);
CREATE INDEX IF NOT EXISTS idx_course_search_gin
    ON course USING GIN (to_tsvector('russian', title || ' ' || COALESCE(description, '')));
CREATE INDEX IF NOT EXISTS idx_course_tags_gin ON course USING GIN (tags);
CREATE INDEX IF NOT EXISTS idx_course_status_rating ON course (status, rating DESC);
CREATE INDEX IF NOT EXISTS idx_course_user_status ON course (user_id, status);


CREATE INDEX IF NOT EXISTS idx_purchased_course_course_id ON purchased_course (course_id);
CREATE INDEX IF NOT EXISTS idx_purchased_course_user_id ON purchased_course (user_id);
CREATE INDEX IF NOT EXISTS idx_purchased_course_lookup ON purchased_course (user_id, course_id);


CREATE INDEX IF NOT EXISTS idx_deactivated_token_keep_until ON deactivated_token (keep_until);
CREATE INDEX IF NOT EXISTS idx_deactivated_token_id ON deactivated_token (id);
CREATE INDEX IF NOT EXISTS idx_deactivated_token_valid ON deactivated_token (id, keep_until);


CREATE INDEX IF NOT EXISTS idx_course_active ON course (id, rating)
    WHERE status IN ('PASSED_MODERATION', 'ON_MODERATION');
CREATE INDEX IF NOT EXISTS idx_app_user_admin_moderator ON app_user (id, username)
    WHERE role IN ('MODERATOR');
CREATE INDEX IF NOT EXISTS idx_course_title_prefix ON course (title varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_app_user_email_prefix ON app_user (email varchar_pattern_ops);