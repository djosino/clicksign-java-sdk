package com.clicksign.resources.types;

/** API string value carried by SDK enums. */
public interface ApiStringEnum {

    /**
     * Returns the API string value for this enum constant.
     *
     * @return API string value
     */
    String apiValue();

    /**
     * Parses API string to enum constant, returning null if unrecognized.
     *
     * @param <E> enum type
     * @param type enum class
     * @param value API string value
     * @return matching enum constant, or {@code null} if not found
     */
    static <E extends Enum<E> & ApiStringEnum> E tryParse(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        for (E constant : type.getEnumConstants()) {
            if (constant.apiValue().equals(value)) {
                return constant;
            }
        }
        return null;
    }

    /**
     * Parses API string, throwing if unrecognized.
     *
     * @param <E> enum type
     * @param type enum class
     * @param value API string value
     * @return matching enum constant
     * @throws IllegalArgumentException if value is not recognized
     */
    static <E extends Enum<E> & ApiStringEnum> E require(Class<E> type, String value) {
        E parsed = tryParse(type, value);
        if (parsed == null) {
            throw new IllegalArgumentException("unknown " + type.getSimpleName() + ": " + value);
        }
        return parsed;
    }
}
