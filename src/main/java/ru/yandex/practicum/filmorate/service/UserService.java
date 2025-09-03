package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final @Qualifier("userDbStorage") UserStorage userStorage;

    public User createUser(User user) {
        validateUser(user);
        User created = userStorage.create(user);
        log.info("Создан пользователь: {}", created);
        return created;
    }

    public User updateUser(User user) {
        validateUser(user);
        User updated = userStorage.update(user);
        log.info("Обновлен пользователь: {}", updated);
        return updated;
    }

    public List<User> getAllUsers() {
        log.info("Запрошен список всех пользователей");
        return userStorage.getAll();
    }

    public void addFriend(int id, int friendId) {
        if (id == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        checkUsersExist(id, friendId);

        userStorage.addFriend(id, friendId);
        log.info("Пользователь {} отправил заявку в друзья пользователю {}", id, friendId);
    }

    public void confirmFriend(int id, int friendId) {
        checkUsersExist(id, friendId);

        userStorage.confirmFriend(id, friendId);
        log.info("Пользователь {} подтвердил заявку от пользователя {}", friendId, id);
    }

    public void removeFriend(int id, int friendId) {
        if (id == friendId) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }
        checkUsersExist(id, friendId);

        userStorage.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    }

    public List<User> getFriends(int id) {
        userStorage.getById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int friendId) {
        checkUsersExist(id, friendId);
        return userStorage.getCommonFriends(id, friendId);
    }

    private void validateUser(User user) {
        if (user == null) {
            log.warn("Ошибка валидации тело запроса (user) равно null");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void checkUsersExist(int id, int friendId) {
        userStorage.getById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
        userStorage.getById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + friendId));

    }
}