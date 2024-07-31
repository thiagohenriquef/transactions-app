package br.com.payment.transactions.service;

import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.strategy.MccStrategy;
import br.com.payment.transactions.strategy.impl.CashStrategy;
import br.com.payment.transactions.utils.TransactionsStatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {
    private final Map<String, MccStrategy> strategyMap = new HashMap<>();
    private MccStrategy cashStrategy;

    @Autowired
    public TransactionService(List<MccStrategy> strategies) {
        for (MccStrategy strategy : strategies) {
            List<String> mccCodes = strategy.getMccCode();
            for (String mccCode : mccCodes) {
                strategyMap.put(mccCode, strategy);
            }
            if (strategy instanceof CashStrategy) {
                this.cashStrategy = strategy;
            }
        }
    }

    public TransactionResponseBody authorizeTransaction(Transaction transaction) {
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
            return new TransactionResponseBody(TransactionsStatusCodes.INSUFFICIENT_BALANCE);
        }
    }
}
