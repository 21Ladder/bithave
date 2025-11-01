package io.github.ladder.backend.listings.api;

public class ListingControllerUtils {

    //normalizes the String just for safety reasons and usability, used in the Listingscontroller
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

    //asserts if not null or negative, used in the Listingscontroller
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
