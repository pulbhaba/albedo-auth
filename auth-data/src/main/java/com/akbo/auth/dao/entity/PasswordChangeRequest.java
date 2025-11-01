package com.akbo.auth.dao.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Audited(withModifiedFlag = true)
@Table(name = "password_change_request")
@EqualsAndHashCode(callSuper = true)
public class PasswordChangeRequest extends AbstractEntity {
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(referencedColumnName = "id", name = "user_id")
    private User user;
    @Column(length = 20)
    private String randomString;
    private Boolean expired;
    private Boolean passwordChanged;
    private LocalDateTime emailNotificationSent;
}
