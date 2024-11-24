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
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
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
    @Autowired
    private AccountSelectedKeyRepository accountSelectedKeyRepository;

    @Override
    public List<AIApiKeyDTO> getAllAIApiKeys() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        try {
            // Fetch Account_Organization for the authenticated user
            Account_Organization userAccountOrg = accountOrganizationRepository
                    .findByAccountAccountIdAndStatusTrue(authenticatedUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not associated with any organization"));

            // Determine the large organization
            Organization rootOrganization = findRootOrganization();
            Organization largeOrganization = findLargeOrganization(userAccountOrg.getOrganization(), rootOrganization);

            // Collect all organization IDs in the hierarchy
            Set<Long> allOrgIds = new HashSet<>();
            collectSubOrganizations(largeOrganization.getOrganizationId(), allOrgIds);

            // Fetch account IDs in the organizations
            List<Long> accountIdsInOrganizations = accountOrganizationRepository
                    .findByOrganizationOrganizationIdInAndStatusTrue(allOrgIds)
                    .stream()
                    .map(accountOrg -> accountOrg.getAccount().getAccountId())
                    .collect(Collectors.toList());

            // Fetch API keys
            List<AI_Api_Key> apiKeys = aiApiKeyRepository
                    .findByAccountAccountIdInAndStatusTrue(accountIdsInOrganizations);

            // Check selection status and map to DTO
            return mapToDTOWithSelectionStatus(apiKeys, authenticatedUserId);

        } catch (Exception e) {
            // Handle recursive organization retrieval errors
            System.err.println("Error during recursive organization retrieval: " + e.getMessage());
        }

        // Fallback: Fetch API keys created by the authenticated user
        List<AI_Api_Key> fallbackApiKeys = aiApiKeyRepository.findByAccountAccountIdAndStatusTrue(authenticatedUserId);
        return mapToDTOWithSelectionStatus(fallbackApiKeys, authenticatedUserId);
    }

    private List<AIApiKeyDTO> mapToDTOWithSelectionStatus(List<AI_Api_Key> apiKeys, Long authenticatedUserId) {
        Set<Long> selectedKeyIds = accountSelectedKeyRepository
                .findByAccountAccountId(authenticatedUserId)
                .stream()
                .map(key -> key.getAiApiKey().getAiApiKeyId())
                .collect(Collectors.toSet());

        return apiKeys.stream()
                .map(apiKey -> new AIApiKeyDTO(
                        apiKey.getAiApiKeyId(),
                        apiKey.getAiName(),
                        apiKey.getAiApiKey(),
                        apiKey.isStatus(),
                        apiKey.isShared(),
                        apiKey.getCreatedAt(),
                        apiKey.getUpdatedAt(),
                        apiKey.getAccount().getAccountId(),
                        selectedKeyIds.contains(apiKey.getAiApiKeyId()) // Check if selected
                ))
                .collect(Collectors.toList());
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

    @Override
    public void updateOrCreateAccountSelectedKey(Long aiApiKeyId) {
        // Lấy authenticatedUserId từ Util
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
    
        // Kiểm tra xem aiApiKeyId có hợp lệ không
        AI_Api_Key selectedApiKey = aiApiKeyRepository.findById(aiApiKeyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid AI API Key ID"));
    
        // Tìm account đã chọn key trước đó
        List<Account_Selected_Key> existingSelectedKeys = accountSelectedKeyRepository
                .findByAccountAccountId(authenticatedUserId);
    
        if (existingSelectedKeys.isEmpty()) {
            // Nếu chưa có, tạo mới
            Account_Selected_Key newSelectedKey = new Account_Selected_Key();
            newSelectedKey.setAccount(selectedApiKey.getAccount()); // Gán account liên quan đến authenticatedUserId
            newSelectedKey.setAiApiKey(selectedApiKey); // Gán AI API Key
            accountSelectedKeyRepository.save(newSelectedKey);
        } else {
            // Nếu đã tồn tại, cập nhật (vì đây có thể là một danh sách, lấy phần tử đầu tiên)
            Account_Selected_Key existingSelectedKey = existingSelectedKeys.get(0); // Giả sử chỉ có 1 bản ghi
            existingSelectedKey.setAiApiKey(selectedApiKey);
            accountSelectedKeyRepository.save(existingSelectedKey);
        }
    }
    


}
