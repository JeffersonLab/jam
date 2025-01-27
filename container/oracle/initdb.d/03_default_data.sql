alter session set container = XEPDB1;

-- Populate Verification
insert into JAM_OWNER.VERIFICATION (VERIFICATION_ID, NAME) values (1, 'Verified');
insert into JAM_OWNER.VERIFICATION (VERIFICATION_ID, NAME) values (50, 'Provisionally Verified');
insert into JAM_OWNER.VERIFICATION (VERIFICATION_ID, NAME) values (100, 'Not Verified');

-- Populate Workgroup
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (1, 'Group 1', 'group1Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (2, 'Group 2', 'group2Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (3, 'Group 3', 'group3Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (4, 'Group 4', 'group4Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (5, 'Group 5', 'group5Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (6, 'Group 6', 'group6Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (7, 'Group 7', 'group7Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (8, 'Group 8', 'group8Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (9, 'Group 9', 'group9Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (10, 'Group 10', 'group10Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (11, 'Group 11', 'group11Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (12, 'Group 12', 'group12Leaders');
insert into JAM_OWNER.WORKGROUP (WORKGROUP_ID, NAME, LEADER_ROLE_NAME) values (13, 'Group 13', 'group13Leaders');

-- Populate Facilities
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, RF_WORKGROUP_ID, BEAM_WORKGROUP_ID, WEIGHT) values (1, 'CEBAF', '/cebaf', 4, 5, 1);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, RF_WORKGROUP_ID, BEAM_WORKGROUP_ID, WEIGHT) values (2, 'LERF', '/lerf', 6, 7, 2);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, RF_WORKGROUP_ID, BEAM_WORKGROUP_ID, WEIGHT) values (3, 'UITF', '/uitf', 8, 9, 3);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, RF_WORKGROUP_ID, BEAM_WORKGROUP_ID, WEIGHT) values (4, 'CMTF', '/cmtf', 10, 11, 4);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, RF_WORKGROUP_ID, BEAM_WORKGROUP_ID, WEIGHT) values (5, 'VTA', '/vta', 12, 13, 5);

-- Populate Credited Controls
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Control 1','Control 1 Description',1,1,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Control 2','Control 2 Description',1,2,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Control 3','Control 3 Description',1,3,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Control 4','Control 4 Description',1,4,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Control 5','Control 5 Description',1,5,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,WORKGROUP_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Control 6','Control 6 Description',1,6,'1 Year');

-- Populate Beam Destinations
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 1', 1, 'uA', 'Y', 1);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 2', 1, 'uA', 'Y', 2);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 3', 1, 'uA', 'Y', 3);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 4', 2, 'uA', 'Y', 4);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 5', 2, 'uA', 'Y', 5);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 6', 2, 'uA', 'Y', 6);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 7', 3, 'uA', 'Y', 7);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 8', 3, 'uA', 'Y', 8);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Destination 9', 3, 'uA', 'Y', 9);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.destination_id.nextval, 'Injector RF Operations', 1, 'uA', 'Y', 9);

-- Populate Initial Control Verification
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 1, 1, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 2, 2, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 3, 3, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 4, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 5, 5, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 6, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 1, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 2, 5, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 3, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 4, 3, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 5, 2, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 6, 1, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 1, 9, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 2, 8, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 3, 7, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 4, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 5, 9, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 6, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
-- Test null VERIFIED_BY (Control 6, Destination 9)
insert into JAM_OWNER.control_verification (CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.control_verification_id.nextval, 6, 9, 100, sysdate, null, null, null, 'admin', sysdate);

-- Populate Initial Verification History (Control 1, Destination 1)
insert into JAM_OWNER.verification_history (VERIFICATION_HISTORY_ID, CONTROL_VERIFICATION_ID, VERIFICATION_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.verification_history_id.nextval, 1, 100, sysdate, 'tbrown', null, null, 'tbrown', sysdate);

-- Populate Initial Authorization
insert into JAM_OWNER.authorization (AUTHORIZATION_ID, MODIFIED_BY, MODIFIED_DATE, AUTHORIZATION_DATE, AUTHORIZED_BY, COMMENTS) values(JAM_OWNER.authorization_id.nextval, 'tbrown', sysdate, sysdate, 'tbrown', 'testing');

-- Populate Initial Destination Authorization
insert into JAM_OWNER.destination_authorization (BEAM_DESTINATION_ID, AUTHORIZATION_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(1, 1, 'None', null, 'test 1', null);
insert into JAM_OWNER.destination_authorization (BEAM_DESTINATION_ID, AUTHORIZATION_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(2, 1, 'Tune', null, 'test 2', null);
insert into JAM_OWNER.destination_authorization (BEAM_DESTINATION_ID, AUTHORIZATION_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(3, 1, 'CW', 10, 'test 3', null);

-- Populate Component
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2763, '0L03', 1);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2764, '0L04', 50);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2765, '1L02', 1);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2766, '1L03', 1);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2767, '1L04', 1);

-- Populate Component Verification
insert into JAM_OWNER.VERIFICATION_COMPONENT (CONTROL_VERIFICATION_ID, COMPONENT_ID) values (1, 2763);
insert into JAM_OWNER.VERIFICATION_COMPONENT (CONTROL_VERIFICATION_ID, COMPONENT_ID) values (1, 2764);
