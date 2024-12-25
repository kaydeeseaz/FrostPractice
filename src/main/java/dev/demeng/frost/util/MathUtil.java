package dev.demeng.frost.util;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {

  public static boolean isInteger(String in) {
    try {
      Integer.parseInt(in);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static int randomNumber(int minimum, int maximum) {
    return ThreadLocalRandom.current().nextInt(maximum + 1) + minimum;
  }

  public static String convertTicksToMinutes(int ticks) {
    long minute = (long) ticks / 1200L;
    long second = (long) ticks / 20L - (minute * 60L);

    String secondString = String.valueOf(Math.round(second));
    if (second < 10) {
      secondString = 0 + secondString;
    }

    String minuteString = String.valueOf(Math.round(minute));
    if (minute == 0) {
      minuteString = 0 + "";
    }

    return minuteString + ":" + secondString;
  }

  public static String convertToRomanNumeral(int number) {
    switch (number) {
      case 1:
        return "I";
      case 2:
        return "II";
    }

    return null;
  }

  public static double roundToHalves(double d) {
    return Math.round(d * 2.0D) / 2.0D;
  }
}
