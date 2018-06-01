package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountTransfer;
import com.db.awmd.challenge.exception.InvalidTransferRequestException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.TransferService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferServiceTest {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private  TransferService transferService;

    private static final String NEGATIVE_TRANSACTION_AMT_MSG = "Transfer amount must be a positive number!";

    private static final String INSUFFICIENT_FUNDS_MSG = "Insufficient funds to carry out this transfer!";

    @Test
    public void testTransfer_withoutErrors() throws Exception {
        this.setupAccounts();
        AccountTransfer accountTransfer = new AccountTransfer("Id-123", "Id=456");
        accountTransfer.setTransferAmount(new BigDecimal(1000));
        this.transferService.transferAmount(accountTransfer);
        assertThat(this.accountsRepository.getAccount("Id-123")
                .getBalance().doubleValue() == 4000);
        assertThat(this.accountsRepository.getAccount("Id-456")
                .getBalance().doubleValue() == 3000);
    }

    @Test
    public void testTransfer_failsOnNegativeTransferAmount() throws Exception {
        this.setupAccounts();
        AccountTransfer accountTransfer = new AccountTransfer("Id-123", "Id=456");
        accountTransfer.setTransferAmount(new BigDecimal(-650));
        try {
            this.transferService.transferAmount(accountTransfer);
            fail("Should have failed due to negative transfer amount!");
        } catch (InvalidTransferRequestException ex) {
            assertThat(ex.getMessage()).isEqualTo(this.NEGATIVE_TRANSACTION_AMT_MSG);
        }
    }

    @Test
    public void testTransfer_failsOnInsufficientFunds() throws Exception {
        this.setupAccountsWithInsufficientFunds();
        AccountTransfer accountTransfer = new AccountTransfer("Id-123", "Id=456");
        accountTransfer.setTransferAmount(new BigDecimal(1000));
        try {
            this.transferService.transferAmount(accountTransfer);
            fail("Should have failed due to insufficient funds!");
        } catch (InvalidTransferRequestException ex) {
            assertThat(ex.getMessage()).isEqualTo(this.INSUFFICIENT_FUNDS_MSG);
        }
    }

    private void setupAccounts() {
        Account fromAccount = new Account("Id-123");
        Account toAccount = new Account("Id=456");
        fromAccount.setBalance(new BigDecimal(5000));
        toAccount.setBalance(new BigDecimal(2000));
        this.accountsRepository.createAccount(fromAccount);
        this.accountsRepository.createAccount(toAccount);
    }

    private void setupAccountsWithInsufficientFunds() {
        Account fromAccount = new Account("Id-123");
        Account toAccount = new Account("Id=456");
        fromAccount.setBalance(new BigDecimal(450));
        toAccount.setBalance(new BigDecimal(2000));
        this.accountsRepository.createAccount(fromAccount);
        this.accountsRepository.createAccount(toAccount);
    }

}
