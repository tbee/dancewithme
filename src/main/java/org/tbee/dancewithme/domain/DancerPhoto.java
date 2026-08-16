package org.tbee.dancewithme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dancer_photo")
public class DancerPhoto extends BaseEntity<DancerPhoto> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dancer dancer;

    @Lob
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(nullable = false)
    private byte[] image;

    public Dancer dancer() {
        return dancer;
    }
    public DancerPhoto dancer(Dancer dancer) {
        this.dancer = dancer;
        return this;
    }

    public byte[] image() {
        return image;
    }
    public DancerPhoto image(byte[] image) {
        this.image = image;
        return this;
    }
}
