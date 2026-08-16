package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
public class BaseEntity<T> implements Comparable<BaseEntity<T>> {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    protected long id;
    static public final String ID = "id";

    @Version
    @Column(nullable = false)
    private long lazylock;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean entityIsNew() { // if we name this 'is...' the Grid component will add columns!
        return id == 0;
    }
    public boolean entityIsPersisted() {  // if we name this 'is...' the Grid component will add columns!
        return !entityIsNew();
    }

    public long id() {
        return id;
    }
    public T id(long id) {
        this.id = id;
        return (T)this;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
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
