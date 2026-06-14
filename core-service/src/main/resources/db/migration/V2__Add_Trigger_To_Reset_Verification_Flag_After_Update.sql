CREATE OR REPLACE FUNCTION reset_email_verified_after_update_email_value()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.email = OLD.email THEN
        RAISE NOTICE 'No changes, skipping trigger';
        RETURN NULL;
    END IF;

    IF OLD.email_verified = TRUE THEN
        UPDATE user_contacts SET email_verified = false WHERE id = NEW.id;
    END IF;

    RETURN NEW;
END;
$$ language plpgsql;

CREATE TRIGGER trigger_update_email_verified
    AFTER UPDATE OF email
    ON user_contacts
    FOR EACH ROW
EXECUTE FUNCTION reset_email_verified_after_update_email_value();