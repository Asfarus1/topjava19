package ru.javawebinar.topjava.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ru.javawebinar.topjava.util.ValidationUtil.checkNotFound;
import static ru.javawebinar.topjava.util.ValidationUtil.checkNotFoundWithId;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @CacheEvict(value = "users", allEntries = true)
    public User create(User user) {
        Assert.notNull(user, "user must not be null");
        return new User(repository.save(user));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void delete(int id) {
        checkNotFoundWithId(repository.delete(id), id);
    }

    public User get(int id) {
        return new User(checkNotFoundWithId(repository.get(id), id));
    }

    public User getByEmail(String email) {
        Assert.notNull(email, "email must not be null");
        return new User(checkNotFound(repository.getByEmail(email), "email=" + email));
    }

    @Transactional
    public User getWithMeals(int id) {
        User user = repository.get(id);
        User result = new User(user);
        result.setMeals(new ArrayList<>(user.getMeals()));
        return result;
    }

    @Cacheable("users")
    public List<User> getAll() {
        return repository.getAll().stream().map(User::new).collect(toList());
    }

    @CacheEvict(value = "users", allEntries = true)
    public void update(User user) {
        Assert.notNull(user, "user must not be null");
        checkNotFoundWithId(repository.save(user), user.getId());
    }
}