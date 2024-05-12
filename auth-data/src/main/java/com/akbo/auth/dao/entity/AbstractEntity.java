package com.akbo.auth.dao.entity;

import java.time.Instant;
import java.util.UUID;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private UUID id;
    @CreationTimestamp
    private Instant createdTime;
    @UpdateTimestamp
    private Instant lastUpdatedTime;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String lastUpdatedBy;
}
