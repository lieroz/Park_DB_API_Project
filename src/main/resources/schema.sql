DROP TABLE IF EXISTS uservotes;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS threads;
DROP TABLE IF EXISTS forums;
DROP TABLE IF EXISTS users;

CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users (
  about TEXT,
  email CITEXT UNIQUE,
  fullname TEXT,
  nickname CITEXT COLLATE ucs_basic PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS forums (
  posts BIGINT DEFAULT NULL,
  slug CITEXT PRIMARY KEY NOT NULL,
  threads BIGINT DEFAULT NULL,
  title TEXT NOT NULL,
  "user" CITEXT COLLATE ucs_basic NOT NULL REFERENCES users (nickname)
);

CREATE TABLE IF NOT EXISTS threads (
  author CITEXT COLLATE ucs_basic NOT NULL REFERENCES users (nickname),
  created TIMESTAMP,
  forum CITEXT NOT NULL REFERENCES forums (slug),
  id SERIAL PRIMARY KEY,
  message TEXT,
  slug CITEXT UNIQUE,
  title TEXT NOT NULL,
  votes BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS uservotes (
  nickname CITEXT COLLATE ucs_basic NOT NULL REFERENCES users (nickname),
  voice INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS posts (
  author CITEXT COLLATE ucs_basic NOT NULL REFERENCES users (nickname),
  created TIMESTAMP,
  forum CITEXT NOT NULL REFERENCES forums (slug),
  id SERIAL PRIMARY KEY,
  isEdited BOOLEAN DEFAULT FALSE,
  message TEXT,
  parent INTEGER,
  thread INTEGER REFERENCES threads (id)
);