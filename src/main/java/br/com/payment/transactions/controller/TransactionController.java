package br.com.payment.transactions.controller;

import br.com.payment.transactions.domain.Transaction;
import br.com.payment.transactions.model.TransactionResponseBody;
import br.com.payment.transactions.service.DistributedLockTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private DistributedLockTransactionService transactionService;

    @PostMapping("/authorize")
    public TransactionResponseBody processTransaction(@RequestBody Transaction transaction) {
        return transactionService.authorizeTransaction(transaction);
    }
}
