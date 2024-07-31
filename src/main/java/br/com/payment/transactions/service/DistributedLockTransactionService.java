package br.com.payment.transactions.service;

import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.strategy.MccStrategy;
import br.com.payment.transactions.strategy.impl.CashStrategy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static br.com.payment.transactions.utils.TransactionsStatusCodes.GENERIC_ERROR;
import static br.com.payment.transactions.utils.TransactionsStatusCodes.INSUFFICIENT_BALANCE;

@Service
public class DistributedLockTransactionService {
    private final Map<String, MccStrategy> strategyMap = new HashMap<>();
    private MccStrategy cashStrategy;
    private RedissonClient redissonClient;

    @Autowired
    public DistributedLockTransactionService(List<MccStrategy> strategies, RedissonClient redissonClient) {
        for (MccStrategy strategy : strategies) {
            List<String> mccCodes = strategy.getMccCode();
            for (String mccCode : mccCodes) {
                strategyMap.put(mccCode, strategy);
            }
            if (strategy instanceof CashStrategy) {
                this.cashStrategy = strategy;
            }
        }
        this.redissonClient = redissonClient;
    }

    public TransactionResponseBody authorizeTransaction(Transaction transaction) {
        String lockKey = "lock:account:" + transaction.getAccountId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                try {
                    String correctedMcc = MerchantMccMapping.getMccForMerchant(transaction.getMerchant());
                    if (correctedMcc != null) {
                        transaction.setMcc(correctedMcc);
                    }

                    MccStrategy strategy = strategyMap.getOrDefault(transaction.getMcc(), this.cashStrategy);

                    if (strategy.hasAvailableBalance(transaction)) {
                        return strategy.authorize(transaction);
                    } else if (this.cashStrategy.hasAvailableBalance(transaction)) {
                        return cashStrategy.authorize(transaction);
                    } else {
                        return new TransactionResponseBody(INSUFFICIENT_BALANCE);
                    }
                } catch (Exception e) {
                    return new TransactionResponseBody(GENERIC_ERROR);
                }
                finally {
                    lock.unlock();
                }
            } else {
                return new TransactionResponseBody(GENERIC_ERROR);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TransactionResponseBody(GENERIC_ERROR);
        }
    }
}
