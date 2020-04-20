package ru.javawebinar.topjava.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.MealTestData;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static ru.javawebinar.topjava.UserTestData.ADMIN_ID;
import static ru.javawebinar.topjava.UserTestData.USER_ID;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-db.xml"})
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class MealServiceTest {

    @Autowired
    private MealService service;

    @Test
    public void get() {
        Meal expected = MealTestData.MEALS.get(0);
        Meal actual = service.get(expected.getId(), USER_ID);
        MealTestData.assertMatch(expected, actual);
    }

    @Test(expected = NotFoundException.class)
    public void getAlien() {
        Meal expected = MealTestData.MEALS.get(0);
        service.get(expected.getId(), ADMIN_ID);
    }

    @Test(expected = NotFoundException.class)
    public void delete() {
        int id = MealTestData.MEALS.get(0).getId();
        service.delete(id, USER_ID);
        service.get(id, USER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void deleteAlien() {
        int id = MealTestData.MEALS.get(0).getId();
        service.delete(id, ADMIN_ID);
    }

    @Test
    public void getBetweenHalfOpen() {
        List<Meal> expected = MealTestData.MEALS.stream()
                .filter(m -> m.getDate().compareTo(MealTestData.BORDER_DATE_TIME.toLocalDate()) <= 0)
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());

        List<Meal> actual = service.getBetweenHalfOpen(
                null, MealTestData.BORDER_DATE_TIME.toLocalDate(), USER_ID);
        MealTestData.assertMatch(actual, expected);

        Assert.assertEquals(0, service.getBetweenHalfOpen(
                null, null, ADMIN_ID).size());
    }

    @Test
    public void getAll() {
        List<Meal> expected = MealTestData.MEALS.stream()
                .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                .collect(Collectors.toList());

        List<Meal> actual = service.getAll(USER_ID);
        MealTestData.assertMatch(actual, expected);

        Assert.assertEquals(0, service.getAll(ADMIN_ID).size());
    }

    @Test
    public void update() {
        Meal expected = MealTestData.getUpdated();
        service.update(expected, USER_ID);
        Meal actual = service.get(expected.getId(), USER_ID);
        MealTestData.assertMatch(actual, expected);
    }

    @Test(expected = NotFoundException.class)
    public void updateAlien() {
        Meal expected = MealTestData.getUpdated();
        service.update(expected, ADMIN_ID);
    }

    @Test
    public void create() {
        Meal expected = MealTestData.getNew();
        Meal newMeal = service.create(new Meal(expected), USER_ID);
        Integer id = newMeal.getId();
        expected.setId(id);
        MealTestData.assertMatch(newMeal, expected);
        MealTestData.assertMatch(service.get(id, USER_ID), expected);
    }

    @Test(expected = DuplicateKeyException.class)
    public void createWithSameDateTime() {
        Meal meal = MealTestData.getNew();
        meal.setDateTime(MealTestData.BORDER_DATE_TIME);
        service.create(meal, USER_ID);
    }
}