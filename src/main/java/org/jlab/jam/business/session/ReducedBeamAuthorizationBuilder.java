package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jam.persistence.entity.*;
import org.jlab.smoothness.business.util.IOUtil;

public class ReducedBeamAuthorizationBuilder {
  private BeamAuthorization createClone(BeamAuthorization auth) {
    BeamAuthorization authClone = auth.createAdminClone();

    List<BeamDestinationAuthorization> newList = new ArrayList<>();

    if (auth.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization operationAuth : auth.getDestinationAuthorizationList()) {
        BeamDestinationAuthorization operationClone = operationAuth.createAdminClone(authClone);
        newList.add(operationClone);
      }
    }

    authClone.setDestinationAuthorizationList(newList);

    return authClone;
  }

  private boolean populateReducedPermissionsDueToVerification(
      BeamAuthorization clone,
      Facility facility,
      List<BeamControlVerification> verificationList,
      Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    boolean atLeastOne = false;
    List<String> revokedDestinationList = new ArrayList<>();

    if (clone.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization operationAuth : clone.getDestinationAuthorizationList()) {

        if ("None".equals(operationAuth.getBeamMode())) {
          continue; // Already None so no need to revoke; move on to next
        }

        BigInteger destinationId = operationAuth.getDestination().getBeamDestinationId();

        for (BeamControlVerification verification : verificationList) {
          if (destinationId.equals(verification.getBeamDestination().getBeamDestinationId())) {
            operationAuth.setBeamMode("None");
            operationAuth.setCwLimit(null);
            operationAuth.setExpirationDate(null);
            operationAuth.setChanged(true);
            operationAuth.setComments(
                "Permission automatically revoked due to credited control "
                    + verification.getCreditedControl().getName()
                    + " verification "
                    + reason);
            atLeastOne = true;
            revokedDestinationList.add(operationAuth.getDestination().getName());
            break; // Found a match so revoke and then break out of loop
          }
        }
      }
    }

    if (atLeastOne) {
      String comments = ""; // We replace Change Notes completely with changes
      String csv = IOUtil.toCsv(revokedDestinationList.toArray());
      comments = comments + "Destination control verification revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
  }

  private boolean containsName(
      List<BeamDestinationAuthorization> destinationList,
      BeamDestinationAuthorization destination) {
    for (BeamDestinationAuthorization operationAuth : destinationList) {
      if (operationAuth.getDestination().getName().equals(destination.getDestination().getName())) {
        return true;
      }
    }
    return false;
  }

  private boolean populateReducedPermissionDueToAuthorizationExpiration(
      BeamAuthorization clone,
      Facility facility,
      List<BeamDestinationAuthorization> destinationList) {

    boolean atLeastOne = false;
    List<String> revokedDestinationList = new ArrayList<>();

    if (clone.getDestinationAuthorizationList() != null) {
      for (BeamDestinationAuthorization operationAuth : clone.getDestinationAuthorizationList()) {

        if ("None".equals(operationAuth.getBeamMode())) {
          continue; // Already None so no need to revoke; move on to next
        }
        if (containsName(destinationList, operationAuth)) {
          operationAuth.setBeamMode("None");
          operationAuth.setCwLimit(null);
          operationAuth.setExpirationDate(null);
          operationAuth.setChanged(true);
          operationAuth.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          atLeastOne = true;
          revokedDestinationList.add(operationAuth.getDestination().getName());
        }
      }
    }

    if (atLeastOne) {
      String comments = ""; // We replace Change Notes completely with changes
      String csv = IOUtil.toCsv(revokedDestinationList.toArray());
      comments = comments + "Destination authorization revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
  }

  public BeamAuthorization build(
      BeamAuthorization previous,
      Facility facility,
      List<BeamDestinationAuthorization> expiredAuthorizationList,
      List<BeamControlVerification> expiredVerificationList,
      boolean isExpirationEvent) {
    BeamAuthorization authReduction = createClone(previous);
    boolean authorizerReduction = false;
    boolean verifierReduction = false;

    if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {

      authorizerReduction =
          populateReducedPermissionDueToAuthorizationExpiration(
              authReduction, facility, expiredAuthorizationList);
    }

    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      verifierReduction =
          populateReducedPermissionsDueToVerification(
              authReduction, facility, expiredVerificationList, isExpirationEvent);
    }

    if (!authorizerReduction && !verifierReduction) {
      authReduction = null;
    }
    return authReduction;
  }
}
