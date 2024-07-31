package br.com.payment.transactions.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "account-balance")
public class AccountBalance  {

    @DynamoDBHashKey(attributeName = "account_id")
    private String accountId;

    @DynamoDBAttribute(attributeName = "food_balance")
    private double foodBalance;

    @DynamoDBAttribute(attributeName = "meal_balance")
    private double mealBalance;

    @DynamoDBAttribute(attributeName = "cash_balance")
    private double cashBalance;
}
