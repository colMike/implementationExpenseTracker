package com.michaelmbugua.expenseTrackerApi.repositories;

import com.michaelmbugua.expenseTrackerApi.domain.Category;
import com.michaelmbugua.expenseTrackerApi.domain.Transaction;
import com.michaelmbugua.expenseTrackerApi.exceptions.EtBadRequestException;
import com.michaelmbugua.expenseTrackerApi.exceptions.EtResourceNotFoundException;

import java.util.List;

public interface TransactionRepository {

    Number create(Integer userId, Integer categoryId, Double amount, String note, Long transactionDate) throws EtBadRequestException;

    List<Transaction> findAll(Integer userId, Integer categoryId) throws EtResourceNotFoundException;

    Transaction findById(Integer userId, Integer categoryId, Number transactionId) throws EtResourceNotFoundException;

    void update(Integer userId, Integer categoryId, Integer transactionId, Transaction transaction) throws EtBadRequestException;

    void removeById(Integer userId, Integer categoryId, Integer transactionId);

}
