alter session set container = XEPDB1;

--DROP SEQUENCE JAM_OWNER.RF_AUTHORIZATION_ID;
--DROP SEQUENCE JAM_OWNER.BEAM_AUTHORIZATION_ID;
--DROP SEQUENCE JAM_OWNER.RF_SEGMENT_ID;
--DROP SEQUENCE JAM_OWNER.BEAM_DESTINATION_ID;
--DROP SEQUENCE JAM_OWNER.CONTROL_VERIFICATION_ID;
--DROP SEQUENCE JAM_OWNER.CREDITED_CONTROL_ID;
--DROP SEQUENCE JAM_OWNER.VERIFICATION_HISTORY_ID;
--DROP SEQUENCE JAM_OWNER.WORKGROUP_ID;

--DROP TABLE JAM_OWNER.VERIFICATION_HISTORY CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.CONTROL_VERIFICATION CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.VERIFICATION CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.CREDITED_CONTROL CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.DESTINATION_AUTHORIZATION CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.AUTHORIZATION CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.BEAM_DESTINATION CASCADE CONSTRAINTS PURGE;
--DROP TABLE JAM_OWNER.WORKGROUP CASCADE CONSTRAINTS PURGE;

CREATE SEQUENCE JAM_OWNER.RF_AUTHORIZATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.BEAM_AUTHORIZATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.RF_SEGMENT_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.BEAM_DESTINATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.RF_CONTROL_VERIFICATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.BEAM_CONTROL_VERIFICATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.CREDITED_CONTROL_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.RF_CONTROL_VERIFICATION_HISTORY_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.BEAM_CONTROL_VERIFICATION_HISTORY_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.WORKGROUP_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE TABLE JAM_OWNER.WORKGROUP
(
    WORKGROUP_ID     INTEGER NOT NULL ,
    NAME             VARCHAR2(64 CHAR) NOT NULL ,
    LEADER_ROLE_NAME VARCHAR2(64 CHAR) NOT NULL ,
    CONSTRAINT WORKGROUP_PK PRIMARY KEY (WORKGROUP_ID)
);

CREATE TABLE JAM_OWNER.FACILITY
(
    FACILITY_ID       INTEGER NOT NULL,
    NAME              VARCHAR2(64 CHAR) NOT NULL,
    PATH              VARCHAR2(32 CHAR) NOT NULL,
    RF_WORKGROUP_ID   INTEGER NOT NULL,
    BEAM_WORKGROUP_ID INTEGER NOT NULL,
    WEIGHT            INTEGER NOT NULL,
    CONSTRAINT FACILITY_PK PRIMARY KEY (FACILITY_ID),
    CONSTRAINT FACILITY_AK1 UNIQUE (NAME),
    CONSTRAINT FACILITY_FK1 FOREIGN KEY (RF_WORKGROUP_ID) REFERENCES JAM_OWNER.WORKGROUP (WORKGROUP_ID),
    CONSTRAINT FACILITY_FK2 FOREIGN KEY (BEAM_WORKGROUP_ID) REFERENCES JAM_OWNER.WORKGROUP (WORKGROUP_ID)
);

CREATE TABLE JAM_OWNER.RF_SEGMENT
(
    RF_SEGMENT_ID INTEGER NOT NULL,
    NAME          VARCHAR2(64 CHAR) NOT NULL,
    FACILITY_ID   INTEGER NOT NULL,
    ACTIVE_YN     CHAR(1 BYTE) DEFAULT 'Y' NOT NULL,
    WEIGHT        INTEGER NOT NULL,
    CONSTRAINT RF_SEGMENT_PK PRIMARY KEY (RF_SEGMENT_ID),
    CONSTRAINT RF_SEGMENT_CK1 CHECK (ACTIVE_YN IN ('Y', 'N'))
);

CREATE TABLE JAM_OWNER.BEAM_DESTINATION
(
    BEAM_DESTINATION_ID  INTEGER NOT NULL,
    NAME                 VARCHAR2(64 CHAR) NOT NULL,
    FACILITY_ID          INTEGER NOT NULL,
    CURRENT_LIMIT_UNITS  VARCHAR2(3 CHAR) DEFAULT  'uA'  NOT NULL,
    ACTIVE_YN            CHAR(1 BYTE) DEFAULT  'Y'  NOT NULL,
    WEIGHT               INTEGER NOT NULL,
    CONSTRAINT BEAM_DESTINATION_PK PRIMARY KEY (BEAM_DESTINATION_ID),
    CONSTRAINT BEAM_DESTINATION_FK1 FOREIGN KEY (FACILITY_ID) REFERENCES JAM_OWNER.FACILITY (FACILITY_ID),
    CONSTRAINT BEAM_DESTINATION_CK1 CHECK (ACTIVE_YN IN ('Y', 'N'))
);

CREATE TABLE JAM_OWNER.RF_AUTHORIZATION
(
    RF_AUTHORIZATION_ID INTEGER NOT NULL,
    MODIFIED_DATE       DATE NOT NULL,
    MODIFIED_BY         VARCHAR2(64 CHAR) NOT NULL,
    AUTHORIZATION_DATE  DATE NOT NULL,
    AUTHORIZED_BY       VARCHAR(64 CHAR) NOT NULL,
    COMMENTS            VARCHAR2(2048 CHAR) NULL,
    CONSTRAINT RF_AUTHORIZATION_PK PRIMARY KEY (RF_AUTHORIZATION_ID)
);

CREATE TABLE JAM_OWNER.BEAM_AUTHORIZATION
(
    BEAM_AUTHORIZATION_ID INTEGER NOT NULL,
    MODIFIED_DATE         DATE NOT NULL,
    MODIFIED_BY           VARCHAR2(64 CHAR) NOT NULL,
    AUTHORIZATION_DATE    DATE NOT NULL,
    AUTHORIZED_BY         VARCHAR(64 CHAR) NOT NULL,
    COMMENTS              VARCHAR2(2048 CHAR) NULL,
    CONSTRAINT BEAM_AUTHORIZATION_PK PRIMARY KEY (BEAM_AUTHORIZATION_ID)
);

CREATE TABLE JAM_OWNER.BEAM_DESTINATION_AUTHORIZATION
(
    BEAM_DESTINATION_ID   INTEGER NOT NULL,
    BEAM_AUTHORIZATION_ID INTEGER NOT NULL,
    BEAM_MODE             VARCHAR2(16) NOT NULL,
    CW_LIMIT              NUMBER(24,12) NULL,
    COMMENTS              VARCHAR2(256) NULL,
    EXPIRATION_DATE       DATE NULL,
    CONSTRAINT BEAM_DESTINATION_AUTHORIZATION_PK PRIMARY KEY (BEAM_DESTINATION_ID,BEAM_AUTHORIZATION_ID),
    CONSTRAINT BEAM_DESTINATION_AUTHORIZATION_FK1 FOREIGN KEY (BEAM_DESTINATION_ID) REFERENCES JAM_OWNER.BEAM_DESTINATION (BEAM_DESTINATION_ID),
    CONSTRAINT BEAM_DESTINATION_AUTHORIZATION_FK2 FOREIGN KEY (BEAM_AUTHORIZATION_ID) REFERENCES JAM_OWNER.BEAM_AUTHORIZATION (BEAM_AUTHORIZATION_ID),
    CONSTRAINT BEAM_DESTINATION_AUTHORIZATION_CK1 CHECK (BEAM_MODE IN ('None', 'Tune', 'CW', 'Ceramic Viewer', 'Viewer Limited', 'High Duty Cycle', 'BLM Checkout', 'RF Only'))
);

CREATE TABLE JAM_OWNER.RF_SEGMENT_AUTHORIZATION
(
    RF_SEGMENT_ID       INTEGER NOT NULL,
    RF_AUTHORIZATION_ID INTEGER NOT NULL,
    RF_MODE             VARCHAR2(16) NOT NULL,
    COMMENTS            VARCHAR2(256) NULL,
    EXPIRATION_DATE     DATE NULL,
    CONSTRAINT RF_SEGMENT_AUTHORIZATION_PK PRIMARY KEY (RF_SEGMENT_ID,RF_AUTHORIZATION_ID),
    CONSTRAINT RF_SEGMENT_AUTHORIZATION_FK1 FOREIGN KEY (RF_SEGMENT_ID) REFERENCES JAM_OWNER.RF_SEGMENT (RF_SEGMENT_ID),
    CONSTRAINT RF_SEGMENT_AUTHORIZATION_FK2 FOREIGN KEY (RF_AUTHORIZATION_ID) REFERENCES JAM_OWNER.RF_AUTHORIZATION (RF_AUTHORIZATION_ID),
    CONSTRAINT RF_SEGMENT_AUTHORIZATION_CK1 CHECK (RF_MODE IN ('None', 'RF ON'))
);

CREATE TABLE JAM_OWNER.CREDITED_CONTROL
(
    CREDITED_CONTROL_ID    INTEGER NOT NULL,
    NAME                   VARCHAR2(128 CHAR) NOT NULL,
    DESCRIPTION            VARCHAR2(2048 CHAR) NULL,
    WORKGROUP_ID           INTEGER NOT NULL,
    WEIGHT                 INTEGER NULL,
    VERIFICATION_FREQUENCY VARCHAR2(128 CHAR) NULL,
    COMMENTS               VARCHAR2(2048) NULL,
    CONSTRAINT CREDITED_CONTROL_PK PRIMARY KEY (CREDITED_CONTROL_ID),
    CONSTRAINT CREDITED_CONTROL_FK1 FOREIGN KEY (WORKGROUP_ID) REFERENCES JAM_OWNER.WORKGROUP(WORKGROUP_ID)
);

CREATE TABLE JAM_OWNER.VERIFICATION_STATUS
(
    VERIFICATION_STATUS_ID INTEGER NOT NULL ,
    NAME                   VARCHAR2(128 CHAR) NOT NULL ,
    CONSTRAINT VERIFICATION_STATUS_PK PRIMARY KEY (VERIFICATION_STATUS_ID)
);

CREATE TABLE JAM_OWNER.RF_CONTROL_VERIFICATION
(
    RF_CONTROL_VERIFICATION_ID INTEGER NOT NULL,
    CREDITED_CONTROL_ID        INTEGER NULL,
    RF_SEGMENT_ID              INTEGER NOT NULL,
    VERIFICATION_STATUS_ID     INTEGER NOT NULL,
    VERIFICATION_DATE          DATE NULL,
    VERIFIED_BY                VARCHAR(64 CHAR) NULL,
    EXPIRATION_DATE            DATE NULL,
    COMMENTS                   VARCHAR2(2048 CHAR) NULL,
    MODIFIED_BY                VARCHAR2(64 CHAR) NOT NULL,
    MODIFIED_DATE              DATE NOT NULL,
    CONSTRAINT RF_CONTROL_VERIFICATION_PK PRIMARY KEY (RF_CONTROL_VERIFICATION_ID),
    CONSTRAINT RF_CONTROL_VERIFICATION_AK1 UNIQUE (CREDITED_CONTROL_ID,RF_SEGMENT_ID),
    CONSTRAINT RF_CONTROL_VERIFICATION_FK1 FOREIGN KEY (CREDITED_CONTROL_ID) REFERENCES JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID) ON DELETE CASCADE,
    CONSTRAINT RF_CONTROL_VERIFICATION_FK2 FOREIGN KEY (RF_SEGMENT_ID) REFERENCES JAM_OWNER.RF_SEGMENT (RF_SEGMENT_ID) ON DELETE CASCADE,
    CONSTRAINT RF_CONTROL_VERIFICATION_FK3 FOREIGN KEY (VERIFICATION_STATUS_ID) REFERENCES JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID) ON DELETE SET NULL
);

CREATE TABLE JAM_OWNER.BEAM_CONTROL_VERIFICATION
(
    BEAM_CONTROL_VERIFICATION_ID INTEGER NOT NULL,
    CREDITED_CONTROL_ID          INTEGER NULL,
    BEAM_DESTINATION_ID          INTEGER NOT NULL,
    VERIFICATION_STATUS_ID       INTEGER NOT NULL,
    VERIFICATION_DATE            DATE NULL,
    VERIFIED_BY                  VARCHAR(64 CHAR) NULL,
    EXPIRATION_DATE              DATE NULL,
    COMMENTS                     VARCHAR2(2048 CHAR) NULL,
    MODIFIED_BY                  VARCHAR2(64 CHAR) NOT NULL,
    MODIFIED_DATE                DATE NOT NULL,
    CONSTRAINT BEAM_CONTROL_VERIFICATION_PK PRIMARY KEY (BEAM_CONTROL_VERIFICATION_ID),
    CONSTRAINT BEAM_CONTROL_VERIFICATION_AK1 UNIQUE (CREDITED_CONTROL_ID,BEAM_DESTINATION_ID),
    CONSTRAINT BEAM_CONTROL_VERIFICATION_FK1 FOREIGN KEY (CREDITED_CONTROL_ID) REFERENCES JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID) ON DELETE CASCADE,
    CONSTRAINT BEAM_CONTROL_VERIFICATION_FK2 FOREIGN KEY (BEAM_DESTINATION_ID) REFERENCES JAM_OWNER.BEAM_DESTINATION (BEAM_DESTINATION_ID) ON DELETE CASCADE,
    CONSTRAINT BEAM_CONTROL_VERIFICATION_FK3 FOREIGN KEY (VERIFICATION_STATUS_ID) REFERENCES JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID) ON DELETE SET NULL
);

CREATE TABLE JAM_OWNER.RF_CONTROL_VERIFICATION_HISTORY
(
    RF_CONTROL_VERIFICATION_HISTORY_ID INTEGER NOT NULL,
    RF_CONTROL_VERIFICATION_ID         INTEGER NOT NULL,
    VERIFICATION_STATUS_ID             INTEGER NOT NULL,
    VERIFIED_BY                        VARCHAR2(64 CHAR) NULL,
    VERIFICATION_DATE                  DATE NOT NULL,
    EXPIRATION_DATE                    DATE NULL,
    COMMENTS                           VARCHAR2(2048 CHAR) NULL,
    MODIFIED_BY                        VARCHAR2(64 CHAR) NOT NULL,
    MODIFIED_DATE                      DATE NOT NULL,
    CONSTRAINT RF_CONTROL_VERIFICATION_HISTORY_PK PRIMARY KEY (RF_CONTROL_VERIFICATION_HISTORY_ID),
    CONSTRAINT RF_CONTROL_VERIFICATION_HISTORY_FK1 FOREIGN KEY (RF_CONTROL_VERIFICATION_ID) REFERENCES JAM_OWNER.RF_CONTROL_VERIFICATION (RF_CONTROL_VERIFICATION_ID) ON DELETE CASCADE,
    CONSTRAINT RF_CONTROL_VERIFICATION_HISTORY_FK3 FOREIGN KEY (VERIFICATION_STATUS_ID) REFERENCES JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID) ON DELETE SET NULL
);

CREATE TABLE JAM_OWNER.BEAM_CONTROL_VERIFICATION_HISTORY
(
    BEAM_CONTROL_VERIFICATION_HISTORY_ID INTEGER NOT NULL,
    BEAM_CONTROL_VERIFICATION_ID         INTEGER NOT NULL,
    VERIFICATION_STATUS_ID               INTEGER NOT NULL,
    VERIFIED_BY                          VARCHAR2(64 CHAR) NULL,
    VERIFICATION_DATE                    DATE NOT NULL,
    EXPIRATION_DATE                      DATE NULL,
    COMMENTS                             VARCHAR2(2048 CHAR) NULL,
    MODIFIED_BY                          VARCHAR2(64 CHAR) NOT NULL,
    MODIFIED_DATE                        DATE NOT NULL,
    CONSTRAINT BEAM_CONTROL_VERIFICATION_HISTORY_PK PRIMARY KEY (BEAM_CONTROL_VERIFICATION_HISTORY_ID),
    CONSTRAINT BEAM_CONTROL_VERIFICATION_HISTORY_FK1 FOREIGN KEY (BEAM_CONTROL_VERIFICATION_ID) REFERENCES JAM_OWNER.BEAM_CONTROL_VERIFICATION (BEAM_CONTROL_VERIFICATION_ID) ON DELETE CASCADE,
    CONSTRAINT BEAM_CONTROL_VERIFICATION_HISTORY_FK3 FOREIGN KEY (VERIFICATION_STATUS_ID) REFERENCES JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID) ON DELETE SET NULL
);

CREATE OR REPLACE FORCE VIEW JAM_OWNER.BEAM_DESTINATION_VERIFICATION (BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, EXPIRATION_DATE) AS
SELECT a.beam_destination_id,
       NVL((SELECT MAX(VERIFICATION_STATUS_ID) FROM JAM_OWNER.BEAM_CONTROL_VERIFICATION b WHERE a.beam_destination_id = b.beam_destination_id), 1) AS VERIFICATION_STATUS_ID,
       (SELECT MIN(EXPIRATION_DATE) FROM JAM_OWNER.BEAM_CONTROL_VERIFICATION b WHERE a.beam_destination_id = b.beam_destination_id) as EXPIRATION_DATE
FROM JAM_OWNER.BEAM_DESTINATION a;

CREATE TABLE JAM_OWNER.COMPONENT
(
    COMPONENT_ID         NUMBER NOT NULL CONSTRAINT COMPONENT_PK PRIMARY KEY,
    NAME                 VARCHAR2(128 char) NOT NULL CONSTRAINT COMPONENT_CK4 CHECK (INSTR(NAME, '*') = 0),
    STATUS_ID            INTEGER NOT NULL,
    CONSTRAINT COMPONENT_AK1 UNIQUE (NAME)
);

/*grant select on srm_owner.component to jam_owner;
  grant select on srm_owner.component_status_2 to jam_owner;
  grant select on system_application to jam_owner;
  create or replace view jam_owner.component as
(
select component_id, name, status_id from srm_owner.component join srm_owner.component_status_2 using (component_id) where system_id in (select system_id from srm_owner.system_application where application_id = 1)
);*/

CREATE TABLE JAM_OWNER.BEAM_CONTROL_VERIFICATION_COMPONENT
(
    BEAM_CONTROL_VERIFICATION_ID INTEGER NOT NULL ,
    COMPONENT_ID                 INTEGER NOT NULL ,
    CONSTRAINT BEAM_CONTROL_VERIFICATION_COMPONENT_PK PRIMARY KEY (BEAM_CONTROL_VERIFICATION_ID, COMPONENT_ID),
    CONSTRAINT BEAM_CONTROL_VERIFICATION_COMPONENT_FK1 FOREIGN KEY (BEAM_CONTROL_VERIFICATION_ID) REFERENCES JAM_OWNER.BEAM_CONTROL_VERIFICATION (BEAM_CONTROL_VERIFICATION_ID) ON DELETE CASCADE
);

CREATE TABLE JAM_OWNER.RF_CONTROL_VERIFICATION_COMPONENT
(
    RF_CONTROL_VERIFICATION_ID INTEGER NOT NULL ,
    COMPONENT_ID               INTEGER NOT NULL ,
    CONSTRAINT RF_CONTROL_VERIFICATION_COMPONENT_PK PRIMARY KEY (RF_CONTROL_VERIFICATION_ID, COMPONENT_ID),
    CONSTRAINT RF_CONTROL_VERIFICATION_COMPONENT_FK1 FOREIGN KEY (RF_CONTROL_VERIFICATION_ID) REFERENCES JAM_OWNER.RF_CONTROL_VERIFICATION (RF_CONTROL_VERIFICATION_ID) ON DELETE CASCADE
);