package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountTransfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.db.awmd.challenge.exception.InvalidTransferRequestException;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.NotificationService;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void transferToAccount(AccountTransfer transfer)
          throws InvalidTransferRequestException {
    if (transfer.getTransferAmount().doubleValue() < 0) {
        throw new InvalidTransferRequestException("Transfer amount must be a positive number!");
    }
    // Transfer operation needs to be thread safe
    synchronized (this) {
        Account fromAccount = this.getAccount(transfer.getFromAccountId());
        if (fromAccount.getBalance().doubleValue() < transfer.getTransferAmount().doubleValue()) {
            throw new InvalidTransferRequestException("Insufficient funds to carry out this transfer!");
        }
        Account toAccount = this.getAccount(transfer.getToAccountId());
        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getTransferAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transfer.getTransferAmount()));
        NotificationService notification = new EmailNotificationService();
        notification.notifyAboutTransfer(fromAccount, "An amount of " + transfer.getTransferAmount() +
                " has been successfully transferred to the account id: " + toAccount.getAccountId() + "!");
    }
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

}
