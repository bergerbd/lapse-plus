package lapsePlus.utils;

public final class StringUtils {
    public static String cutto(String str, int to) {
        if (str.length() < to - 3) {
            return str + repeat(" ", to - str.length());
        } else {
            return str.substring(0, to - 3) + "...";
        }
    }

    public static String repeat(String str, int times) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < times; i++) {
            buf.append(str);
        }
        return buf.toString();
    }

}
