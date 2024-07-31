package br.com.payment.transactions.repository;

import br.com.payment.transactions.domain.AccountBalance;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import static br.com.payment.transactions.utils.TransactionsStatusCodes.GENERIC_ERROR;

@Repository
@AllArgsConstructor
public class AccountBalanceRepository {

    private DynamoDBMapper dynamoDBMapper;

    public AccountBalance getAccountBalanceById(String accountId) throws Exception {
        try {
            DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
                    .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build();

            return dynamoDBMapper.load(AccountBalance.class, accountId, config);
        } catch (Exception e) {
            throw new Exception(GENERIC_ERROR);
        }

    }

    public void save(AccountBalance accountBalance) {
        dynamoDBMapper.save(accountBalance);
    }
}
