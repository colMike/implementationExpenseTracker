package com.michaelmbugua.expenseTrackerApi.repositories;

import com.michaelmbugua.expenseTrackerApi.domain.User;
import com.michaelmbugua.expenseTrackerApi.exceptions.EtAuthException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String SQL_CREATE = "INSERT INTO et_users (first_name, last_name, email, password) VALUES(?, ?, ?, ?)";
    private static final String SQL_COUNT_BY_EMAIL = "SELECT COUNT(*) FROM et_users WHERE email = ?";
    private static final String SQL_FIND_BY_ID = "SELECT user_id, first_name, last_name, email, password FROM et_users WHERE user_id = ?";
    private static final String SQL_FIND_BY_EMAIL = "SELECT user_id, first_name, last_name, email, password FROM et_users WHERE email = ?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    private RowMapper<User> userRowMapper = ((rs, rowNum) -> {
        return new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("password")
        );
    });

    @Override
    public Number create(String firstName, String lastName, String email, String password) throws EtAuthException {

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, email);
                ps.setString(4, hashedPassword);
                return ps;
            }, keyHolder);

            return (Number) keyHolder.getKeys().get("GENERATED_KEY");

        } catch (Exception e) {
            throw new EtAuthException("Invalid Details. Failed to create account." + e);
        }
    }

    @Override
    public User findByEmailAndPassword(String email, String password) throws EtAuthException {

        try {
            User user = jdbcTemplate.queryForObject(SQL_FIND_BY_EMAIL, new Object[]{email}, userRowMapper);
            assert user != null;
            if (!BCrypt.checkpw(password, user.getPassword()))
                throw new EtAuthException("Invalid email/password");
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new EtAuthException("Invalid email/ password");
        }
    }

    @Override
    public Integer getCountByEmail(String email) {
        return jdbcTemplate.queryForObject(SQL_COUNT_BY_EMAIL, new Object[]{email}, Integer.class);
    }

    @Override
    public User findById(Number userId) {
//        System.out.println(userId);
//        System.out.println((jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{userId}, userRowMapper).toString()));
        return jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{userId}, userRowMapper);
    }
}
