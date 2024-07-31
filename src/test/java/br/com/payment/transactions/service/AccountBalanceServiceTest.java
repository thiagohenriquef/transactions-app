package br.com.payment.transactions.service;

import br.com.payment.transactions.domain.AccountBalance;
import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.exception.AccountNotFoundException;
import br.com.payment.transactions.repository.AccountBalanceRepository;
import br.com.payment.transactions.utils.TransactionsStatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountBalanceServiceTest {

    @Mock
    private AccountBalanceRepository accountRepository;

    @InjectMocks
    private AccountBalanceService accountBalanceService;

    private Transaction transaction;

    @BeforeEach
    public void setUp() {
        transaction = new Transaction();
        transaction.setAccountId("12345");
    }

    @Test
    public void testGetAccountBalance_Success() throws Exception {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountId("12345");

        when(accountRepository.getAccountBalanceById("12345")).thenReturn(accountBalance);

        AccountBalance result = accountBalanceService.getAccountBalance(transaction);
        assertNotNull(result);
        assertEquals("12345", result.getAccountId());
    }

    @Test
    public void testGetAccountBalance_AccountNotFound() throws Exception {
        when(accountRepository.getAccountBalanceById("12345")).thenReturn(null);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountBalanceService.getAccountBalance(transaction);
        });

        assertEquals(TransactionsStatusCodes.GENERIC_ERROR, exception.getCode());
    }

    @Test
    public void testGetAccountBalance_Exception() throws Exception {
        when(accountRepository.getAccountBalanceById("12345")).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountBalanceService.getAccountBalance(transaction);
        });

        assertTrue(exception.getMessage().contains("Database error"));
    }

    @Test
    public void testSaveBalance() {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountId("12345");

        doNothing().when(accountRepository).save(accountBalance);

        accountBalanceService.saveBalance(accountBalance);

        verify(accountRepository, times(1)).save(accountBalance);
    }
}
