alter session set container = XEPDB1;

--DROP SEQUENCE JAM_OWNER.AUTHORIZATION_ID;
--DROP SEQUENCE JAM_OWNER.DESTINATION_ID;
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

CREATE SEQUENCE JAM_OWNER.AUTHORIZATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.DESTINATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.CONTROL_VERIFICATION_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.CREDITED_CONTROL_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.VERIFICATION_HISTORY_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;
CREATE SEQUENCE JAM_OWNER.WORKGROUP_ID
    START WITH 1
    NOCYCLE
    NOCACHE
    ORDER;

CREATE TABLE JAM_OWNER.BEAM_DESTINATION
(
    BEAM_DESTINATION_ID  INTEGER NOT NULL ,
    NAME                 VARCHAR2(64 CHAR) NOT NULL ,
    MACHINE              VARCHAR2(32 CHAR) DEFAULT  'CEBAF'  NOT NULL ,
    CURRENT_LIMIT_UNITS  VARCHAR2(3 CHAR) DEFAULT  'uA'  NOT NULL ,
    ACTIVE_YN            CHAR(1 BYTE) DEFAULT  'Y'  NOT NULL CONSTRAINT BEAM_DESTINATION_CK1 CHECK (ACTIVE_YN IN ('Y', 'N')),
    WEIGHT               INTEGER NOT NULL,
    CONSTRAINT BEAM_DESTINATION_PK PRIMARY KEY (BEAM_DESTINATION_ID)
);

CREATE TABLE JAM_OWNER.AUTHORIZATION
(
    AUTHORIZATION_ID     INTEGER NOT NULL ,
    MODIFIED_DATE        DATE NOT NULL ,
    MODIFIED_BY          VARCHAR2(64 CHAR) NOT NULL ,
    AUTHORIZATION_DATE   DATE NOT NULL ,
    AUTHORIZED_BY        VARCHAR(64 CHAR) NOT NULL ,
    COMMENTS             VARCHAR2(2048 CHAR) NULL ,
    CONSTRAINT AUTHORIZATION_PK PRIMARY KEY (AUTHORIZATION_ID)
);

CREATE TABLE JAM_OWNER.DESTINATION_AUTHORIZATION
(
    BEAM_DESTINATION_ID  INTEGER NOT NULL ,
    AUTHORIZATION_ID     INTEGER NOT NULL ,
    BEAM_MODE            VARCHAR2(16) NOT NULL  CONSTRAINT  DESTINATION_AUTHORIZATION_CK1 CHECK (BEAM_MODE IN ('None', 'Tune', 'CW', 'Ceramic Viewer', 'Viewer Limited', 'High Duty Cycle', 'BLM Checkout', 'RF Only')),
    CW_LIMIT             NUMBER(24,12) NULL ,
    COMMENTS             VARCHAR2(256) NULL ,
    EXPIRATION_DATE      DATE NULL ,
    CONSTRAINT DESTINATION_AUTHORIZATION_PK PRIMARY KEY (BEAM_DESTINATION_ID,AUTHORIZATION_ID),
    CONSTRAINT DESTINATION_AUTHORIZATION_FK1 FOREIGN KEY (BEAM_DESTINATION_ID) REFERENCES JAM_OWNER.BEAM_DESTINATION (BEAM_DESTINATION_ID),
    CONSTRAINT DESTINATION_AUTHORIZATION_FK2 FOREIGN KEY (AUTHORIZATION_ID) REFERENCES JAM_OWNER.AUTHORIZATION (AUTHORIZATION_ID)
);

CREATE TABLE JAM_OWNER.WORKGROUP
(
    WORKGROUP_ID     INTEGER NOT NULL ,
    NAME             VARCHAR2(64 CHAR) NOT NULL ,
    LEADER_ROLE_NAME VARCHAR2(64 CHAR) NOT NULL ,
    CONSTRAINT WORKGROUP_PK PRIMARY KEY (WORKGROUP_ID)
);

CREATE TABLE JAM_OWNER.CREDITED_CONTROL
(
    CREDITED_CONTROL_ID  INTEGER NOT NULL ,
    NAME                 VARCHAR2(128 CHAR) NOT NULL ,
    DESCRIPTION          VARCHAR2(2048 CHAR) NULL ,
    WORKGROUP_ID         INTEGER NOT NULL ,
    WEIGHT               INTEGER NULL ,
    VERIFICATION_FREQUENCY VARCHAR2(128 CHAR) NULL ,
    COMMENTS             VARCHAR2(2048) NULL ,
    CONSTRAINT CREDITED_CONTROL_PK PRIMARY KEY (CREDITED_CONTROL_ID),
    CONSTRAINT CREDITED_CONTROL_FK1 FOREIGN KEY (WORKGROUP_ID) REFERENCES JAM_OWNER.WORKGROUP(WORKGROUP_ID)
);

CREATE TABLE JAM_OWNER.VERIFICATION
(
    VERIFICATION_ID      INTEGER NOT NULL ,
    NAME                 VARCHAR2(128 CHAR) NOT NULL ,
    CONSTRAINT VERIFICATION_PK PRIMARY KEY (VERIFICATION_ID)
);

CREATE TABLE JAM_OWNER.CONTROL_VERIFICATION
(
    CONTROL_VERIFICATION_ID INTEGER NOT NULL ,
    CREDITED_CONTROL_ID  INTEGER NULL ,
    BEAM_DESTINATION_ID  INTEGER NOT NULL ,
    VERIFICATION_ID      INTEGER NOT NULL,
    VERIFICATION_DATE    DATE NULL ,
    VERIFIED_BY          VARCHAR(64 CHAR) NULL ,
    EXPIRATION_DATE      DATE NULL ,
    COMMENTS             VARCHAR2(2048 CHAR) NULL ,
    MODIFIED_BY          VARCHAR2(64 CHAR) NOT NULL ,
    MODIFIED_DATE        DATE NOT NULL ,
    CONSTRAINT CONTROL_VERIFICATION_PK PRIMARY KEY (CONTROL_VERIFICATION_ID),CONSTRAINT  CONTROL_VERIFICATION_AK1 UNIQUE (CREDITED_CONTROL_ID,BEAM_DESTINATION_ID),
    CONSTRAINT CONTROL_VERIFICATION_FK1 FOREIGN KEY (CREDITED_CONTROL_ID) REFERENCES JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID) ON DELETE CASCADE,
    CONSTRAINT CONTROL_VERIFICATION_FK2 FOREIGN KEY (BEAM_DESTINATION_ID) REFERENCES JAM_OWNER.BEAM_DESTINATION (BEAM_DESTINATION_ID) ON DELETE CASCADE,
    CONSTRAINT CONTROL_VERIFICATION_FK3 FOREIGN KEY (VERIFICATION_ID) REFERENCES JAM_OWNER.VERIFICATION (VERIFICATION_ID) ON DELETE SET NULL
);

CREATE TABLE JAM_OWNER.VERIFICATION_HISTORY
(
    VERIFICATION_HISTORY_ID INTEGER NOT NULL ,
    CONTROL_VERIFICATION_ID INTEGER NOT NULL ,
    VERIFICATION_ID      INTEGER NOT NULL ,
    VERIFIED_BY          VARCHAR2(64 CHAR) NULL ,
    VERIFICATION_DATE    DATE NOT NULL ,
    EXPIRATION_DATE      DATE NULL ,
    COMMENTS             VARCHAR2(2048 CHAR) NULL ,
    MODIFIED_BY          VARCHAR2(64 CHAR) NOT NULL ,
    MODIFIED_DATE        DATE NOT NULL ,
    CONSTRAINT VERIFICATION_HISTORY_PK PRIMARY KEY (VERIFICATION_HISTORY_ID),
    CONSTRAINT VERIFICATION_HISTORY_FK1 FOREIGN KEY (CONTROL_VERIFICATION_ID) REFERENCES JAM_OWNER.CONTROL_VERIFICATION (CONTROL_VERIFICATION_ID) ON DELETE CASCADE,
    CONSTRAINT VERIFICATION_HISTORY_FK3 FOREIGN KEY (VERIFICATION_ID) REFERENCES JAM_OWNER.VERIFICATION (VERIFICATION_ID) ON DELETE SET NULL
);

CREATE OR REPLACE FORCE VIEW JAM_OWNER.BEAM_DESTINATION_VERIFICATION (BEAM_DESTINATION_ID, VERIFICATION_ID, EXPIRATION_DATE) AS
SELECT a.beam_destination_id,
       NVL((SELECT MAX(VERIFICATION_ID) FROM JAM_OWNER.CONTROL_VERIFICATION b WHERE a.beam_destination_id = b.beam_destination_id), 1) AS VERIFICATION_ID,
       (SELECT MIN(EXPIRATION_DATE) FROM JAM_OWNER.CONTROL_VERIFICATION b WHERE a.beam_destination_id = b.beam_destination_id) as EXPIRATION_DATE
FROM JAM_OWNER.BEAM_DESTINATION a;
