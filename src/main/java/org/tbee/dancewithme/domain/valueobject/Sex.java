package org.tbee.dancewithme.domain.valueobject;

public enum Sex { MALE, FEMALE, UNKNOWN;

    public String translationKey() {
        return "sex." + name().toLowerCase();
    }

    public String translationKeyNoun() {
        return "sex." + name().toLowerCase() + ".noun";
    }
}
