alter session set container = XEPDB1;

ALTER SYSTEM SET db_create_file_dest = '/opt/oracle/oradata';

create tablespace JAM;

create user "JAM_OWNER" profile "DEFAULT" identified by "password" default tablespace "JAM" account unlock;

grant connect to JAM_OWNER;
grant unlimited tablespace to JAM_OWNER;

grant create view to JAM_OWNER;
grant create sequence to JAM_OWNER;
grant create table to JAM_OWNER;
grant create procedure to JAM_OWNER;
grant create type to JAM_OWNER;