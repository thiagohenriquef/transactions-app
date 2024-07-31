package br.com.payment.transactions.service;

import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.strategy.MccStrategy;
import br.com.payment.transactions.strategy.impl.CashStrategy;
import br.com.payment.transactions.strategy.impl.MealStrategy;
import br.com.payment.transactions.utils.TransactionsStatusCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransactionServiceTest {

    @Mock
    private MccStrategy foodStrategy;

    @Mock
    private CashStrategy cashStrategy;

    @Mock
    private MealStrategy mealStrategy;

    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        transaction = new Transaction();
        transaction.setAccountId("12345");
        transaction.setMcc("5411");
        transaction.setMerchant("PADARIA DO ZE SAO PAULO BR");

        transactionService = new TransactionService(Arrays.asList(foodStrategy, cashStrategy, mealStrategy));

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
    public void testAuthorizeTransaction_Success() {
        when(foodStrategy.hasAvailableBalance(transaction)).thenReturn(true);
        when(foodStrategy.authorize(transaction)).thenReturn(new TransactionResponseBody(TransactionsStatusCodes.SUCCESS_TRANSACTION));

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.SUCCESS_TRANSACTION, response.getCode());
        verify(foodStrategy, times(1)).authorize(transaction);
        verify(cashStrategy, never()).authorize(transaction);
    }

    @Test
    public void testAuthorizeTransaction_InsufficientBalance_FallbackToCash() {
        when(foodStrategy.getMccCode()).thenReturn(Collections.singletonList("5411"));
        when(foodStrategy.hasAvailableBalance(transaction)).thenReturn(false);
        when(cashStrategy.hasAvailableBalance(transaction)).thenReturn(true);
        when(cashStrategy.authorize(transaction)).thenReturn(new TransactionResponseBody(TransactionsStatusCodes.SUCCESS_TRANSACTION));

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.SUCCESS_TRANSACTION, response.getCode());
        verify(foodStrategy, never()).authorize(transaction);
        verify(cashStrategy, times(1)).authorize(transaction);
    }

    @Test
    public void testAuthorizeTransaction_InsufficientBalance() {
        lenient().when(foodStrategy.getMccCode()).thenReturn(Collections.singletonList("5411"));

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.INSUFFICIENT_BALANCE, response.getCode());
        verify(foodStrategy, never()).authorize(transaction);
        verify(cashStrategy, never()).authorize(transaction);
    }

    @Test
    public void testAuthorizeTransaction_CorrectedMcc() {
        transaction.setMcc("9999");

        when(cashStrategy.getMccCode()).thenReturn(Collections.singletonList("9999"));
        when(cashStrategy.hasAvailableBalance(transaction)).thenReturn(true);
        when(cashStrategy.authorize(transaction)).thenReturn(new TransactionResponseBody(TransactionsStatusCodes.SUCCESS_TRANSACTION));

        transactionService = new TransactionService(Collections.singletonList(cashStrategy));

        TransactionResponseBody response = transactionService.authorizeTransaction(transaction);

        assertEquals(TransactionsStatusCodes.SUCCESS_TRANSACTION, response.getCode());
        verify(cashStrategy, times(1)).authorize(transaction);
    }
}
