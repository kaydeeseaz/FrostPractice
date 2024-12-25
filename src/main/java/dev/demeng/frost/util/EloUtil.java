package dev.demeng.frost.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EloUtil {

  private static final String FILEHASH = "%%__FILEHASH__%%";

  private final KFactor[] K_FACTORS = {
      new KFactor(0, 1000, 25),
      new KFactor(1001, 1400, 20),
      new KFactor(1401, 1800, 15),
      new KFactor(1801, 2200, 10)
  };

  private final int DEFAULT_K_FACTOR = 25;
  private final int WIN = 1;
  private final int LOSS = 0;

  public int getNewRating(int rating, int opponentRating, boolean won) {
    if (won) {
      return EloUtil.getNewRating(rating, opponentRating, EloUtil.WIN);
    } else {
      return EloUtil.getNewRating(rating, opponentRating, EloUtil.LOSS);
    }
  }

  public int getNewRating(int rating, int opponentRating, int score) {
    double kFactor = EloUtil.getKFactor(rating);
    double expectedScore = EloUtil.getExpectedScore(rating, opponentRating);
    int newRating = EloUtil.calculateNewRating(rating, score, expectedScore, kFactor);

    if (score == 1 && newRating == rating) {
      newRating++;
    }

    return newRating;
  }

  private int calculateNewRating(int oldRating, int score, double expectedScore, double kFactor) {
    return oldRating + (int) (kFactor * (score - expectedScore));
  }

  private double getKFactor(int rating) {
    for (int i = 0; i < EloUtil.K_FACTORS.length; i++) {
      if (rating >= EloUtil.K_FACTORS[i].getStartIndex()
          && rating <= EloUtil.K_FACTORS[i].getEndIndex()) {
        return EloUtil.K_FACTORS[i].getValue();
      }
    }

    return EloUtil.DEFAULT_K_FACTOR;
  }

  private double getExpectedScore(int rating, int opponentRating) {
    return 1 / (1 + Math.pow(10, ((double) (opponentRating - rating) / 400)));
  }
}
