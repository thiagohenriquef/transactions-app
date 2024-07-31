package br.com.payment.transactions.repository;

import br.com.payment.transactions.domain.AccountBalance;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static br.com.payment.transactions.utils.TransactionsStatusCodes.GENERIC_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountBalanceRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @InjectMocks
    private AccountBalanceRepository accountBalanceRepository;

    private AccountBalance accountBalance;

    @BeforeEach
    public void setUp() {
        accountBalance = new AccountBalance();
        accountBalance.setAccountId("12345");
        accountBalance.setCashBalance(100.0);
        accountBalance.setFoodBalance(50.0);
        accountBalance.setMealBalance(30.0);
    }

    @Test
    public void testGetAccountBalanceById_Success() throws Exception {
        when(dynamoDBMapper.load(eq(AccountBalance.class), eq("12345"), any(DynamoDBMapperConfig.class)))
                .thenReturn(accountBalance);

        AccountBalance result = accountBalanceRepository.getAccountBalanceById("12345");

        assertEquals(accountBalance, result);
        verify(dynamoDBMapper, times(1)).load(eq(AccountBalance.class), eq("12345"), any(DynamoDBMapperConfig.class));
    }

    @Test
    public void testGetAccountBalanceById_Exception() {
        when(dynamoDBMapper.load(eq(AccountBalance.class), eq("12345"), any(DynamoDBMapperConfig.class)))
                .thenThrow(new RuntimeException("DynamoDB error"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountBalanceRepository.getAccountBalanceById("12345");
        });

        assertEquals(GENERIC_ERROR, exception.getMessage());
        verify(dynamoDBMapper, times(1)).load(eq(AccountBalance.class), eq("12345"), any(DynamoDBMapperConfig.class));
    }

    @Test
    public void testSave() {
        doNothing().when(dynamoDBMapper).save(accountBalance);

        accountBalanceRepository.save(accountBalance);

        verify(dynamoDBMapper, times(1)).save(accountBalance);
    }
}
