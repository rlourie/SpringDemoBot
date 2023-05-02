-- bot.numbers definition

-- Drop table

-- DROP TABLE bot.numbers;

CREATE TABLE bot.numbers (
	id bigserial NOT NULL,
	"number" varchar(255) NULL,
	CONSTRAINT numbers_pkey PRIMARY KEY (id)
);


-- bot.users definition

-- Drop table

-- DROP TABLE bot.users;

CREATE TABLE bot.users (
	id int8 NOT NULL,
	auth bool NULL,
	command varchar(255) NULL,
	"name" varchar(255) NULL,
	"number" varchar(255) NULL,
	state varchar(255) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);


-- bot."document" definition

-- Drop table

-- DROP TABLE bot."document";

CREATE TABLE bot."document" (
	id varchar(255) NOT NULL,
	"name" varchar(255) NULL,
	"size" varchar(255) NULL,
	unique_id varchar(255) NULL,
	user_id int8 NULL,
	CONSTRAINT document_pkey PRIMARY KEY (id),
	CONSTRAINT fkm19xjdnh3l6aueyrpm1705t52 FOREIGN KEY (user_id) REFERENCES bot.users(id)
);