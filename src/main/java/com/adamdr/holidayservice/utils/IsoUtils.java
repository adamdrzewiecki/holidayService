package com.adamdr.holidayservice.utils;

import java.util.Locale;
import java.util.Set;

public final class IsoUtils {

    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

    private IsoUtils() {}

    public static boolean isValidISOCountry(String countryCode) {
        return ISO_COUNTRIES.contains(countryCode);
    }
}
