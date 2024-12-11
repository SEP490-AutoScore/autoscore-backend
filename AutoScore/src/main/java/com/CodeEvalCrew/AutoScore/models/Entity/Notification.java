package com.CodeEvalCrew.AutoScore.models.Entity;

import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Type_Enum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    private String title;
    private String content;
    private String targetUrl;
    @Enumerated(EnumType.STRING)
    private Notification_Type_Enum type;
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
    private Set<Accout_Notification> notification_Accounts;
}
