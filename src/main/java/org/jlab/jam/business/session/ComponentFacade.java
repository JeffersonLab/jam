package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.*;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jlab.jam.persistence.entity.BeamControlVerification;
import org.jlab.jam.persistence.entity.Component;
import org.jlab.jam.persistence.entity.RFControlVerification;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class ComponentFacade extends AbstractFacade<Component> {
  @PersistenceContext(unitName = "webappPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @EJB RFControlVerificationFacade rfControlVerificationFacade;
  @EJB BeamControlVerificationFacade beamControlVerificationFacade;

  public ComponentFacade() {
    super(Component.class);
  }

  @RolesAllowed("jam-admin")
  public void removeRFComponent(BigInteger verificationId, BigInteger componentId)
      throws UserFriendlyException {
    if (verificationId == null) {
      throw new UserFriendlyException("verificationId is required");
    }

    if (componentId == null) {
      throw new UserFriendlyException("componentId is required");
    }

    RFControlVerification verification = rfControlVerificationFacade.find(verificationId);

    if (verification == null) {
      throw new UserFriendlyException("verification with ID " + verificationId + " not found");
    }

    List<Component> componentList = verification.getComponentList();

    if (componentList != null) {
      List<Component> newList = new ArrayList<>();
      for (Component component : componentList) {
        if (!componentId.equals(component.getComponentId())) {
          newList.add(component);
        }
      }
      verification.setComponentList(newList);
      rfControlVerificationFacade.edit(verification);
    }
  }

  @RolesAllowed("jam-admin")
  public void removeBeamComponent(BigInteger verificationId, BigInteger componentId)
      throws UserFriendlyException {
    if (verificationId == null) {
      throw new UserFriendlyException("verificationId is required");
    }

    if (componentId == null) {
      throw new UserFriendlyException("componentId is required");
    }

    BeamControlVerification verification = beamControlVerificationFacade.find(verificationId);

    if (verification == null) {
      throw new UserFriendlyException("verification with ID " + verificationId + " not found");
    }

    List<Component> componentList = verification.getComponentList();

    if (componentList != null) {
      List<Component> newList = new ArrayList<>();
      for (Component component : componentList) {
        if (!componentId.equals(component.getComponentId())) {
          newList.add(component);
        }
      }
      verification.setComponentList(newList);
      beamControlVerificationFacade.edit(verification);
    }
  }

  @RolesAllowed("jam-admin")
  public void addBeamComponent(BigInteger verificationId, BigInteger componentId)
      throws UserFriendlyException {
    if (verificationId == null) {
      throw new UserFriendlyException("verificationId is required");
    }

    if (componentId == null) {
      throw new UserFriendlyException("componentId is required");
    }

    BeamControlVerification verification = beamControlVerificationFacade.find(verificationId);

    if (verification == null) {
      throw new UserFriendlyException("verification with ID " + verificationId + " not found");
    }

    Component component = find(componentId);

    if (component == null) {
      throw new UserFriendlyException("component with ID " + componentId + " not found");
    }

    List<Component> componentList = verification.getComponentList();

    if (componentList == null) {
      componentList = new ArrayList<>();
    }

    componentList.add(component);

    beamControlVerificationFacade.edit(verification);
  }

  @RolesAllowed("jam-admin")
  public void addRFComponent(BigInteger verificationId, BigInteger componentId)
      throws UserFriendlyException {
    if (verificationId == null) {
      throw new UserFriendlyException("verificationId is required");
    }

    if (componentId == null) {
      throw new UserFriendlyException("componentId is required");
    }

    RFControlVerification verification = rfControlVerificationFacade.find(verificationId);

    if (verification == null) {
      throw new UserFriendlyException("verification with ID " + verificationId + " not found");
    }

    Component component = find(componentId);

    if (component == null) {
      throw new UserFriendlyException("component with ID " + componentId + " not found");
    }

    List<Component> componentList = verification.getComponentList();

    if (componentList == null) {
      componentList = new ArrayList<>();
    }

    componentList.add(component);

    rfControlVerificationFacade.edit(verification);
  }
}
