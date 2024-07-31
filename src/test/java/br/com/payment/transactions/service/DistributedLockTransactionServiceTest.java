package br.com.payment.transactions.service;

import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.strategy.MccStrategy;
import br.com.payment.transactions.strategy.impl.CashStrategy;
import br.com.payment.transactions.strategy.impl.MealStrategy;
import br.com.payment.transactions.utils.TransactionsStatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DistributedLockTransactionServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private MccStrategy foodStrategy;

    @Mock
    private CashStrategy cashStrategy;

    @Mock
    private MealStrategy mealStrategy;

    private DistributedLockTransactionService transactionService;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        when(redissonClient.getLock(anyString())).thenReturn(rLock);

        // Mock the strategies list
        List<MccStrategy> strategies = Arrays.asList(foodStrategy, cashStrategy);

        // Initialize the service manually
        transactionService = new DistributedLockTransactionService(strategies, redissonClient);

        // Mock the getMccCode() method for strategies
        when(foodStrategy.getMccCode()).thenReturn(Collections.singletonList("5411"));
        when(cashStrategy.getMccCode()).thenReturn(Collections.emptyList());

        lenient().when(foodStrategy.getMccCode()).thenReturn(Collections.singletonList("5411"));
        lenient().when(mealStrategy.getMccCode()).thenReturn(Collections.singletonList("5811"));
        lenient().when(cashStrategy.getMccCode()).thenReturn(Collections.singletonList("9999"));

        Map<String, MccStrategy> strategyMap = new HashMap<>();
        strategyMap.put("5411", foodStrategy);
        strategyMap.put("5811", mealStrategy);
        strategyMap.put("9999", cashStrategy);

        java.lang.reflect.Field field = transactionService.getClass().getDeclaredField("strategyMap");
        field.setAccessible(true);
        field.set(transactionService, strategyMap);
    }

    @Test
    public void testAuthorizeTransaction_Success() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAccountId("123");
        transaction.setAmount(50);
        transaction.setMcc("5411");
        transaction.setMerchant("PADARIA DO ZE SAO PAULO BR");

        when(rLock.tryLock(5, 3, TimeUnit.SECONDS)).thenReturn(true);
        when(foodStrategy.hasAvailableBalance(transaction)).thenReturn(true);
        when(foodStrategy.authorize(transaction)).thenReturn(new TransactionResponseBody(TransactionsStatusCodes.SUCCESS_TRANSACTION));

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.SUCCESS_TRANSACTION, response.getCode());
        verify(foodStrategy, times(1)).authorize(transaction);
        verify(cashStrategy, never()).authorize(transaction);
        verify(rLock, times(1)).unlock();
    }

    @Test
    public void testAuthorizeTransaction_InsufficientBalance_FallbackToCash() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAccountId("123");
        transaction.setAmount(50);
        transaction.setMcc("5411");
        transaction.setMerchant("PADARIA DO ZE SAO PAULO BR");

        when(rLock.tryLock(5, 3, TimeUnit.SECONDS)).thenReturn(true);
        when(foodStrategy.hasAvailableBalance(transaction)).thenReturn(false);
        when(cashStrategy.hasAvailableBalance(transaction)).thenReturn(true);
        when(cashStrategy.authorize(transaction)).thenReturn(new TransactionResponseBody(TransactionsStatusCodes.SUCCESS_TRANSACTION));

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.SUCCESS_TRANSACTION, response.getCode());
        verify(foodStrategy, never()).authorize(transaction);
        verify(cashStrategy, times(1)).authorize(transaction);
        verify(rLock, times(1)).unlock();
    }

    @Test
    public void testAuthorizeTransaction_InsufficientBalance() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAccountId("123");
        transaction.setAmount(50);
        transaction.setMcc("5411");
        transaction.setMerchant("PADARIA DO ZE SAO PAULO BR");

        when(rLock.tryLock(5, 3, TimeUnit.SECONDS)).thenReturn(true);
        when(foodStrategy.hasAvailableBalance(transaction)).thenReturn(false);
        when(cashStrategy.hasAvailableBalance(transaction)).thenReturn(false);

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.INSUFFICIENT_BALANCE, response.getCode());
        verify(foodStrategy, never()).authorize(transaction);
        verify(cashStrategy, never()).authorize(transaction);
        verify(rLock, times(1)).unlock();
    }

    @Test
    public void testAuthorizeTransaction_GenericError() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAccountId("123");
        transaction.setAmount(50);
        transaction.setMcc("5411");

        when(rLock.tryLock(5, 3, TimeUnit.SECONDS)).thenReturn(false);

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.GENERIC_ERROR, response.getCode());
        verify(foodStrategy, never()).authorize(transaction);
        verify(cashStrategy, never()).authorize(transaction);
        verify(rLock, never()).unlock();
    }
}
