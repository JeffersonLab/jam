package org.jlab.jam.business.util;

import java.util.Date;

public final class EqualityHelper {
  private EqualityHelper() {
    // Can't instantiate because private
  }

  public static boolean nullableObjEqual(Object one, Object two) {
    boolean equal = false;

    if (one == null) {
      if (two == null) {
        equal = true;
      }
    } else {
      equal = one.equals(two);
    }

    return equal;
  }

  public static boolean nullableStringEqual(String one, String two) {
    // String can be null, or contain just whitespace and we treat them as equal

    boolean equal = false;

    if (one != null && one.isBlank()) {
      one = null;
    }

    if (two != null && two.isBlank()) {
      two = null;
    }

    if (one == null) {
      if (two == null) {
        equal = true;
      }
    } else {
      equal = one.equals(two);
    }

    return equal;
  }

  public static boolean nullableDateEqual(Date one, Date two) {
    // Dates can be of type sql.Date vs util.Date and we treat them as equal

    boolean equal = false;

    Long oneUnix = null;
    Long twoUnix = null;

    if (one != null) {
      oneUnix = one.getTime();
    }

    if (two != null) {
      twoUnix = two.getTime();
    }

    if (one == null) {
      if (two == null) {
        equal = true;
      }
    } else {
      equal = oneUnix.equals(twoUnix);
    }

    return equal;
  }
}
