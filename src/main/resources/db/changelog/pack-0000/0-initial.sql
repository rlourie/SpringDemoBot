CREATE TABLE bot.users (
	id int8 NOT NULL,
	name varchar(255) NULL,
	"number" varchar(255) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);
CREATE TABLE bot.users_status (
	id int8 NOT NULL,
	command varchar(255) NULL,
	status varchar(255) NULL,
	user_id int4 NULL,
	CONSTRAINT users_status_pkey PRIMARY KEY (id)
);


ALTER TABLE bot.users_status ADD CONSTRAINT fksh94fsos58gq2g28maty4brrh FOREIGN KEY (user_id) REFERENCES bot.users(id);