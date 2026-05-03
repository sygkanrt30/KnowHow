ALTER TABLE course
    ALTER COLUMN user_id DROP NOT NULL,
    DROP CONSTRAINT IF EXISTS course_user_id_fkey,
    ADD CONSTRAINT course_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES app_user (id)
            ON DELETE SET NULL;

ALTER TABLE course
    ADD COLUMN not_for_sale BOOLEAN DEFAULT FALSE;

ALTER TABLE rating
    ALTER COLUMN user_id DROP NOT NULL;

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