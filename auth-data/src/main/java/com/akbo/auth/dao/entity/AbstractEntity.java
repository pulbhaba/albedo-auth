package com.akbo.auth.dao.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
  @GeneratedValue(generator = "sequence-generator")
  @GenericGenerator(name = "sequence-generator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
      @Parameter(name = "sequence_name", value = "user_sequence"),
      @Parameter(name = "initial_value", value = "100000"),
      @Parameter(name = "increment_size", value = "1")
  })
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
