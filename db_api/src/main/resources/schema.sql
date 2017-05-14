DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS threads CASCADE;
DROP TABLE IF EXISTS forums CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS forum_users CASCADE;
DROP TABLE IF EXISTS votes CASCADE;

DROP INDEX IF EXISTS forums_user_id_idx;
DROP INDEX IF EXISTS threads_user_id_idx;
DROP INDEX IF EXISTS threads_forum_id_idx;
DROP INDEX IF EXISTS posts_user_id_idx;
DROP INDEX IF EXISTS posts_forum_id_idx;
DROP INDEX IF EXISTS posts_thread_id_idx;
DROP INDEX IF EXISTS forum_users_user_id_idx;
DROP INDEX IF EXISTS forum_users_forum_id_idx;

CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  about    TEXT DEFAULT NULL,
  email    CITEXT UNIQUE,
  fullname TEXT DEFAULT NULL,
  nickname CITEXT COLLATE ucs_basic UNIQUE
);

CREATE TABLE IF NOT EXISTS forums (
  id      SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users (id) ON DELETE CASCADE NOT NULL,
  posts   INTEGER DEFAULT 0,
  threads INTEGER DEFAULT 0,
  slug    CITEXT UNIQUE                                   NOT NULL,
  title   TEXT                                            NOT NULL
);

CREATE INDEX IF NOT EXISTS forums_user_id_idx
  ON forums (user_id);

CREATE TABLE IF NOT EXISTS threads (
  user_id  INTEGER REFERENCES users (id) ON DELETE CASCADE  NOT NULL,
  created  TIMESTAMPTZ DEFAULT NOW(),
  forum_id INTEGER REFERENCES forums (id) ON DELETE CASCADE NOT NULL,
  id       SERIAL PRIMARY KEY,
  message  TEXT        DEFAULT NULL,
  slug     CITEXT UNIQUE,
  title    TEXT                                             NOT NULL,
  votes    INTEGER     DEFAULT 0
);

CREATE INDEX IF NOT EXISTS threads_user_id_idx
  ON threads (user_id);
CREATE INDEX IF NOT EXISTS threads_forum_id_idx
  ON threads (forum_id);

CREATE TABLE IF NOT EXISTS posts (
  user_id   INTEGER REFERENCES users (id) ON DELETE CASCADE   NOT NULL,
  created   TIMESTAMPTZ DEFAULT NOW(),
  forum_id  INTEGER REFERENCES forums (id) ON DELETE CASCADE  NOT NULL,
  id        SERIAL PRIMARY KEY,
  is_edited BOOLEAN     DEFAULT FALSE,
  message   TEXT        DEFAULT NULL,
  parent    INTEGER     DEFAULT 0,
  thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE NOT NULL,
  path      INTEGER []                                        NOT NULL
);

CREATE INDEX IF NOT EXISTS posts_user_id_idx
  ON posts (user_id);
CREATE INDEX IF NOT EXISTS posts_forum_id_idx
  ON posts (forum_id);
CREATE INDEX IF NOT EXISTS posts_thread_id_idx
  ON posts (thread_id);

CREATE TABLE IF NOT EXISTS forum_users (
  user_id  INTEGER REFERENCES users (id) ON DELETE CASCADE,
  forum_id INTEGER REFERENCES forums (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS forum_users_user_id_idx
  ON forum_users (user_id);
CREATE INDEX IF NOT EXISTS forum_users_forum_id_idx
  ON forum_users (forum_id);

CREATE OR REPLACE FUNCTION on_insert_post_or_thread()
  RETURNS TRIGGER AS '
BEGIN
  IF NOT EXISTS(SELECT *
                FROM forum_users
                WHERE forum_id = NEW.forum_id AND user_id = NEW.user_id)
  THEN
    INSERT INTO forum_users (user_id, forum_id) VALUES (NEW.user_id, NEW.forum_id);
  END IF;
  RETURN NEW;
END;
' LANGUAGE plpgsql;


CREATE TRIGGER post_insert_trigger
AFTER INSERT ON posts
FOR EACH ROW EXECUTE PROCEDURE on_insert_post_or_thread();

CREATE TRIGGER thread_insert_trigger
AFTER INSERT ON threads
FOR EACH ROW EXECUTE PROCEDURE on_insert_post_or_thread();

CREATE TABLE votes (
  user_id   INTEGER REFERENCES users (id) ON DELETE CASCADE,
  thread_id INTEGER REFERENCES threads (id) ON DELETE CASCADE,
  voice     INTEGER DEFAULT 0
);

CREATE OR REPLACE FUNCTION update_or_insert_votes(u_id INTEGER, t_id INTEGER, v INTEGER)
  RETURNS VOID AS '
DECLARE
  count INTEGER;
BEGIN
  SELECT COUNT(*)
  FROM votes
  WHERE user_id = u_id AND thread_id = t_id
  INTO count;
  IF count > 0
  THEN
    UPDATE votes
    SET voice = v
    WHERE user_id = u_id AND thread_id = t_id;
  ELSE
    INSERT INTO votes (user_id, thread_id, voice) VALUES (u_id, t_id, v);
  END IF;
  UPDATE threads
  SET votes = (SELECT SUM(voice)
               FROM votes
               WHERE thread_id = t_id)
  WHERE id = t_id;
END;
' LANGUAGE plpgsql