package io.github.ladder.backend.listings.api;

public class ListingControllerUtils {

    public static String normalizeString(String s) {
        if (s != null) {
            s = s.trim();
            if (s.isEmpty()) {
                return null;
            } else {
                return s;
            }
        } else {
            return null;
        }
    }

    public static Long nonNegativeOrNull(Long v) {
        if (v != null) {
            if (v >= 0) {
                return v;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}
