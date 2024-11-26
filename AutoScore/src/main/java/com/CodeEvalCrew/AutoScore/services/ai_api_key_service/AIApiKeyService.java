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
import com.CodeEvalCrew.AutoScore.models.Entity.Account_Selected_Key;
import com.CodeEvalCrew.AutoScore.models.Entity.Employee;
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

    @Override
    public List<AIApiKeyDTO> getAllAIApiKeys() {
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        // Lấy API keys thuộc về chính authenticated user
        List<AI_Api_Key> userApiKeys = aiApiKeyRepository.findByAccountAccountIdAndStatusTrue(authenticatedUserId);

        // Lấy API keys từ các tài khoản khác nhưng được chia sẻ
        List<AI_Api_Key> sharedApiKeys = aiApiKeyRepository.findByStatusTrueAndSharedTrue();

        // Sử dụng Map để loại bỏ trùng lặp dựa trên aiApiKeyId
        Map<Long, AI_Api_Key> uniqueApiKeys = new HashMap<>();
        userApiKeys.forEach(apiKey -> uniqueApiKeys.put(apiKey.getAiApiKeyId(), apiKey));
        sharedApiKeys.forEach(apiKey -> uniqueApiKeys.put(apiKey.getAiApiKeyId(), apiKey));

        // Chuyển đổi sang DTO và trả về
        return mapToDTOWithSelectionStatus(new ArrayList<>(uniqueApiKeys.values()), authenticatedUserId);
    }

    private List<AIApiKeyDTO> mapToDTOWithSelectionStatus(List<AI_Api_Key> apiKeys, Long authenticatedUserId) {
        // Lấy danh sách các API Key ID được chọn bởi authenticated user
        Set<Long> selectedKeyIds = accountSelectedKeyRepository
                .findByAccountAccountId(authenticatedUserId)
                .stream()
                .map(selectedKey -> selectedKey.getAiApiKey().getAiApiKeyId())
                .collect(Collectors.toSet());

        // Chuyển đổi danh sách AI_Api_Key sang AIApiKeyDTO
        return apiKeys.stream()
                .map(apiKey -> {
                    // Lấy fullName từ Employee liên kết với Account
                    String fullName = Optional.ofNullable(apiKey.getAccount())
                            .map(account -> Optional
                                    .ofNullable(employeeRepository.findByAccount_AccountId(account.getAccountId()))
                                    .map(Employee::getFullName)
                                    .orElse("Unknown")) // Xử lý trường hợp không tìm thấy Employee
                            .orElse("Unknown");

                    return new AIApiKeyDTO(
                            apiKey.getAiApiKeyId(),
                            apiKey.getAiName(),
                            apiKey.getAiApiKey(),
                            apiKey.isStatus(),
                            apiKey.isShared(),
                            apiKey.getCreatedAt(),
                            apiKey.getUpdatedAt(),
                            fullName, // Gán fullName thay vì accountId
                            selectedKeyIds.contains(apiKey.getAiApiKeyId()) // Kiểm tra xem API Key có được chọn không
                    );
                })
                .collect(Collectors.toList());
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
            // Nếu đã tồn tại, cập nhật (vì đây có thể là một danh sách, lấy phần tử đầu
            // tiên)
            Account_Selected_Key existingSelectedKey = existingSelectedKeys.get(0); // Giả sử chỉ có 1 bản ghi
            existingSelectedKey.setAiApiKey(selectedApiKey);
            accountSelectedKeyRepository.save(existingSelectedKey);
        }
    }

    @Override
    public AIApiKeyDTO createAIApiKey(CreateAIApiKeyDTO dto) {
        // Lấy ID người dùng được xác thực
        Long authenticatedUserId = Util.getAuthenticatedAccountId();

        // Tìm Account tương ứng
        Account account = accountRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        // Lấy fullName từ Employee liên kết với Account
        String fullName = Optional.ofNullable(employeeRepository.findByAccount_AccountId(authenticatedUserId))
                .map(Employee::getFullName)
                .orElse("Unknown");

        // Tạo mới AI_Api_Key
        AI_Api_Key newApiKey = new AI_Api_Key();
        newApiKey.setAiName(dto.getAiName());
        newApiKey.setAiApiKey(dto.getAiApiKey());
        newApiKey.setStatus(true); // Mặc định là active
        newApiKey.setShared(dto.isShared()); // Đảm bảo rằng giá trị isShared được gán đúng
        newApiKey.setCreatedAt(LocalDateTime.now());
     
        newApiKey.setAccount(account);

        // Lưu vào cơ sở dữ liệu
        AI_Api_Key savedApiKey = aiApiKeyRepository.save(newApiKey);

        // Chuyển đổi sang DTO để trả về
        return new AIApiKeyDTO(
                savedApiKey.getAiApiKeyId(),
                savedApiKey.getAiName(),
                savedApiKey.getAiApiKey(),
                savedApiKey.isStatus(),
                savedApiKey.isShared(),
                savedApiKey.getCreatedAt(),
                savedApiKey.getUpdatedAt(),
                fullName,
                false // Mặc định chưa được chọn
        );
    }

    @Override
public void updateAI_Api_Key(Long aiApiKeyId, String aiApiKey) {
    // Lấy AI_Api_Key từ repository dựa trên aiApiKeyId
    Optional<AI_Api_Key> existingApiKeyOpt = aiApiKeyRepository.findById(aiApiKeyId);

    if (existingApiKeyOpt.isEmpty()) {
        // Ném ngoại lệ nếu không tìm thấy API Key
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API Key không tồn tại");
    }

    AI_Api_Key existingApiKey = existingApiKeyOpt.get();

    // Kiểm tra quyền sở hữu API Key (nếu cần)
    Long authenticatedUserId = Util.getAuthenticatedAccountId();
    if (!existingApiKey.getAccount().getAccountId().equals(authenticatedUserId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền cập nhật API Key này");
    }

    // Cập nhật giá trị API Key và thời gian cập nhật
    existingApiKey.setAiApiKey(aiApiKey);
    existingApiKey.setUpdatedAt(LocalDateTime.now());

    // Lưu thay đổi vào repository
    aiApiKeyRepository.save(existingApiKey);
}



    

    @Override
    public void deleteAiApiKey(Long aiApiKeyId) {
        // Tìm AI_Api_Key theo ID
        AI_Api_Key apiKey = aiApiKeyRepository.findById(aiApiKeyId)
                .orElseThrow(() -> new IllegalArgumentException("AI API Key not found with ID: " + aiApiKeyId));

        // Set status = false (đánh dấu là đã xóa)
        apiKey.setStatus(false);

        // Lưu thay đổi vào cơ sở dữ liệu
        aiApiKeyRepository.save(apiKey);
    }

}
