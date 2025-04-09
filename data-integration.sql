--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4 (Debian 17.4-1.pgdg120+2)
-- Dumped by pg_dump version 17.4 (Debian 17.4-1.pgdg120+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: data-integration; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "data-integration" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.UTF-8';


ALTER DATABASE "data-integration" OWNER TO postgres;

\encoding SQL_ASCII
\connect -reuse-previous=on "dbname='data-integration'"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: checkpoint; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.checkpoint (
    id uuid NOT NULL,
    batch_size integer NOT NULL,
    total_batches bigint NOT NULL,
    scope_id uuid NOT NULL
);


ALTER TABLE public.checkpoint OWNER TO postgres;

--
-- Name: checkpoint_batch; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.checkpoint_batch (
    id uuid NOT NULL,
    batch_index bigint NOT NULL,
    checkpoint_id uuid NOT NULL
);


ALTER TABLE public.checkpoint_batch OWNER TO postgres;

--
-- Name: database; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.database (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    host_id uuid NOT NULL
);


ALTER TABLE public.database OWNER TO postgres;

--
-- Name: host; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.host (
    id uuid NOT NULL,
    base_url character varying(255) NOT NULL,
    header_path character varying(255) NOT NULL,
    headers jsonb,
    integration_path character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.host OWNER TO postgres;

--
-- Name: item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.item (
    id uuid NOT NULL,
    line_number bigint NOT NULL,
    properties jsonb NOT NULL,
    scope_id uuid NOT NULL
);


ALTER TABLE public.item OWNER TO postgres;

--
-- Name: mapped_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mapped_item (
    id uuid NOT NULL,
    error_messages jsonb,
    properties jsonb,
    status character varying(255) NOT NULL,
    item_id uuid NOT NULL,
    mapping_id uuid NOT NULL
);


ALTER TABLE public.mapped_item OWNER TO postgres;

--
-- Name: mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mapping (
    id uuid NOT NULL,
    created_date timestamp(6) without time zone NOT NULL,
    delete boolean NOT NULL,
    finished boolean NOT NULL,
    last_processed_batch bigint NOT NULL,
    mapping jsonb NOT NULL,
    name character varying(255) NOT NULL,
    processing boolean NOT NULL,
    database_id uuid NOT NULL,
    scope_id uuid NOT NULL
);


ALTER TABLE public.mapping OWNER TO postgres;

--
-- Name: project; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project (
    id uuid NOT NULL,
    created_by character varying(255) NOT NULL,
    created_date timestamp(6) without time zone NOT NULL,
    delete boolean NOT NULL,
    last_modified_date timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.project OWNER TO postgres;

--
-- Name: scope; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.scope (
    id uuid NOT NULL,
    created_date timestamp(6) without time zone NOT NULL,
    delete boolean NOT NULL,
    external boolean NOT NULL,
    finished boolean NOT NULL,
    headers jsonb,
    key character varying(255) NOT NULL,
    project_id uuid NOT NULL
);


ALTER TABLE public.scope OWNER TO postgres;

--
-- Data for Name: checkpoint; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.checkpoint (id, batch_size, total_batches, scope_id) FROM stdin;
\.


--
-- Data for Name: checkpoint_batch; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.checkpoint_batch (id, batch_index, checkpoint_id) FROM stdin;
\.


--
-- Data for Name: database; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.database (id, name, host_id) FROM stdin;
\.


--
-- Data for Name: host; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.host (id, base_url, header_path, headers, integration_path, name) FROM stdin;
\.


--
-- Data for Name: item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.item (id, line_number, properties, scope_id) FROM stdin;
\.


--
-- Data for Name: mapped_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mapped_item (id, error_messages, properties, status, item_id, mapping_id) FROM stdin;
\.


--
-- Data for Name: mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mapping (id, created_date, delete, finished, last_processed_batch, mapping, name, processing, database_id, scope_id) FROM stdin;
\.


--
-- Data for Name: project; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project (id, created_by, created_date, delete, last_modified_date, name) FROM stdin;
\.


--
-- Data for Name: scope; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.scope (id, created_date, delete, external, finished, headers, key, project_id) FROM stdin;
\.


--
-- Name: checkpoint_batch checkpoint_batch_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.checkpoint_batch
    ADD CONSTRAINT checkpoint_batch_pkey PRIMARY KEY (id);


--
-- Name: checkpoint checkpoint_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.checkpoint
    ADD CONSTRAINT checkpoint_pkey PRIMARY KEY (id);


--
-- Name: database database_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.database
    ADD CONSTRAINT database_pkey PRIMARY KEY (id);


--
-- Name: host host_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.host
    ADD CONSTRAINT host_pkey PRIMARY KEY (id);


--
-- Name: item item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT item_pkey PRIMARY KEY (id);


--
-- Name: mapped_item mapped_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapped_item
    ADD CONSTRAINT mapped_item_pkey PRIMARY KEY (id);


--
-- Name: mapping mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapping
    ADD CONSTRAINT mapping_pkey PRIMARY KEY (id);


--
-- Name: project project_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- Name: scope scope_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope
    ADD CONSTRAINT scope_pkey PRIMARY KEY (id);


--
-- Name: mapped_item ukd2tx23cunu24y0oqgs5i1r0m6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapped_item
    ADD CONSTRAINT ukd2tx23cunu24y0oqgs5i1r0m6 UNIQUE (item_id, mapping_id);


--
-- Name: idx_item_line_number; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_item_line_number ON public.item USING btree (line_number);


--
-- Name: idx_item_scope_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_item_scope_id ON public.item USING btree (scope_id);


--
-- Name: idx_mapped_item_item_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mapped_item_item_id ON public.mapped_item USING btree (item_id);


--
-- Name: idx_mapped_item_item_id_mapping_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mapped_item_item_id_mapping_id ON public.mapped_item USING btree (item_id, mapping_id);


--
-- Name: idx_mapped_item_mapping_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mapped_item_mapping_id ON public.mapped_item USING btree (mapping_id);


--
-- Name: idx_mapped_item_mapping_id_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mapped_item_mapping_id_status ON public.mapped_item USING btree (mapping_id, status);


--
-- Name: mapped_item fk6edf5gj2fynogxbyhe7kl76p5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapped_item
    ADD CONSTRAINT fk6edf5gj2fynogxbyhe7kl76p5 FOREIGN KEY (mapping_id) REFERENCES public.mapping(id);


--
-- Name: mapping fkb7yiydr8dvqf6tif440w6rj9x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapping
    ADD CONSTRAINT fkb7yiydr8dvqf6tif440w6rj9x FOREIGN KEY (database_id) REFERENCES public.database(id);


--
-- Name: checkpoint_batch fkbl43sjv53alegpj5bbdr2xccu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.checkpoint_batch
    ADD CONSTRAINT fkbl43sjv53alegpj5bbdr2xccu FOREIGN KEY (checkpoint_id) REFERENCES public.checkpoint(id);


--
-- Name: checkpoint fkee4r535bbra7v6iyhamx06ehk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.checkpoint
    ADD CONSTRAINT fkee4r535bbra7v6iyhamx06ehk FOREIGN KEY (scope_id) REFERENCES public.scope(id);


--
-- Name: scope fkh8worxua0v01myptk8sog87iu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scope
    ADD CONSTRAINT fkh8worxua0v01myptk8sog87iu FOREIGN KEY (project_id) REFERENCES public.project(id);


--
-- Name: mapped_item fkib1gny4fa52a9vyvgx3a3dwwt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapped_item
    ADD CONSTRAINT fkib1gny4fa52a9vyvgx3a3dwwt FOREIGN KEY (item_id) REFERENCES public.item(id);


--
-- Name: item fkjg4v0oornw0k2dph1b0e90kfl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.item
    ADD CONSTRAINT fkjg4v0oornw0k2dph1b0e90kfl FOREIGN KEY (scope_id) REFERENCES public.scope(id);


--
-- Name: database fkohd8gl2jwac0ldnny5qbuyees; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.database
    ADD CONSTRAINT fkohd8gl2jwac0ldnny5qbuyees FOREIGN KEY (host_id) REFERENCES public.host(id);


--
-- Name: mapping fkpn0lambsq8gtchk672xv582u3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mapping
    ADD CONSTRAINT fkpn0lambsq8gtchk672xv582u3 FOREIGN KEY (scope_id) REFERENCES public.scope(id);


--
-- PostgreSQL database dump complete
--

