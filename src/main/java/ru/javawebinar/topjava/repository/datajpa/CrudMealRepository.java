package ru.javawebinar.topjava.repository.datajpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.util.List;

public interface CrudMealRepository extends JpaRepository<Meal, Integer> {
    Meal findByIdAndUserId(Integer id, Integer userId);

    List<Meal> findAllByUserIdAndDateTimeGreaterThanEqualAndDateTimeLessThan(
            Integer userId, LocalDateTime startDateTime, LocalDateTime endDateTime, Sort sort);

    List<Meal> findAllByUserId(int userId, Sort sort);
}
