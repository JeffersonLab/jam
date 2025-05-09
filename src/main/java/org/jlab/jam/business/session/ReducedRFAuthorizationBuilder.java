package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jam.persistence.entity.*;
import org.jlab.smoothness.business.util.IOUtil;

public class ReducedRFAuthorizationBuilder {

  private RFAuthorization createClone(RFAuthorization auth) {
    RFAuthorization authClone = auth.createAdminClone();

    List<RFSegmentAuthorization> newList = new ArrayList<>();

    if (auth.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : auth.getRFSegmentAuthorizationList()) {
        RFSegmentAuthorization operationClone = operationAuth.createAdminClone(authClone);
        newList.add(operationClone);
      }
    }

    authClone.setRFSegmentAuthorizationList(newList);

    return authClone;
  }

  private boolean populateReducedPermissionsDueToVerification(
      RFAuthorization clone,
      Facility facility,
      List<RFControlVerification> verificationList,
      Boolean expiration) {
    String reason = "expiration";

    if (!expiration) {
      reason = "downgrade";
    }

    boolean atLeastOne = false;
    List<String> revokedSegmentList = new ArrayList<>();

    if (clone.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : clone.getRFSegmentAuthorizationList()) {

        if (!operationAuth.isHighPowerRf()) {
          continue; // Already No High RF Auth so no need to revoke; move on to next
        }

        BigInteger segmentId = operationAuth.getSegment().getRFSegmentId();

        for (RFControlVerification verification : verificationList) {
          if (segmentId.equals(verification.getRFSegment().getRFSegmentId())) {
            operationAuth.setHighPowerRf(false);
            operationAuth.setExpirationDate(null);
            operationAuth.setChanged(true);
            operationAuth.setComments(
                "Permission automatically revoked due to credited control "
                    + verification.getCreditedControl().getName()
                    + " verification "
                    + reason);
            atLeastOne = true;
            revokedSegmentList.add(operationAuth.getSegment().getName());
            break; // Found a match so revoke and then break out of loop
          }
        }
      }
    }

    if (atLeastOne) {
      String comments = ""; // We replace Change Notes completely with changes
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "Segment control verification revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
  }

  private boolean containsName(
      List<RFSegmentAuthorization> segmentList, RFSegmentAuthorization segment) {
    for (RFSegmentAuthorization operationAuth : segmentList) {
      if (operationAuth.getSegment().getName().equals(segment.getSegment().getName())) {
        return true;
      }
    }
    return false;
  }

  private boolean populateReducedPermissionDueToAuthorizationExpiration(
      RFAuthorization clone, Facility facility, List<RFSegmentAuthorization> segmentList) {

    boolean atLeastOne = false;
    List<String> revokedSegmentList = new ArrayList<>();

    if (clone.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : clone.getRFSegmentAuthorizationList()) {

        if (!operationAuth.isHighPowerRf()) {
          continue; // Already No High Power RF auth so no need to revoke; move on to next
        }

        if (containsName(segmentList, operationAuth)) {
          operationAuth.setHighPowerRf(false);
          operationAuth.setExpirationDate(null);
          operationAuth.setChanged(true);
          operationAuth.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          atLeastOne = true;
          revokedSegmentList.add(operationAuth.getSegment().getName());
        }
      }
    }

    if (atLeastOne) {
      String comments = ""; // We replace Change Notes completely with changes
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "Segment authorization revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
  }

  public RFAuthorization build(
      RFAuthorization previous,
      Facility facility,
      List<RFSegmentAuthorization> expiredAuthorizationList,
      List<RFControlVerification> expiredVerificationList,
      boolean isExpirationEvent) {
    RFAuthorization authReduction = createClone(previous);
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
