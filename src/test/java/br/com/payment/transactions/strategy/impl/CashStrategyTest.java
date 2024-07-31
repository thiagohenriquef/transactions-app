package br.com.payment.transactions.strategy.impl;

import br.com.payment.transactions.domain.AccountBalance;
import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.service.AccountBalanceService;
import br.com.payment.transactions.utils.TransactionsStatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CashStrategyTest {

    @Mock
    private AccountBalanceService accountBalanceService;

    @InjectMocks
    private CashStrategy cashStrategy;

    private Transaction transaction;
    private AccountBalance accountBalance;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        transaction = new Transaction();
        transaction.setAccountId("123");
        transaction.setAmount(100.0);
        accountBalance = new AccountBalance();
    }

    @Test
    public void testAuthorize_Success() {
        accountBalance.setCashBalance(200.0);
        when(accountBalanceService.getAccountBalance(any(Transaction.class))).thenReturn(accountBalance);

        TransactionResponseBody response = cashStrategy.authorize(transaction);

        assertEquals(TransactionsStatusCodes.SUCCESS_TRANSACTION, response.getCode());
        assertEquals(100.0, accountBalance.getCashBalance());
        verify(accountBalanceService, times(1)).saveBalance(accountBalance);
    }

    @Test
    public void testAuthorize_InsufficientBalance() {
        accountBalance.setCashBalance(50.0);
        when(accountBalanceService.getAccountBalance(any(Transaction.class))).thenReturn(accountBalance);

        TransactionResponseBody response = cashStrategy.authorize(transaction);

        assertEquals(TransactionsStatusCodes.INSUFFICIENT_BALANCE, response.getCode());
        assertEquals(50.0, accountBalance.getCashBalance());
        verify(accountBalanceService, never()).saveBalance(accountBalance);
    }

    @Test
    public void testHasAvailableBalance_SufficientBalance() {
        accountBalance.setCashBalance(200.0);
        when(accountBalanceService.getAccountBalance(any(Transaction.class))).thenReturn(accountBalance);

        boolean hasBalance = cashStrategy.hasAvailableBalance(transaction);

        assertEquals(true, hasBalance);
    }

    @Test
    public void testHasAvailableBalance_InsufficientBalance() {
        accountBalance.setCashBalance(50.0);
        when(accountBalanceService.getAccountBalance(any(Transaction.class))).thenReturn(accountBalance);

        boolean hasBalance = cashStrategy.hasAvailableBalance(transaction);

        assertEquals(false, hasBalance);
    }

    @Test
    public void testGetMccCode() {
        List<String> mccCodes = cashStrategy.getMccCode();
        assertEquals(0, mccCodes.size());
    }
}
