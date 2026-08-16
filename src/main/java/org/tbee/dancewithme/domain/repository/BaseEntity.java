package org.tbee.dancewithme.domain.repository;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@MappedSuperclass
public class BaseEntity<T> implements Comparable<BaseEntity<T>> {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    protected long id;
    static public final String ID = "id";

    public boolean entityIsNew() { // if we name this 'is...' the Grid component will add columns!
        return id == 0;
    }
    public boolean entityIsPersisted() {  // if we name this 'is...' the Grid component will add columns!
        return !entityIsNew();
    }

    @Version
    private long lazylock;

    public long id() {
        return id;
    }
    public T id(long id) {
        this.id = id;
        return (T)this;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }

    @Override
    public int hashCode() {
        if (id == 0) {
            return System.identityHashCode(this);
        }
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BaseEntity other = (BaseEntity) obj;
        if (id == 0) {
            return System.identityHashCode(this) == System.identityHashCode(obj);
        }
        return id == other.id;
    }

    @Override
    public int compareTo(@NotNull BaseEntity<T> o) {
        return Long.compare(id, o.id);
    }
}
