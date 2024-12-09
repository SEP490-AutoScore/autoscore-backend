package com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Type_Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationView {
    private Long notificationId;
    private String title;
    private String content;
    private String targetUrl;
    private Notification_Type_Enum type;
}
