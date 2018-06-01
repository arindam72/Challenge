package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class AccountTransfer {

    @NotNull
    @NotEmpty
    private final String fromAccountId;

    @NotNull
    @NotEmpty
    private final String toAccountId;

    @NotNull
    @Min(value = 1, message = "Transfer Amount must be greater than 0!")
    private BigDecimal transferAmount;

    public AccountTransfer(String fromAccountId, String toAccountId) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transferAmount = BigDecimal.ZERO;
    }

    @JsonCreator
    public AccountTransfer(@JsonProperty("fromAccountId") String fromAccountId,
                   @JsonProperty("toAccountId") String toAccountId,
                   @JsonProperty("transferAmount") BigDecimal transferAmount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transferAmount = transferAmount;
    }
}
