package com.CodeEvalCrew.AutoScore.services.ai_api_key_service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.ai_api_key_repository.AiApiKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.organization_repository.IOrganizationRepoistory;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class AIApiKeyService implements IAIApiKeyService {

    @Autowired
    private AiApiKeyRepository aiApiKeyRepository;

    @Autowired
    private AccountOrganizationRepository accountOrganizationRepository;

    @Autowired
    private IOrganizationRepoistory organizationRepository;

    @Override
    public List<AIApiKeyDTO> getAllAIApiKeys() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        try {
            // Lấy Account_Organization của authenticatedUserId
            Account_Organization userAccountOrg = accountOrganizationRepository
                    .findByAccountAccountIdAndStatusTrue(authenticatedUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not associated with any organization"));

            // Xác định tổ chức lớn (node con của root gốc)
            Organization rootOrganization = findRootOrganization();
            Organization largeOrganization = findLargeOrganization(userAccountOrg.getOrganization(), rootOrganization);

            // Thu thập tất cả organizationId thuộc tổ chức lớn
            Set<Long> allOrgIds = new HashSet<>();
            collectSubOrganizations(largeOrganization.getOrganizationId(), allOrgIds);

            // Lấy danh sách accountId trong các tổ chức
            List<Long> accountIdsInOrganizations = accountOrganizationRepository
                    .findByOrganizationOrganizationIdInAndStatusTrue(allOrgIds)
                    .stream()
                    .map(accountOrg -> accountOrg.getAccount().getAccountId())
                    .collect(Collectors.toList());

            // Lọc API keys
            List<AI_Api_Key> apiKeys = aiApiKeyRepository
                    .findByAccountAccountIdInAndStatusTrue(accountIdsInOrganizations);

            // Nếu tìm thấy API keys, trả về danh sách
            if (!apiKeys.isEmpty()) {
                return apiKeys.stream().map(apiKey -> new AIApiKeyDTO(
                        apiKey.getAiApiKeyId(),
                        apiKey.getAiName(),
                        apiKey.getAiApiKey(),
                        apiKey.isStatus(),
                        apiKey.isShared(),
                        apiKey.getCreatedAt(),
                        apiKey.getUpdatedAt(),
                        apiKey.getAccount().getAccountId())).collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Log lỗi đệ quy (nếu cần)
            System.err.println("Error during recursive organization retrieval: " + e.getMessage());
        }

        // Nếu đệ quy lỗi hoặc không tìm thấy API keys, trả về keys do
        // authenticatedUserId tạo
        List<AI_Api_Key> fallbackApiKeys = aiApiKeyRepository.findByAccountAccountIdAndStatusTrue(authenticatedUserId);
        return fallbackApiKeys.stream().map(apiKey -> new AIApiKeyDTO(
                apiKey.getAiApiKeyId(),
                apiKey.getAiName(),
                apiKey.getAiApiKey(),
                apiKey.isStatus(),
                apiKey.isShared(),
                apiKey.getCreatedAt(),
                apiKey.getUpdatedAt(),
                apiKey.getAccount().getAccountId())).collect(Collectors.toList());
    }

    // Tìm root gốc (organization có parentId = null)
    private Organization findRootOrganization() {
        return organizationRepository.findByParentIdIsNullAndStatusTrue()
                .orElseThrow(() -> new IllegalArgumentException("Root organization not found"));
    }

    // Tìm tổ chức lớn của một tổ chức hiện tại
    private Organization findLargeOrganization(Organization organization, Organization rootOrganization) {
        while (!organization.getParentId().equals(rootOrganization.getOrganizationId())) {
            organization = organizationRepository.findById(organization.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid organization hierarchy"));
        }
        return organization;
    }

    // Đệ quy lấy tất cả tổ chức con của một tổ chức
    private void collectSubOrganizations(Long parentId, Set<Long> allOrgIds) {
        allOrgIds.add(parentId);
        List<Organization> subOrgs = organizationRepository.findByParentIdAndStatusTrue(parentId);
        for (Organization subOrg : subOrgs) {
            collectSubOrganizations(subOrg.getOrganizationId(), allOrgIds);
        }
    }

}
