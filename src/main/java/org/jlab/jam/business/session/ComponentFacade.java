package org.jlab.jam.business.session;

import java.math.BigInteger;
import java.util.*;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import org.jlab.jam.persistence.entity.Component;
import org.jlab.jam.persistence.entity.ControlVerification;
import org.jlab.smoothness.business.exception.UserFriendlyException;

/**
 * @author ryans
 */
@Stateless
public class ComponentFacade extends AbstractFacade<Component> {
  @PersistenceContext(unitName = "jamPU")
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  @EJB ControlVerificationFacade controlVerificationFacade;

  public ComponentFacade() {
    super(Component.class);
  }

  @RolesAllowed("jam-admin")
  public void removeComponent(BigInteger verificationId, BigInteger componentId)
      throws UserFriendlyException {
    if (verificationId == null) {
      throw new UserFriendlyException("verificationId is required");
    }

    if (componentId == null) {
      throw new UserFriendlyException("componentId is required");
    }

    ControlVerification verification = controlVerificationFacade.find(verificationId);

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
      controlVerificationFacade.edit(verification);
    }
  }
}
