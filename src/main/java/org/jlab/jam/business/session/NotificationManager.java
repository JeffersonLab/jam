package org.jlab.jam.business.session;

import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.jlab.jam.persistence.entity.*;
import org.jlab.jam.persistence.enumeration.OperationsType;
import org.jlab.jam.persistence.view.FacilityExpirationEvent;

@Stateless
public class NotificationManager {
  @EJB LogbookFacade logbookFacade;
  @EJB EmailFacade emailFacade;

  public void notifyExpirationAndUpcoming(Map<Facility, FacilityExpirationEvent> facilityMap) {}

  public void notifyFacilityExpiration(FacilityExpirationEvent event) {
    if (event != null) {
      logbookFacade.sendAsyncAuthorizationLogEntries(event);
      emailFacade.sendAsyncExpirationEmails(event);
    }
  }

  public void notifyRFVerificationDowngrade(
      Facility facility, List<RFControlVerification> verificationList, RFAuthorization auth) {
    logbookFacade.sendAsyncAuthorizationLogEntry(
        facility, OperationsType.RF, auth.getRfAuthorizationId());

    emailFacade.sendAsyncRFVerifierDowngradeEmail(facility, verificationList);
  }

  public void notifyBeamVerificationDowngrade(
      Facility facility, List<BeamControlVerification> verificationList, BeamAuthorization auth) {
    logbookFacade.sendAsyncAuthorizationLogEntry(
        facility, OperationsType.BEAM, auth.getBeamAuthorizationId());

    emailFacade.sendAsyncBeamVerifierDowngradeEmail(facility, verificationList);
  }
}
