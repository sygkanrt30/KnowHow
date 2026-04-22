CREATE OR REPLACE FUNCTION auto_add_moderator_to_load()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.role = 'MODERATOR' THEN
        INSERT INTO moderator_load (moderator_id, courses_in_moderation)
        VALUES (NEW.id, 0)
        ON CONFLICT (moderator_id) DO NOTHING;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_auto_add_moderator ON app_user;
CREATE TRIGGER trigger_auto_add_moderator
    AFTER INSERT ON app_user
    FOR EACH ROW
    EXECUTE FUNCTION auto_add_moderator_to_load();