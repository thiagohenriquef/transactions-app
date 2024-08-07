package br.com.payment.transactions.strategy.impl;

import br.com.payment.transactions.domain.AccountBalance;
import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.service.AccountBalanceService;
import br.com.payment.transactions.strategy.MccStrategy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static br.com.payment.transactions.utils.TransactionsStatusCodes.INSUFFICIENT_BALANCE;
import static br.com.payment.transactions.utils.TransactionsStatusCodes.SUCCESS_TRANSACTION;
import static java.util.Collections.emptyList;

@Component
@AllArgsConstructor
public class CashStrategy implements MccStrategy {

    private final AccountBalanceService accountBalanceService;

    @Override
    public TransactionResponseBody authorize(Transaction transaction) {
        AccountBalance accountBalance = accountBalanceService.getAccountBalance(transaction);
        double amount = transaction.getAmount();

        if (accountBalance.getCashBalance() >= amount) {
            accountBalance.setCashBalance(accountBalance.getCashBalance() - amount);
            accountBalanceService.saveBalance(accountBalance);
            return new TransactionResponseBody(SUCCESS_TRANSACTION);
        }
        return new TransactionResponseBody(INSUFFICIENT_BALANCE);
    }

    @Override
    public boolean hasAvailableBalance(Transaction transaction) {
        AccountBalance accountBalance = accountBalanceService.getAccountBalance(transaction);
        double amount = transaction.getAmount();

        return accountBalance.getCashBalance() >= amount;
    }

    @Override
    public List<String> getMccCode() {
        return emptyList();
    }
}
