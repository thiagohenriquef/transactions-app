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

@Component
@AllArgsConstructor
public class MealStrategy implements MccStrategy {
    private final AccountBalanceService accountBalanceService;

    @Override
    public TransactionResponseBody authorize(Transaction transaction) {
        AccountBalance accountBalance = accountBalanceService.getAccountBalance(transaction);
        double amount = transaction.getAmount();

        if (accountBalance.getMealBalance() >= amount) {
            accountBalance.setMealBalance(accountBalance.getMealBalance() - amount);
            accountBalanceService.saveBalance(accountBalance);
            return new TransactionResponseBody(SUCCESS_TRANSACTION);
        }
        return new TransactionResponseBody(INSUFFICIENT_BALANCE);
    }

    @Override
    public boolean hasAvailableBalance(Transaction transaction) {
        AccountBalance accountBalance = accountBalanceService.getAccountBalance(transaction);
        double amount = transaction.getAmount();

        return accountBalance.getMealBalance() >= amount;
    }

    @Override
    public List<String> getMccCode() {
        return List.of("5811", "5812");
    }
}
