package com.santhrupthi.model.base;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;

/**
 * @author Manjunath M T
 * @version 1.0
 * @since 2025-05-04
 */
@Getter
@Setter
@MappedSuperclass
@EqualsAndHashCode
public abstract class BaseEntity <T extends BaseEntity<T>> implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    
    @Column
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private String modifiedBy;
    
    @PrePersist
    protected void onPrePersist() {
        this.createdOn = new Date();
        this.modifiedOn = new Date();
        this.createdBy = "system";
        this.modifiedBy = "system"; 
    }
    
    @PreUpdate
    protected void onPreUpdate() {
        this.modifiedOn = new Date();
        this.modifiedBy = "system";
    }
    
} 