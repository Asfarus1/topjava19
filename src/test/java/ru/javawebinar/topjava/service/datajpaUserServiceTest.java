package ru.javawebinar.topjava.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.MealTestData;
import ru.javawebinar.topjava.Profiles;
import ru.javawebinar.topjava.model.User;

import static ru.javawebinar.topjava.UserTestData.*;

@ActiveProfiles(Profiles.DATAJPA)
public class datajpaUserServiceTest extends UserServiceTest{

    @Test
    public void testGetWithMeals() {
        User user = service.getWithMeals(USER_ID);
        Assertions.assertThat(user.getMeals()).usingDefaultElementComparator().isEqualTo(MealTestData.MEALS);
    }
}
