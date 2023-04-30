CREATE TABLE bot.users (
	id int8 NOT NULL,
	"admin" bool NULL,
	"name" varchar(255) NULL,
	"number" varchar(255) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE bot."document" (
	id varchar(255) NOT NULL,
	"name" varchar(255) NULL,
	"size" varchar(255) NULL,
	unique_id varchar(255) NULL,
	user_id int8 NULL,
	CONSTRAINT document_pkey PRIMARY KEY (id),
	CONSTRAINT fkm19xjdnh3l6aueyrpm1705t52 FOREIGN KEY (user_id) REFERENCES bot.users(id)
);

CREATE TABLE bot.users_status (
	id bigserial NOT NULL,
	command varchar(255) NULL,
	status varchar(255) NULL,
	user_id int8 NULL,
	CONSTRAINT users_status_pkey PRIMARY KEY (id),
	CONSTRAINT fksh94fsos58gq2g28maty4brrh FOREIGN KEY (user_id) REFERENCES bot.users(id)
);