package br.com.payment.transactions.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountNotFoundException extends RuntimeException {
    private String code;
}
