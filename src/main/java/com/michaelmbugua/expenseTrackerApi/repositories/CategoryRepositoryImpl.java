package com.michaelmbugua.expenseTrackerApi.repositories;

import com.michaelmbugua.expenseTrackerApi.domain.Category;
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
public class CategoryRepositoryImpl implements CategoryRepository {

    public static final String SQL_FIND_BY_ID = "SELECT c.category_id, c.user_id, c.title, c.description, " +
            "COALESCE(SUM(t.amount), 0) total_expense " +
            "FROM et_transactions t RIGHT OUTER JOIN et_categories c on c.category_id = t.category_id " +
            "WHERE c.user_id = ? and c.category_id = ? GROUP BY c.category_id";

    public static final String SQL_CREATE = "INSERT INTO et_categories (user_id, title, description) values(?,?,?)";

    public static final String SQL_FIND_ALL = "SELECT c.category_id, c.user_id, c.title, c.description, " +
            "COALESCE(SUM(t.amount), 0) total_expense " +
            "FROM et_transactions t RIGHT OUTER JOIN et_categories c on c.category_id = t.category_id " +
            "WHERE c.user_id = ? GROUP BY c.category_id";

    public static final String SQL_UPDATE = "UPDATE et_categories SET title = ?, description = ? " +
            "WHERE user_id = ? AND category_id = ?";


    @Autowired
    JdbcTemplate jdbcTemplate;

    private RowMapper<Category> categoryRowMapper = ((rs, rowNum) -> new Category(
            rs.getInt("category_id"),
            rs.getInt("user_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getDouble("total_expense")
    ));

    @Override
    public Number create(Integer userId, String title, String description) throws EtBadRequestException {
        try {

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setString(2, title);
                ps.setString(3, description);
                return ps;
            }, keyHolder);

//            System.out.println(keyHolder.getKeys().get("category_id"));
            System.out.println(keyHolder.getKeys());

            return (Number) keyHolder.getKeys().get("GENERATED_KEY");
//            return 14;

        } catch (Exception e) {
            throw new EtBadRequestException("Invalid Request" + e);
        }

    }

    @Override
    public List<Category> findAll(Integer userId) throws EtResourceNotFoundException {
        try {
            return jdbcTemplate.query(SQL_FIND_ALL, new Object[]{userId}, categoryRowMapper);
        } catch (Exception e){
            throw new EtResourceNotFoundException("Categories not found");
        }
    }

    @Override
    public Category findById(Integer userId, Number categoryId) throws EtResourceNotFoundException {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{userId, categoryId}, categoryRowMapper);
        } catch (Exception e) {
            throw new EtResourceNotFoundException("Category not found");
        }
    }

    @Override
    public void update(Integer userId, Integer categoryId, Category category) throws EtBadRequestException {
        try {
            jdbcTemplate.update(SQL_UPDATE, new Object[]{category.getTitle(), category.getDescription(), userId, categoryId});
        }catch (Exception e){
            throw new EtBadRequestException("Invalid request");
        }
    }

    @Override
    public void removeById(Integer userId, Integer categoryId) {

    }

}
