package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;

import com.db.awmd.challenge.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private TransferService transferService;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/create").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  public void transferAmountWithoutErrors() throws Exception {
      this.setupAccounts();
      this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
              .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-456\"," +
                      "\"transferAmount\":1000}")).andExpect(status().isOk());

  }

  @Test
  public void transferWithNegativeAmount() throws Exception {
      this.setupAccounts();
      this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
              .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-456\"," +
                      "\"transferAmount\":-345}")).andExpect(status().isBadRequest());
  }

    @Test
    public void transferWithInsufficientBalance() throws Exception {
        this.setupAccountsWithInsufficientFunds();
        this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-456\"," +
                        "\"transferAmount\":800}")).andExpect(status().isBadRequest());
    }


    private void setupAccounts() {
        Account fromAccount = new Account("Id-123");
        Account toAccount = new Account("Id-456");
        fromAccount.setBalance(new BigDecimal(5000));
        toAccount.setBalance(new BigDecimal(2000));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);
    }

    private void setupAccountsWithInsufficientFunds() {
        Account fromAccount = new Account("Id-123");
        Account toAccount = new Account("Id-456");
        fromAccount.setBalance(new BigDecimal(450));
        toAccount.setBalance(new BigDecimal(2000));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);
    }

}
