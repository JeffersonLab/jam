alter session set container = XEPDB1;

-- Populate Verification Statuses
insert into JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID, NAME) values (1, 'Verified');
insert into JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID, NAME) values (50, 'Provisionally Verified');
insert into JAM_OWNER.VERIFICATION_STATUS (VERIFICATION_STATUS_ID, NAME) values (100, 'Not Verified');

-- Populate Verification Teams
insert into JAM_OWNER.VERIFICATION_TEAM (VERIFICATION_TEAM_ID, NAME, DIRECTORY_ROLE_NAME) values (1, 'Group 1', 'group1Leaders');
insert into JAM_OWNER.VERIFICATION_TEAM (VERIFICATION_TEAM_ID, NAME, DIRECTORY_ROLE_NAME) values (2, 'Group 2', 'group2Leaders');
insert into JAM_OWNER.VERIFICATION_TEAM (VERIFICATION_TEAM_ID, NAME, DIRECTORY_ROLE_NAME) values (3, 'Group 3', 'group3Leaders');
insert into JAM_OWNER.VERIFICATION_TEAM (VERIFICATION_TEAM_ID, NAME, DIRECTORY_ROLE_NAME) values (4, 'Group 4', 'group4Leaders');

-- Populate Facilities
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, WEIGHT) values (1, 'CEBAF', '/cebaf',  1);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, WEIGHT) values (2, 'LERF', '/lerf', 2);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, WEIGHT) values (3, 'UITF', '/uitf', 3);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, WEIGHT) values (4, 'CMTF', '/cmtf', 4);
insert into JAM_OWNER.FACILITY (FACILITY_ID, NAME, PATH, WEIGHT) values (5, 'VTA', '/vta', 5);

-- Populate Credited Controls
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Earth Berms/Overburden','Non-structural fill, cover, or berms',1,1,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Structural Shielding','Beam enclosure concrete structure',2,2,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Movable Shielding','Includes penetrations',1,3,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Beam Dump Cooling Building Design','Structural integrity of the buildings and their sump pits',1,4,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Nitrogen Gas Supply Orifices','1/8" orifice plates to restrict the amount of nitrogen that could be introduced into the tunnel enclosures',1,5,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'ODH vents, lintels and facility configuration','Passive ceiling vents, door configuration, and lintels for slowing helium migration into stairwells',1,6,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'PSS Access Controls','CEBAF and LERF controls for keeping people out of segments. Includes tunnel door maglock, keyswitches, sweep procedures, klaxons, beacons',1,7,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'CEBAF PSS Beam Containment Controls','CEBAF controls for keeping beam inside segments (protect adjacent segments). Critical devices including beam stops, beam segment steering elecromagnets, and interlocks such as magnet power supply/RF waveguide pressure interfaces',1,8,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'ODH System Controls','Oxygen sensors and alarms',1,9,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'Doors, Gates, Fences, and other Barriers','Non-interlocked administrative controls for personnel safety',1,10,'1 Year');
insert into JAM_OWNER.CREDITED_CONTROL (CREDITED_CONTROL_ID,NAME,DESCRIPTION,VERIFICATION_TEAM_ID,WEIGHT,VERIFICATION_FREQUENCY) values (JAM_OWNER.CREDITED_CONTROL_ID.nextval,'UITF Nitrogen Gas Supply Orifices','1/8" orifice plates to restrict the amount of nitrogen that could be introduced into the UITF enclosure',1,11,'1 Year');


-- Populate RF Segments
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'Injector', 1, 'Y', 1);
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'North Linac', 1, 'Y', 2);
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'South Linac', 1, 'Y', 3);
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'Entire Facility', 2, 'Y', 4);
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'Entire Facility', 3, 'Y', 5);
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'Entire Facility', 4, 'Y', 6);
insert into JAM_OWNER.rf_segment (RF_SEGMENT_ID, NAME, FACILITY_ID, ACTIVE_YN, WEIGHT) values(JAM_OWNER.rf_segment_id.nextval, 'Entire Facility', 5, 'Y', 7);

-- Populate Beam Destinations
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, '1D Spectrometer (IDL1D00)', 1, 'uA', 'Y', 1);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'Faraday Cup #2 (IFY0L03)', 1, 'uA', 'Y', 2);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'Inline Dump (SDL0R08)', 1, 'uA', 'Y', 3);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'BSY', 1, 'uA', 'Y', 4);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'Hall A', 1, 'uA', 'Y', 5);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'Hall B', 1, 'uA', 'Y', 6);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'Hall C', 1, 'uA', 'Y', 7);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'Hall D Tagger Dump (IBDAD00)', 1, 'uA', 'Y', 8);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, '0G Tune/Moeller Dump', 2, 'uA', 'Y', 9);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, '2G 100MeV SA Dump (IDC2G00)', 2, 'uA', 'Y', 10);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, '1G Dump (IDC1G03)', 2, 'uA', 'Y', 11);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, '1X Dump', 2, 'uA', 'Y', 12);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'KeV Dumps', 3, 'uA', 'Y', 13);
insert into JAM_OWNER.beam_destination (BEAM_DESTINATION_ID, NAME, FACILITY_ID, CURRENT_LIMIT_UNITS, ACTIVE_YN, WEIGHT) values(JAM_OWNER.beam_destination_id.nextval, 'MeV Dumps', 3, 'uA', 'Y', 14);


-- Populate Initial Beam Control Verification
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 1, 1, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 2, 2, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 3, 3, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 4, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 5, 5, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 6, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 1, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 2, 5, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 3, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 4, 3, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 5, 2, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 6, 1, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 1, 9, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 2, 8, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 3, 7, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 4, 6, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 5, 9, 100, sysdate, 'admin', null, null, 'admin', sysdate);
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 6, 4, 100, sysdate, 'admin', null, null, 'admin', sysdate);
-- Test null VERIFIED_BY (Control 6, Destination 9)
insert into JAM_OWNER.beam_control_verification (BEAM_CONTROL_VERIFICATION_ID, CREDITED_CONTROL_ID, BEAM_DESTINATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_id.nextval, 6, 9, 100, sysdate, null, null, null, 'admin', sysdate);

-- Populate Initial Verification History (Control 1, Destination 1)
insert into JAM_OWNER.beam_control_verification_history (BEAM_CONTROL_VERIFICATION_HISTORY_ID, BEAM_CONTROL_VERIFICATION_ID, VERIFICATION_STATUS_ID, VERIFICATION_DATE, VERIFIED_BY, EXPIRATION_DATE, COMMENTS, MODIFIED_BY, MODIFIED_DATE) values(JAM_OWNER.beam_control_verification_history_id.nextval, 1, 100, sysdate, 'tbrown', null, null, 'tbrown', sysdate);

-- Populate Initial Beam Authorization
insert into JAM_OWNER.BEAM_AUTHORIZATION (BEAM_AUTHORIZATION_ID, FACILITY_ID, MODIFIED_BY, MODIFIED_DATE, AUTHORIZATION_DATE, AUTHORIZED_BY, COMMENTS) values(JAM_OWNER.beam_authorization_id.nextval, 1, 'tbrown', sysdate, sysdate, 'tbrown', 'testing');

-- Populate Initial Destination Authorization
insert into JAM_OWNER.beam_destination_authorization (BEAM_DESTINATION_ID, BEAM_AUTHORIZATION_ID, FACILITY_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(1, 1, 1, 'None', null, 'test 1', null);
insert into JAM_OWNER.beam_destination_authorization (BEAM_DESTINATION_ID, BEAM_AUTHORIZATION_ID, FACILITY_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(2, 1, 1, 'Tune', null, 'test 2', null);
insert into JAM_OWNER.beam_destination_authorization (BEAM_DESTINATION_ID, BEAM_AUTHORIZATION_ID, FACILITY_ID, BEAM_MODE, CW_LIMIT, COMMENTS, EXPIRATION_DATE) values(3, 1, 1, 'CW', 10, 'test 3', null);

-- Populate Component
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2763, '0L03', 1);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2764, '0L04', 50);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2765, '1L02', 1);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2766, '1L03', 1);
insert into JAM_OWNER.COMPONENT (COMPONENT_ID, NAME, STATUS_ID) values (2767, '1L04', 1);

-- Populate Component Verification
insert into JAM_OWNER.BEAM_CONTROL_VERIFICATION_COMPONENT (BEAM_CONTROL_VERIFICATION_ID, COMPONENT_ID) values (1, 2763);
insert into JAM_OWNER.BEAM_CONTROL_VERIFICATION_COMPONENT (BEAM_CONTROL_VERIFICATION_ID, COMPONENT_ID) values (1, 2764);
