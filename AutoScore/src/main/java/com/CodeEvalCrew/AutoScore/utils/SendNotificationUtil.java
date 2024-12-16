package com.CodeEvalCrew.AutoScore.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Accout_Notification;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Notification_Status_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Notification;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.notification_repository.AccountNotificationRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepository;

@Component
public class SendNotificationUtil {
    private final AccountNotificationRepository accNotiRepo;
    private final AccountOrganizationRepository accountOrganizationRepository;

    public SendNotificationUtil(
        AccountNotificationRepository accNotiRepo,
        IOrganizationRepository organizationRepository,
        AccountOrganizationRepository accountOrganizationRepository
    ){
        this.accNotiRepo = accNotiRepo;
        this.accountOrganizationRepository = accountOrganizationRepository;
    }

    public void sendNotiToAllAccountIncampus(Notification notio, Organization campus){
        List<Account_Organization> accOrgs = accountOrganizationRepository.findByOrganizationOrganizationIdAndStatusTrue(campus.getOrganizationId());
        for (Account_Organization accOrg : accOrgs) {
            Accout_Notification accNoti = new Accout_Notification(null, Notification_Status_Enum.UNREAD, accOrg.getAccount(), notio);
            accNotiRepo.save(accNoti);
        }
    }
}
