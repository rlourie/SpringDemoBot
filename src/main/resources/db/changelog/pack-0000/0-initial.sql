CREATE TABLE bot.users (
	id BIGSERIAL  NOT NULL,
	name varchar(255) NULL,
	"number" varchar(255) NULL,
	CONSTRAINT users_pkey PRIMARY KEY (id)
);
CREATE TABLE bot.users_status (
	id BIGSERIAL  NOT NULL,
	command varchar(255) NULL,
	status varchar(255) NULL,
	user_id bigint NOT NULL,
	CONSTRAINT users_status_pkey PRIMARY KEY (id)
);


ALTER TABLE bot.users_status ADD CONSTRAINT fksh94fsos58gq2g28maty4brrh FOREIGN KEY (user_id) REFERENCES bot.users(id);