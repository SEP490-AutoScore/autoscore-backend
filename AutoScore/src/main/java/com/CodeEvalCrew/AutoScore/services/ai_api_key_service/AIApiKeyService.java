package com.CodeEvalCrew.AutoScore.services.ai_api_key_service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.CodeEvalCrew.AutoScore.models.DTO.RequestDTO.CreateAIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.DTO.ResponseDTO.AIApiKeyDTO;
import com.CodeEvalCrew.AutoScore.models.Entity.AI_Api_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Account;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Organization;
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
import com.CodeEvalCrew.AutoScore.models.Entity.Enum.Organization_Enum;
import com.CodeEvalCrew.AutoScore.models.Entity.Organization;
import com.CodeEvalCrew.AutoScore.repositories.account_organization_repository.AccountOrganizationRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IAccountRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_repository.IEmployeeRepository;
import com.CodeEvalCrew.AutoScore.repositories.account_selected_key_repository.AccountSelectedKeyRepository;
import com.CodeEvalCrew.AutoScore.repositories.ai_api_key_repository.AiApiKeyRepository;
import com.CodeEvalCrew.AutoScore.utils.Util;

@Service
public class AIApiKeyService implements IAIApiKeyService {

    @Autowired
    private AiApiKeyRepository aiApiKeyRepository;

    @Autowired
    private AccountSelectedKeyRepository accountSelectedKeyRepository;
    @Autowired
    private IAccountRepository accountRepository;
    @Autowired
    private IEmployeeRepository employeeRepository;
    @Autowired
    private AccountOrganizationRepository accountOrganizationRepository;

    private String checkCampusForAccount(Long accountId) {

        List<Account_Organization> accountOrganizations = accountOrganizationRepository
                .findByAccount_AccountId(accountId);

        for (Account_Organization accountOrg : accountOrganizations) {
            Organization organization = accountOrg.getOrganization();
            if (organization.getType() == Organization_Enum.CAMPUS) {
                return organization.getName();
            }
        }

        return null;
    }

    @Override
    public List<AIApiKeyDTO> getAllAIApiKeys() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        String userCampus = checkCampusForAccount(authenticatedUserId);
        if (userCampus == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authenticated user does not belong to any campus.");
        }

        List<AI_Api_Key> userApiKeys = aiApiKeyRepository.findByAccountAccountIdAndStatusTrue(authenticatedUserId);

        List<AI_Api_Key> sharedApiKeys = aiApiKeyRepository.findByStatusTrueAndSharedTrue();

        List<AI_Api_Key> filteredSharedApiKeys = sharedApiKeys.stream()
                .filter(apiKey -> {
                    String creatorCampus = checkCampusForAccount(apiKey.getAccount().getAccountId());
                    return userCampus.equals(creatorCampus);
                })
                .collect(Collectors.toList());

        Map<Long, AI_Api_Key> uniqueApiKeys = new HashMap<>();
        userApiKeys.forEach(apiKey -> uniqueApiKeys.put(apiKey.getAiApiKeyId(), apiKey));
        filteredSharedApiKeys.forEach(apiKey -> uniqueApiKeys.put(apiKey.getAiApiKeyId(), apiKey));

        return mapToDTOWithSelectionStatus(new ArrayList<>(uniqueApiKeys.values()), authenticatedUserId);
    }

    private List<AIApiKeyDTO> mapToDTOWithSelectionStatus(List<AI_Api_Key> apiKeys, Long authenticatedUserId) {

        Set<Long> selectedKeyIds = accountSelectedKeyRepository
                .findByAccountAccountId(authenticatedUserId)
                .stream()
                .map(selectedKey -> selectedKey.getAiApiKey().getAiApiKeyId())
                .collect(Collectors.toSet());

        return apiKeys.stream()
                .map(apiKey -> {

                    String fullName = Optional.ofNullable(apiKey.getAccount())
                            .map(account -> Optional
                                    .ofNullable(employeeRepository.findByAccount_AccountId(account.getAccountId()))
                                    .map(Employee::getFullName)
                                    .orElse("Unknown"))
                            .orElse("Unknown");

                    return new AIApiKeyDTO(
                            apiKey.getAiApiKeyId(),
                            apiKey.getAiName(),
                            apiKey.getAiApiKey(),
                            apiKey.isStatus(),
                            apiKey.isShared(),
                            apiKey.getCreatedAt(),
                            apiKey.getUpdatedAt(),
                            fullName,
                            selectedKeyIds.contains(apiKey.getAiApiKeyId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrCreateAccountSelectedKey(Long aiApiKeyId) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        AI_Api_Key selectedApiKey = aiApiKeyRepository.findById(aiApiKeyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid AI API Key ID"));

        List<Account_Selected_Key> existingSelectedKeys = accountSelectedKeyRepository
                .findByAccountAccountId(authenticatedUserId);

        if (existingSelectedKeys.isEmpty()) {

            Account_Selected_Key newSelectedKey = new Account_Selected_Key();
            newSelectedKey.setAccount(selectedApiKey.getAccount());
            newSelectedKey.setAiApiKey(selectedApiKey);
            accountSelectedKeyRepository.save(newSelectedKey);
        } else {

            Account_Selected_Key existingSelectedKey = existingSelectedKeys.get(0);
            existingSelectedKey.setAiApiKey(selectedApiKey);
            accountSelectedKeyRepository.save(existingSelectedKey);
        }
    }

    @Override
    public AIApiKeyDTO createAIApiKey(CreateAIApiKeyDTO dto) {

        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        Account account = accountRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        String fullName = Optional.ofNullable(employeeRepository.findByAccount_AccountId(authenticatedUserId))
                .map(Employee::getFullName)
                .orElse("Unknown");

        AI_Api_Key newApiKey = new AI_Api_Key();
        newApiKey.setAiName(dto.getAiName());
        newApiKey.setAiApiKey(dto.getAiApiKey());
        newApiKey.setStatus(true);
        newApiKey.setShared(dto.isShared());
        newApiKey.setCreatedAt(LocalDateTime.now());

        newApiKey.setAccount(account);

        AI_Api_Key savedApiKey = aiApiKeyRepository.save(newApiKey);

        return new AIApiKeyDTO(
                savedApiKey.getAiApiKeyId(),
                savedApiKey.getAiName(),
                savedApiKey.getAiApiKey(),
                savedApiKey.isStatus(),
                savedApiKey.isShared(),
                savedApiKey.getCreatedAt(),
                savedApiKey.getUpdatedAt(),
                fullName,
                false);
    }

    @Override
    public void updateAI_Api_Key(Long aiApiKeyId, String aiApiKey, boolean shared) {

        Optional<AI_Api_Key> existingApiKeyOpt = aiApiKeyRepository.findById(aiApiKeyId);
    
        if (existingApiKeyOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API Key not exists");
        }
    
        AI_Api_Key existingApiKey = existingApiKeyOpt.get();
    
        Long authenticatedUserId = Util.getAuthenticatedAccountId();
        if (!existingApiKey.getAccount().getAccountId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this key");
        }
    
        existingApiKey.setAiApiKey(aiApiKey);
        existingApiKey.setShared(shared);  
        existingApiKey.setUpdatedAt(LocalDateTime.now());
    
        aiApiKeyRepository.save(existingApiKey);
    }
    

    @Override
    public boolean deleteAIApiKey(Long aiApiKeyId) {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        AI_Api_Key apiKey = aiApiKeyRepository.findById(aiApiKeyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI API Key not found"));

        if (!apiKey.getAccount().getAccountId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to delete this AI API Key");
        }

        apiKey.setStatus(false);
        apiKey.setUpdatedAt(LocalDateTime.now());
        aiApiKeyRepository.save(apiKey);

        return true;
    }

    @Override
    public AIApiKeyDTO getAIApiKeyById(Long aiApiKeyId) {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        Optional<AI_Api_Key> aiApiKeyOptional = aiApiKeyRepository.findById(aiApiKeyId);

        if (!aiApiKeyOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API Key not found");
        }

        AI_Api_Key aiApiKey = aiApiKeyOptional.get();

        String fullName = Optional.ofNullable(aiApiKey.getAccount())
        .map(account -> Optional
            .ofNullable(employeeRepository.findByAccount_AccountId(account.getAccountId()))
            .map(Employee::getFullName)
            .orElse("Unknown"))
        .orElse("Unknown");
        
        Set<Long> selectedKeyIds = accountSelectedKeyRepository
        .findByAccountAccountId(authenticatedUserId)
        .stream()
        .map(selectedKey -> selectedKey.getAiApiKey().getAiApiKeyId())
        .collect(Collectors.toSet());

        AIApiKeyDTO aiApiKeyDTO = new AIApiKeyDTO(
        

                aiApiKey.getAiApiKeyId(),
                aiApiKey.getAiName(),
                aiApiKey.getAiApiKey(),
                aiApiKey.isStatus(),
                aiApiKey.isShared(),
                aiApiKey.getCreatedAt(),
                aiApiKey.getUpdatedAt(),
                fullName,
                selectedKeyIds.contains(aiApiKey.getAiApiKeyId())
                );
               
        return aiApiKeyDTO;
    }


}
