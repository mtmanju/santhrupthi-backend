create sequence if not exists public.donations_id_seq
	start with 1
	increment by 1
	no minvalue
	no maxvalue
	cache 1;
CREATE TABLE if not exists public.donations  (
	id bigint primary key default nextval('public.donations_id_seq'::regclass) not null,
	donation_id varchar(255) NOT NULL,
	order_id varchar(255) NULL,
	"name" varchar(255) NULL,
	phone varchar(20) NULL,
	email varchar(255) NULL,
	amount numeric(38, 2) NOT NULL,
	pan_card varchar(20) NULL,
	address text NULL,
	message text NULL,
	currency varchar(10) NOT NULL DEFAULT 'INR',
	status varchar(50) NOT NULL DEFAULT 'CREATED',
	payment_method varchar(50) NULL DEFAULT 'UPI',
	donation_date timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	signature varchar(255) NULL,
	created_by varchar(255) NOT NULL,
	created_on timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	modified_by varchar(255) NOT null,
	modified_on timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	payment_id varchar(255) NULL,
	CONSTRAINT donations_donation_id_key UNIQUE (donation_id)
);