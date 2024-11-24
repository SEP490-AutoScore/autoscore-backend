package com.CodeEvalCrew.AutoScore.models.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.CodeEvalCrew.AutoScore.models.Entity.Enum.AIName_Enum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "ai_api_key")
public class AI_Api_Key {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiApiKeyId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AIName_Enum aiName;

    @NotNull
    private String aiApiKey;

    private boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean isShared;

    @OneToMany(mappedBy = "aiApiKey", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Account_Selected_Key> accountSelectedKeys;

    @ManyToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

}
