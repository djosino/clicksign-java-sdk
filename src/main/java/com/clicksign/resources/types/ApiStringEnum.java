package com.clicksign.resources.types;

/** API string value carried by SDK enums. */
public interface ApiStringEnum {

    String apiValue();

    static <E extends Enum<E> & ApiStringEnum> E tryParse(Class<E> type, String value) {
        if (value == null) return null;
        for (E constant : type.getEnumConstants()) {
            if (constant.apiValue().equals(value)) return constant;
        }
        return null;
    }

    static <E extends Enum<E> & ApiStringEnum> E require(Class<E> type, String value) {
        E parsed = tryParse(type, value);
        if (parsed == null) {
            throw new IllegalArgumentException("unknown " + type.getSimpleName() + ": " + value);
        }
        return parsed;
    }
}
