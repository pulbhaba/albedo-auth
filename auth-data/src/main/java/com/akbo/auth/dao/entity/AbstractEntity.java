package com.akbo.auth.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(generator = "sequence-generator")
    @SequenceGenerator(
            name = "sequence-generator",
            sequenceName = "user_sequence",
            initialValue = 100000,
            allocationSize = 1
    )
    private Long id;

    @CreationTimestamp
    @Column(columnDefinition = "datetime")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(columnDefinition = "datetime")
    private LocalDateTime lastUpdatedTime;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastUpdatedBy;
}
