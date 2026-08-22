package org.tbee.dancewithme.domain.valueobject;

public enum Sex { MALE, FEMALE, UNKNOWN;

    public String translationKey() {
        return "sex." + name().toLowerCase();
    }
}
