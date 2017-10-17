CREATE TYPE batch_type AS ENUM ('foo');

CREATE TABLE batch (
	id UUID NOT NULL,
	type batch_type NOT NULL,
	remain INT NOT NULL,
	total INT NOT NULL,
	created TIMESTAMPTZ DEFAULT now() NOT NULL,
	modified TIMESTAMPTZ DEFAULT now() NOT NULL,
	CONSTRAINT batch_pk PRIMARY KEY (id)
);
