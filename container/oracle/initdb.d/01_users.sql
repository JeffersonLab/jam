alter session set container = XEPDB1;

ALTER SYSTEM SET db_create_file_dest = '/opt/oracle/oradata';

create tablespace JAM;

create user "JAM_4202C_OWNER" profile "DEFAULT" identified by "password" default tablespace "JAM" account unlock;

grant connect to JAM_4202C_OWNER;
grant unlimited tablespace to JAM_4202C_OWNER;

grant create view to JAM_4202C_OWNER;
grant create sequence to JAM_4202C_OWNER;
grant create table to JAM_4202C_OWNER;
grant create procedure to JAM_4202C_OWNER;
grant create type to JAM_4202C_OWNER;