package org.jlab.jam.presentation.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author ryans
 */
public final class BeamAuthFunctions {

  private static final Logger LOGGER = Logger.getLogger(BeamAuthFunctions.class.getName());

  private static final List<String> RF_LIST = Arrays.asList("None", "RF Only");
  private static final List<String> CEBAF_LIST = Arrays.asList("None", "Tune", "CW");
  private static final List<String> LERF_LIST =
      Arrays.asList(
          "None", "Ceramic Viewer", "Viewer Limited", "High Duty Cycle", "BLM Checkout", "CW");
  private static final List<String> UITF_LIST =
      Arrays.asList("None", "Viewer Limited", "Tune", "CW");

  private BeamAuthFunctions() {
    // cannot instantiate publicly
  }

  public static List<String> beamModeList(String facility, String destination) {
    List<String> modes = null;

    if (destination.contains("RF Operations")) {
      modes = RF_LIST;
    } else {
      switch (facility) {
        case "cebaf":
          modes = CEBAF_LIST;
          break;
        case "lerf":
          modes = LERF_LIST;
          break;
        case "uitf":
          modes = UITF_LIST;
          break;
      }
    }

    return modes;
  }

  public static List<String> laseModeList() {
    return Arrays.asList("None", "UV", "IR");
  }

  public static Date now() {
    return new Date();
  }

  public static Date twoDaysFromNow() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 2);
    return cal.getTime();
  }
}
