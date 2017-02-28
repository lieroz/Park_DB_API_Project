CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS Users (
  about TEXT,
  email CITEXT UNIQUE,
  fullname VARCHAR(100),
  nickname CITEXT PRIMARY KEY
);

-- CREATE TABLE IF NOT EXISTS Forums (
--   posts BIGINT DEFAULT 0,
--   slug VARCHAR(100) PRIMARY KEY NOT NULL,
--   threads BIGINT DEFAULT 0,
--   title VARCHAR(100) UNIQUE NOT NULL,
--   "user" VARCHAR(100) NOT NULL REFERENCES Users (nickname)
-- );
--
-- CREATE TABLE IF NOT EXISTS Threads (
--   author VARCHAR(100) NOT NULL REFERENCES Users(nickname),
--   created VARCHAR(100) DEFAULT 0,
--   forum VARCHAR(100) NOT NULL REFERENCES Forums(slug),
--   id SERIAL,
--   message VARCHAR(100),
--   slug VARCHAR(100) PRIMARY KEY,
--   title VARCHAR(100) UNIQUE NOT NULL,
--   votes BIGINT DEFAULT 0
-- );
--
-- CREATE TABLE IF NOT EXISTS Posts (
--   author VARCHAR(100) NOT NULL REFERENCES Users(nickname),
--   created VARCHAR(100) DEFAULT 0,
--   forum VARCHAR(100) NOT NULL REFERENCES Forums(slug),
--   id SERIAL,
--   isEdited BOOLEAN DEFAULT TRUE,
--   message VARCHAR(100),
--   parent BIGINT DEFAULT 0,
--   thread VARCHAR(100) REFERENCES Threads(slug)
-- );