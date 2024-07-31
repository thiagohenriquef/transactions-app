package br.com.payment.transactions.strategy;


import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;

import java.util.List;

public interface MccStrategy {
    TransactionResponseBody authorize(Transaction transaction);
    boolean hasAvailableBalance(Transaction transaction);
    List<String> getMccCode();
}

