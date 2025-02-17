package org.jlab.jam.presentation.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author ryans
 */
public final class WebFunctions {

  private static final Logger LOGGER = Logger.getLogger(WebFunctions.class.getName());

  private static final List<String> CEBAF_LIST = Arrays.asList("None", "Tune", "CW");
  private static final List<String> LERF_LIST =
      Arrays.asList(
          "None", "Ceramic Viewer", "Viewer Limited", "High Duty Cycle", "BLM Checkout", "CW");
  private static final List<String> UITF_LIST =
      Arrays.asList("None", "Viewer Limited", "Tune", "CW");

  private WebFunctions() {
    // cannot instantiate publicly
  }

  public static List<String> beamModeList(String facility, String destination) {
    List<String> modes = null;

    switch (facility) {
      case "CEBAF":
        modes = CEBAF_LIST;
        break;
      case "LERF":
        modes = LERF_LIST;
        break;
      case "UITF":
        modes = UITF_LIST;
        break;
    }

    return modes;
  }

  public static List<String> laseModeList() {
    return Arrays.asList("None", "UV", "IR");
  }

  public static Date now() {
    return new Date();
  }

  public static Date sevenDaysFromNow() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 7);
    return cal.getTime();
  }

  public static boolean isExpiringSoon(Date expiration) {
    boolean expiringSoon = false;

    if (expiration != null) {
      if (expiration.after(now())) {
        if (expiration.before(sevenDaysFromNow())) {
          expiringSoon = true;
        }
      }
    }

    return expiringSoon;
  }
}
