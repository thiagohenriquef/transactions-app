#!/bin/bash

echo "Running init-dynamodb.sh script"

# Criando a tabela account-balance
awslocal dynamodb create-table \
    --table-name account-balance \
    --attribute-definitions AttributeName=account_id,AttributeType=S \
    --key-schema AttributeName=account_id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

if [ $? -eq 0 ]; then
    echo "Table created successfully."
else
    echo "Failed to create table."
    exit 1
fi

# Inserindo um item na tabela account-balance
awslocal dynamodb put-item \
    --table-name account-balance \
    --item '{
        "account_id": {"S": "123"},
        "cash_balance": {"N": "100"},
        "food_balance": {"N": "100"},
        "meal_balance": {"N": "100"}
    }'

if [ $? -eq 0 ]; then
    echo "Item inserted successfully."
else
    echo "Failed to insert item."
    exit 1
fi
