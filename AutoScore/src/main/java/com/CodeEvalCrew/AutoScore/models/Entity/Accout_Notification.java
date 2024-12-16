package com.CodeEvalCrew.AutoScore.models.Entity;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Status_Enum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Accout_Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationAccountId;
    @Enumerated(EnumType.STRING)
    private Notification_Status_Enum status;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accountId", referencedColumnName = "accountId", nullable = false)
    private Account account;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "notificationId", referencedColumnName = "notificationId", nullable = false)
    private Notification notification;
}
