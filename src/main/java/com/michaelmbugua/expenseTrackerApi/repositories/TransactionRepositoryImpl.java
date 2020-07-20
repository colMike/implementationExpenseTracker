package com.michaelmbugua.expenseTrackerApi.repositories;

import com.michaelmbugua.expenseTrackerApi.domain.Transaction;
import com.michaelmbugua.expenseTrackerApi.exceptions.EtBadRequestException;
import com.michaelmbugua.expenseTrackerApi.exceptions.EtResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class TransactionRepositoryImpl implements TransactionRepository {

    private static final String SQL_CREATE = "INSERT INTO et_transactions (category_id, user_id, amount, note, transaction_date) VALUES(?, ?, ?, ?, ?)";
    private static final String SQL_FIND_ALL = "SELECT transaction_id, category_id, user_id, amount, note, transaction_date FROM et_transactions WHERE user_id = ? AND category_id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT transaction_id, category_id, user_id, amount, note, transaction_date FROM et_transactions WHERE user_id = ? AND category_id = ? AND transaction_id = ?";
    private static final String SQL_UPDATE = "UPDATE et_transactions SET amount = ?, note = ?, transaction_date = ? WHERE user_id = ? and category_id = ? AND transaction_id = ?";
    private static final String SQL_DELETE = "DELETE FROM et_transactions WHERE user_id = ? and category_id = ? AND transaction_id = ?";


    @Autowired
    JdbcTemplate jdbcTemplate;
    private RowMapper<Transaction> transactionRowMapper = ((rs, rowNum) -> {
        return new Transaction(
                rs.getInt("user_id"),
                rs.getInt("category_id"),
                rs.getInt("transaction_id"),
                rs.getDouble("amount"),
                rs.getString("note"),
                rs.getLong("transaction_date")
        );
    });

    @Override
    public Number create(Integer userId, Integer categoryId, Double amount, String note, Long transactionDate) throws EtBadRequestException {
        try {

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, categoryId);
                ps.setInt(2, userId);
                ps.setDouble(3, amount);
                ps.setString(4, note);
                ps.setLong(5, transactionDate);

                return ps;
            }, keyHolder);

            return (Number) keyHolder.getKeys().get("GENERATED_KEY");

        } catch (Exception e) {
            throw new EtBadRequestException("Invalid Request.");
        }

    }

    @Override
    public List<Transaction> findAll(Integer userId, Integer categoryId) throws EtResourceNotFoundException {

        return jdbcTemplate.query(SQL_FIND_ALL, new Object[]{userId, categoryId}, transactionRowMapper);
    }

    @Override
    public Transaction findById(Integer userId, Integer categoryId, Number transactionId) throws EtResourceNotFoundException {
        try {

            return jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{userId, categoryId, transactionId}, transactionRowMapper);

        } catch (Exception e) {
            throw new EtResourceNotFoundException("Transaction not found" + e);
        }
    }

    @Override
    public void update(Integer userId, Integer categoryId, Integer transactionId, Transaction transaction) throws EtBadRequestException {

        System.out.println(userId + ", " + categoryId + ", " + transactionId);
        System.out.println(transaction.getAmount() + ", " + transaction.getNote() + ", " + transaction.getTransactionDate());
        try {
            jdbcTemplate.update(SQL_UPDATE, transaction.getAmount(), transaction.getNote(), transaction.getTransactionDate(), userId, categoryId, transactionId);
//            jdbcTemplate.update(SQL_UPDATE, new Object[]{20000, "Kanote", 146677899, 40, 2, 1}, transactionRowMapper);
        } catch (Exception e) {
            throw new EtBadRequestException("Invalid Request" + e);
        }
    }

    @Override
    public void removeById(Integer userId, Integer categoryId, Integer transactionId) {
        int count = jdbcTemplate.update(SQL_DELETE, new Object[]{userId, categoryId, transactionId});

        if (count == 0 ){
            throw new EtResourceNotFoundException("Transaction could not be found");
        }
    }
}
