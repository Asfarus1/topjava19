package ru.javawebinar.topjava.repository.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcMealRepository implements MealRepository {
    private static final BeanPropertyRowMapper<Meal> ROW_MAPPER = BeanPropertyRowMapper.newInstance(Meal.class);
    private static final String UPDATE_QUERY = "UPDATE meals SET date_time=:date_time, description=:description, calories=:calories WHERE id=:id AND user_id=:user_id";
    private static final String SELECT_HALF_BETWEEN_QUERY = "SELECT * FROM meals WHERE user_id=:userId AND date_time>=:startDate AND date_time<:endDate ORDER BY date_time DESC";

    private final NamedParameterJdbcTemplate namedTemplate;
    private final SimpleJdbcInsert insert;

    public JdbcMealRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.namedTemplate = jdbcTemplate;
        insert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("meals")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Meal save(Meal meal, int userId) {
        if (meal.isNew()) {
            Number id = insert.executeAndReturnKey(toParameters(meal, userId));
            meal.setId(id.intValue());
        } else {
            namedTemplate.update(UPDATE_QUERY, toParameters(meal, userId));
        }
        return meal;
    }

    @Override
    public boolean delete(int id, int userId) {
        return namedTemplate.getJdbcTemplate().update(
                "DELETE FROM meals WHERE id=? AND user_id=?", id, userId) > 0;
    }

    @Override
    public Meal get(int id, int userId) {
        return namedTemplate.getJdbcTemplate().queryForObject(
                "SELECT * FROM meals WHERE id=? AND user_id=?", ROW_MAPPER, id, userId);
    }

    @Override
    public List<Meal> getAll(int userId) {
        return namedTemplate.getJdbcTemplate().query(
                "SELECT * FROM meals WHERE user_id=? ORDER BY date_time DESC", ROW_MAPPER, userId);
    }

    @Override
    public List<Meal> getBetweenHalfOpen(LocalDateTime startDate, LocalDateTime endDate, int userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        return namedTemplate.query(SELECT_HALF_BETWEEN_QUERY, params, ROW_MAPPER);
    }

    private static Map<String,Object> toParameters(Meal meal, int userId) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> values = mapper.convertValue(meal, Map.class);
        values.put("user_id", userId);
        values.put("date_time", meal.getDateTime());
        return values;
    }
}
