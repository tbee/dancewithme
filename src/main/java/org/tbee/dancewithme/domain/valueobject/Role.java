package org.tbee.dancewithme.domain.valueobject;

public enum Role { LEAD, FOLLOW;
    public String translationKey() {
        return "role." + name().toLowerCase();
    }

    public boolean match(Role role) {
        return switch (this) {
            case LEAD -> role == FOLLOW;
            case FOLLOW -> role == LEAD;
        };
    }
}
