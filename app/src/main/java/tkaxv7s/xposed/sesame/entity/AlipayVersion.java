package tkaxv7s.xposed.sesame.entity;

import lombok.Getter;

@Getter
public class AlipayVersion implements Comparable<AlipayVersion> {

    private final String versionString;

    private final Integer[] versionArray;

    public AlipayVersion(String versionString) {
        this.versionString = versionString;
        String[] split = versionString.split("\\.");
        int length = split.length;
        versionArray = new Integer[length];
        for (int i = 0; i < length; i++) {
            try {
                versionArray[i] = Integer.parseInt(split[i]);
            } catch (Exception e) {
                versionArray[i] = Integer.MAX_VALUE;
            }
        }
    }

    @Override
    public int compareTo(AlipayVersion alipayVersion) {
        int thisLength = versionArray.length;
        int thatLength = alipayVersion.versionArray.length;
        int compareResult;
        int length;
        if (thisLength > thatLength) {
            compareResult = 1;
            length = thatLength;
        } else if (thisLength < thatLength) {
            compareResult = -1;
            length = thisLength;
        } else {
            compareResult = 0;
            length = thisLength;
        }
        for (int i = 0; i < length; i++) {
            Integer thisVer = versionArray[i];
            Integer thatVer = alipayVersion.versionArray[i];
            if (thisVer < thatVer) {
                return -1;
            } else if (thisVer > thatVer) {
                return 1;
            }
        }
        return compareResult;
    }

}
