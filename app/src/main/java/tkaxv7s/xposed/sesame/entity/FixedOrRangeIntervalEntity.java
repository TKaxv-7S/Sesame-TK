package tkaxv7s.xposed.sesame.entity;

import java.util.concurrent.ThreadLocalRandom;

public class FixedOrRangeIntervalEntity {

    private final Boolean fixedOrRange;

    private final Integer fixedInt;

    private final Integer rangeMin;

    private final Integer rangeMax;

    public FixedOrRangeIntervalEntity(String fixedOrRangeStr, int min, int max) {
        if (fixedOrRangeStr != null && !fixedOrRangeStr.isEmpty()) {
            String[] split = fixedOrRangeStr.split("-");
            if (split.length == 2) {
                int rangeMinTmp, rangeMaxTmp;
                try {
                    rangeMinTmp = Math.max(Integer.parseInt(split[0]), min);
                } catch (Exception ignored) {
                    rangeMinTmp = min;
                }
                try {
                    rangeMaxTmp = Math.min(Integer.parseInt(split[1]), max) + 1;
                } catch (Exception ignored) {
                    rangeMaxTmp = max;
                }
                if (rangeMinTmp >= rangeMaxTmp) {
                    rangeMaxTmp = rangeMinTmp + 1;
                }
                fixedInt = null;
                rangeMin = rangeMinTmp;
                rangeMax = rangeMaxTmp;
                fixedOrRange = false;
            } else {
                int fixedIntTmp;
                try {
                    fixedIntTmp = Math.max(Integer.parseInt(fixedOrRangeStr), min);
                } catch (Exception ignored) {
                    fixedIntTmp = min;
                }
                fixedInt = fixedIntTmp;
                rangeMin = null;
                rangeMax = null;
                fixedOrRange = true;
            }
        } else {
            fixedInt = min;
            rangeMin = null;
            rangeMax = null;
            fixedOrRange = true;
        }
    }

    public Integer getInterval() {
        if (fixedOrRange) {
            return fixedInt;
        }
        return ThreadLocalRandom.current().nextInt(rangeMin, rangeMax);
    }

}
