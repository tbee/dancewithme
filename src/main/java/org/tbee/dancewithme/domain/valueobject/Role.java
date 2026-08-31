package org.tbee.dancewithme.domain.valueobject;

public enum Role { LEAD, FOLLOW, EITHER;
    public String translationKey() {
        return "role." + name().toLowerCase();
    }
}
