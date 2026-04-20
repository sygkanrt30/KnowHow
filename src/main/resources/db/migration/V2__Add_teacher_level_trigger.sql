ALTER TABLE app_user ADD COLUMN IF NOT EXISTS level INTEGER DEFAULT 1;

CREATE OR REPLACE FUNCTION recalculate_teacher_level()
RETURNS TRIGGER AS $$
BEGIN
UPDATE app_user
SET level = (
    SELECT COALESCE(ROUND(AVG(c.rating)), 1)
    FROM course c
    WHERE c.user_id = app_user.id
)
WHERE id = (
    SELECT user_id FROM course WHERE id = NEW.id
);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_recalculate_teacher_level ON course;
CREATE TRIGGER trigger_recalculate_teacher_level
    AFTER UPDATE OF rating ON course
    FOR EACH ROW
    EXECUTE FUNCTION recalculate_teacher_level();