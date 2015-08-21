package co.launcharea.fitter.util;

/**
 * Created by jack on 15. 8. 11.
 */
public class StringUtils {
    public static int countLines(String str) {
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }
}
