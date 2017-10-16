CREATE TRIGGER batch_update_modified_time BEFORE UPDATE ON batch FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
