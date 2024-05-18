package com.akbo.auth.dao.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.joda.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Audited(withModifiedFlag = true)
@Table(name = "password_change_request")
@EqualsAndHashCode(callSuper = true)
public class PasswordChangeRequest extends AbstractEntity {
    @ManyToOne(targetEntity = User.class, optional = true)
    @JoinColumn(referencedColumnName = "id", name = "user_id")
    private User user;
    private String randomString;
    private Boolean expired;
    private Boolean passwordChanged;
    private LocalDateTime emailNotificationSent;
}
