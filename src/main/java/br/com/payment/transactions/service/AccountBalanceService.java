package br.com.payment.transactions.service;

import br.com.payment.transactions.domain.AccountBalance;
import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.exception.AccountNotFoundException;
import br.com.payment.transactions.repository.AccountBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static br.com.payment.transactions.utils.TransactionsStatusCodes.GENERIC_ERROR;

@Service
public class AccountBalanceService {

    @Autowired
    private AccountBalanceRepository accountRepository;

    public AccountBalance getAccountBalance(Transaction transaction) {
        try {
            AccountBalance account =  accountRepository.getAccountBalanceById(transaction.getAccountId());
            if (account == null) {
                throw new AccountNotFoundException("Account not found");
            }
            return account;
        } catch (AccountNotFoundException e) {
            throw new AccountNotFoundException(GENERIC_ERROR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveBalance(AccountBalance accountBalance) {
        accountRepository.save(accountBalance);
    }
}
