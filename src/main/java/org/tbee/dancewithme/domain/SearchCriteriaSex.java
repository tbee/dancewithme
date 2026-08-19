package org.tbee.dancewithme.domain;

public enum SearchCriteriaSex { MALE, FEMALE, EITHER;

    public boolean match(Sex sex) {
        return switch (this) {
            case MALE -> sex == Sex.MALE;
            case FEMALE -> sex == Sex.FEMALE;
            case EITHER -> true;
        };
    }
}
