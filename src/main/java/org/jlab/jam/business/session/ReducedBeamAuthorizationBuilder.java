package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jam.persistence.entity.Facility;
import org.jlab.jam.persistence.entity.RFAuthorization;
import org.jlab.jam.persistence.entity.RFControlVerification;
import org.jlab.jam.persistence.entity.RFSegmentAuthorization;
import org.jlab.smoothness.business.util.IOUtil;

public class ReducedBeamAuthorizationBuilder {

  private RFAuthorization createClone(RFAuthorization auth) {
    RFAuthorization authClone = auth.createAdminClone();

    System.err.println("Created auth clone:" + authClone);

    List<RFSegmentAuthorization> newList = new ArrayList<>();

    if (auth.getRFSegmentAuthorizationList() != null) {
      for (RFSegmentAuthorization operationAuth : auth.getRFSegmentAuthorizationList()) {
        RFSegmentAuthorization operationClone = operationAuth.createAdminClone(authClone);
        operationClone.transientOperationId = operationAuth.getSegment().getRFSegmentId();
        newList.add(operationClone);
        System.err.println("Created Segment PK:" + operationClone.getSegmentAuthorizationPK());
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
      System.err.println("Looping over operations");
      for (RFSegmentAuthorization operationAuth : clone.getRFSegmentAuthorizationList()) {

        System.err.println("operationAuth = " + operationAuth.getSegment().getName());

        if (!operationAuth.isHighPowerRf()) {
          continue; // Already No High RF Auth so no need to revoke; move on to next
        }

        BigInteger segmentId = operationAuth.transientOperationId;

        System.err.println(
            "This one has RF ON so let's continue!  We're looking for segmentId=" + segmentId);

        for (RFControlVerification verification : verificationList) {
          System.err.println(
              "Does it match known expiration? " + verification.getRFSegment().getRFSegmentId());
          if (segmentId.equals(verification.getRFSegment().getRFSegmentId())) {
            System.err.println("Found RF Segment with expiration!");
            operationAuth.setHighPowerRf(false);
            operationAuth.setExpirationDate(null);
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
      String comments = clone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "\nCHANGE: Segment control verification revoked: " + csv;
      clone.setComments(comments);
    }

    return atLeastOne;
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
        if (segmentList.contains(operationAuth)) {
          operationAuth.setHighPowerRf(false);
          operationAuth.setExpirationDate(null);
          operationAuth.setComments(
              "Permission automatically revoked due to director's authorization expiration");
          atLeastOne = true;
          revokedSegmentList.add(operationAuth.getSegment().getName());
        }
      }
    }

    if (atLeastOne) {
      String comments = clone.getComments();
      if (comments == null) {
        comments = "";
      }
      String csv = IOUtil.toCsv(revokedSegmentList.toArray());
      comments = comments + "\nCHANGE: Segment authorization revoked: " + csv;
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

    System.err.println("Made it to B");

    if (expiredAuthorizationList != null && !expiredAuthorizationList.isEmpty()) {

      authorizerReduction =
          populateReducedPermissionDueToAuthorizationExpiration(
              authReduction, facility, expiredAuthorizationList);

      System.err.println("Made it to C1");
    }

    System.err.println("Made it to C2");

    if (expiredVerificationList != null && !expiredVerificationList.isEmpty()) {
      System.err.println("Attempting to populate with verification reduction");
      verifierReduction =
          populateReducedPermissionsDueToVerification(
              authReduction, facility, expiredVerificationList, isExpirationEvent);

      System.err.println("Made it to D1");
    }

    System.err.println("Made it to D2");

    if (!authorizerReduction && !verifierReduction) {
      System.err.println(
          "No modifications were made!: authorizer: "
              + authorizerReduction
              + ", verifier: "
              + verifierReduction);
      authReduction = null;
    }
    return authReduction;
  }
}
