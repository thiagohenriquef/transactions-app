package br.com.payment.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction  {
    private String id;
    private String accountId;
    private double amount;
    private String merchant;
    private String mcc;
}

