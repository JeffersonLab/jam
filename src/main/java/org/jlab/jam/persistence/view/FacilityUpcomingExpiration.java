package org.jlab.jam.persistence.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jlab.jam.persistence.entity.*;

public class FacilityUpcomingExpiration {
  private Facility facility;
  private List<BeamDestinationAuthorization> upcomingBeamAuthorizationExpirationList;
  private List<BeamControlVerification> upcomingBeamVerificationExpirationList;
  private List<RFSegmentAuthorization> upcomingRFAuthorizationExpirationList;
  private List<RFControlVerification> upcomingRFVerificationExpirationList;

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public List<BeamDestinationAuthorization> getUpcomingBeamAuthorizationExpirationList() {
    return upcomingBeamAuthorizationExpirationList;
  }

  public void setUpcomingBeamAuthorizationExpirationList(
      List<BeamDestinationAuthorization> upcomingBeamAuthorizationExpirationList) {
    this.upcomingBeamAuthorizationExpirationList = upcomingBeamAuthorizationExpirationList;
  }

  public List<BeamControlVerification> getUpcomingBeamVerificationExpirationList() {
    return upcomingBeamVerificationExpirationList;
  }

  public void setUpcomingBeamVerificationExpirationList(
      List<BeamControlVerification> upcomingBeamVerificationExpirationList) {
    this.upcomingBeamVerificationExpirationList = upcomingBeamVerificationExpirationList;
  }

  public List<RFSegmentAuthorization> getUpcomingRFAuthorizationExpirationList() {
    return upcomingRFAuthorizationExpirationList;
  }

  public void setUpcomingRFAuthorizationExpirationList(
      List<RFSegmentAuthorization> upcomingRFAuthorizationExpirationList) {
    this.upcomingRFAuthorizationExpirationList = upcomingRFAuthorizationExpirationList;
  }

  public List<RFControlVerification> getUpcomingRFVerificationExpirationList() {
    return upcomingRFVerificationExpirationList;
  }

  public void setUpcomingRFVerificationExpirationList(
      List<RFControlVerification> upcomingRFVerificationExpirationList) {
    this.upcomingRFVerificationExpirationList = upcomingRFVerificationExpirationList;
  }

  // Anything within 7 days of expiration is considered upcoming, but
  // Only things between day 6 and 7 are considered in the notification window
  // As to avoid spamming users daily (there is a once a day scheduled notification).
  public FacilityUpcomingExpiration inNotificationBoundary() {
    FacilityUpcomingExpiration facilityUpcomingExpiration = new FacilityUpcomingExpiration();

    List<RFSegmentAuthorization> upcomingRFAuthExpirations = new ArrayList<>();
    List<BeamDestinationAuthorization> upcomingBeamAuthExpirations = new ArrayList<>();
    List<RFControlVerification> upcomingRFVerificationExpirations = new ArrayList<>();
    List<BeamControlVerification> upcomingBeamVerificationExpirations = new ArrayList<>();

    facilityUpcomingExpiration.setUpcomingRFAuthorizationExpirationList(upcomingRFAuthExpirations);
    facilityUpcomingExpiration.setUpcomingBeamAuthorizationExpirationList(
        upcomingBeamAuthExpirations);
    facilityUpcomingExpiration.setUpcomingRFVerificationExpirationList(
        upcomingRFVerificationExpirations);
    facilityUpcomingExpiration.setUpcomingBeamVerificationExpirationList(
        upcomingBeamVerificationExpirations);

    Date now = new Date();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 7);
    Date sevenDaysFromNow = cal.getTime();
    cal.add(Calendar.DATE, -1);
    Date sixDaysFromNow = cal.getTime();

    if (upcomingRFAuthorizationExpirationList != null) {
      for (RFSegmentAuthorization operation : upcomingRFAuthorizationExpirationList) {
        if (operation.isHighPowerRf()
            && operation.getExpirationDate() != null
            && (sixDaysFromNow.before(operation.getExpirationDate())
                || sixDaysFromNow.equals(operation.getExpirationDate()))
            && sevenDaysFromNow.after(operation.getExpirationDate())) {
          upcomingRFAuthExpirations.add(operation);

          System.err.println(
              "[NotificationBoundary] Found upcoming RF operations authorization expiration: "
                  + operation.getSegment().getName());
        }
      }
    }

    if (upcomingBeamAuthorizationExpirationList != null) {
      for (BeamDestinationAuthorization operation : upcomingBeamAuthorizationExpirationList) {
        if (!"None".equals(operation.getBeamMode())
            && operation.getExpirationDate() != null
            && (sixDaysFromNow.before(operation.getExpirationDate())
                || sixDaysFromNow.equals(operation.getExpirationDate()))
            && sevenDaysFromNow.after(operation.getExpirationDate())) {
          upcomingBeamAuthExpirations.add(operation);

          System.err.println(
              "[NotificationBoundary] Found upcoming Beam operations authorization expiration: "
                  + operation.getDestination().getName());
        }
      }
    }

    if (upcomingRFVerificationExpirationList != null) {
      for (RFControlVerification verification : upcomingRFVerificationExpirationList) {
        if (verification.getVerificationStatusId() == 1
            && verification.getExpirationDate() != null
            && (sixDaysFromNow.before(verification.getExpirationDate())
                || sixDaysFromNow.equals(verification.getExpirationDate()))
            && sevenDaysFromNow.after(verification.getExpirationDate())) {
          upcomingRFVerificationExpirations.add(verification);

          System.err.println(
              "[NotificationBoundary] Found upcoming RF verification expiration: "
                  + verification.getRFSegment().getName());
        }
      }
    }

    if (upcomingBeamVerificationExpirationList != null) {
      for (BeamControlVerification verification : upcomingBeamVerificationExpirationList) {
        if (verification.getVerificationStatusId() == 1
            && verification.getExpirationDate() != null
            && (sixDaysFromNow.before(verification.getExpirationDate())
                || sixDaysFromNow.equals(verification.getExpirationDate()))
            && sevenDaysFromNow.after(verification.getExpirationDate())) {
          upcomingBeamVerificationExpirations.add(verification);

          System.err.println(
              "[NotificationBoundary] Found upcoming Beam verification expiration: "
                  + verification.getBeamDestination().getName());
        }
      }
    }

    return facilityUpcomingExpiration;
  }
}
