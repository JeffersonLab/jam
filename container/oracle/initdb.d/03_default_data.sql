alter session set container = XEPDB1;

-- Populate Verification
insert into JAM_4202C_OWNER.VERIFICATION (VERIFICATION_ID, NAME) values (1, 'Verified');
insert into JAM_4202C_OWNER.VERIFICATION (VERIFICATION_ID, NAME) values (50, 'Provisionally Verified');
insert into JAM_4202C_OWNER.VERIFICATION (VERIFICATION_ID, NAME) values (100, 'Not Verified');

-- Populate Workgroup
insert into JAM_4202C_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (JAM_4202C_OWNER.WORKGROUP_ID.nextval, 'Group 1', 'group1Leaders');
insert into JAM_4202C_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (JAM_4202C_OWNER.WORKGROUP_ID.nextval, 'Group 2', 'group2Leaders');
insert into JAM_4202C_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (JAM_4202C_OWNER.WORKGROUP_ID.nextval, 'Group 3', 'group3Leaders');

-- Populate Credited Controls
insert into JAM_4202C_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_4202C_OWNER.CREDITED_CONTROL_ID.nextval,'Control 1','Control 1 Description',1,1,'1 Year');
insert into JAM_4202C_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_4202C_OWNER.CREDITED_CONTROL_ID.nextval,'Control 2','Control 2 Description',1,2,'1 Year');
insert into JAM_4202C_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_4202C_OWNER.CREDITED_CONTROL_ID.nextval,'Control 3','Control 3 Description',1,3,'1 Year');
insert into JAM_4202C_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_4202C_OWNER.CREDITED_CONTROL_ID.nextval,'Control 4','Control 4 Description',1,4,'1 Year');
insert into JAM_4202C_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_4202C_OWNER.CREDITED_CONTROL_ID.nextval,'Control 5','Control 5 Description',1,5,'1 Year');
insert into JAM_4202C_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_4202C_OWNER.CREDITED_CONTROL_ID.nextval,'Control 6','Control 6 Description',1,6,'1 Year');

-- Populate Beam Destinations
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 1', 'CEBAF', 'uA', 'Y', 1);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 2', 'CEBAF', 'uA', 'Y', 2);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 3', 'CEBAF', 'uA', 'Y', 3);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 4', 'LERF', 'uA', 'Y', 4);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 5', 'LERF', 'uA', 'Y', 5);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 6', 'LERF', 'uA', 'Y', 6);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 7', 'UITF', 'uA', 'Y', 7);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 8', 'UITF', 'uA', 'Y', 8);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Destination 9', 'UITF', 'uA', 'Y', 9);
insert into JAM_4202C_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, MACHINE, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_4202C_OWNER.destination_id.nextval, 'Injector RF Operations', 'CEBAF', 'uA', 'Y', 9);

-- Populate Initial Control Verification
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 1, 1, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 2, 2, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 3, 3, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 4, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 5, 5, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 6, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 1, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 2, 5, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 3, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 4, 3, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 5, 2, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 6, 1, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 1, 9, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 2, 8, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 3, 7, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 4, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 5, 9, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 6, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
-- Test null VERIFIED_BY (Control 6, Destination 9)
insert into JAM_4202C_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.control_verification_id.nextval, 6, 9, 100, sysdate, null, null, null, 'admin', sysdate);

-- Populate Initial Verification History (Control 1, Destination 1)
insert into JAM_4202C_OWNER.verification_history (VERIFICATION_HISTORY_ID, CONTROL_VERIFICATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_4202C_OWNER.verification_history_id.nextval, 1, 100, sysdate, 'tbrown', null, null, 'tbrown', sysdate);

-- Populate Initial Authorization
insert into JAM_4202C_OWNER.authorization (AUTHORIZATION_ID, MODIFIED_BY, MODIFIED_DATE, AUTHORIZATION_DATE, AUTHORIZED_BY, COMMENTS) values(JAM_4202C_OWNER.authorization_id.nextval, 'tbrown', sysdate, sysdate, 'tbrown', 'testing');

-- Populate Initial Destination Authorization
insert into JAM_4202C_OWNER.destination_authorization (BEAM_DESTINATION_ID, AUTHORIZATION_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(1, 1, 'None', null, 'test 1', null);
insert into JAM_4202C_OWNER.destination_authorization (BEAM_DESTINATION_ID, AUTHORIZATION_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(2, 1, 'Tune', null, 'test 2', null);
insert into JAM_4202C_OWNER.destination_authorization (BEAM_DESTINATION_ID, AUTHORIZATION_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(3, 1, 'CW', 10, 'test 3', null);