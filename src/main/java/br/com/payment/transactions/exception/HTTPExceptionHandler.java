package br.com.payment.transactions.exception;

import br.com.payment.transactions.model.TransactionResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static br.com.payment.transactions.utils.TransactionsStatusCodes.GENERIC_ERROR;

@ControllerAdvice
public class HTTPExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<TransactionResponseBody> handleException(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.OK).body(
                TransactionResponseBody.builder()
                        .code(GENERIC_ERROR).build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TransactionResponseBody> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.OK).body(
                TransactionResponseBody.builder()
                        .code(GENERIC_ERROR).build()
        );
    }
}
