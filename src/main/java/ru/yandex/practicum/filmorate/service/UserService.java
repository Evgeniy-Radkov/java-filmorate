package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserStorage userStorage;

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

        Optional<User> user1 = userStorage.getById(id);
        Optional<User> user2 = userStorage.getById(friendId);

        if (user1.isEmpty() || user2.isEmpty()) {
            throw new NotFoundException("Один или оба пользователя не найдены");
        }

        User u1 = user1.get();
        User u2 = user2.get();
        boolean added1 = u1.getFriends().add(friendId);
        boolean added2 = u2.getFriends().add(id);

        if (added1 && added2) {
            log.info("Пользователь {} и пользователь {} стали друзьями", id, friendId);
        } else {
            log.warn("Пользователь {} и пользователь {} уже были друзьями", id, friendId);
        }
    }

    public void removeFriend(int id, int friendId) {
        if (id == friendId) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        Optional<User> user1 = userStorage.getById(id);
        Optional<User> user2 = userStorage.getById(friendId);

        if (user1.isEmpty() || user2.isEmpty()) {
            throw new NotFoundException("Один или оба пользователя не найдены");
        }

        User u1 = user1.get();
        User u2 = user2.get();
        boolean removed1 = u1.getFriends().remove(friendId);
        boolean removed2 = u2.getFriends().remove(id);

        if (removed1 && removed2) {
            log.info("Пользователь {} и пользователь {} больше не друзья", id, friendId);
        } else {
            log.warn("Пользователь {} и пользователь {} не были друзьями", id, friendId);
        }
    }

    public List<User> getFriends(int id) {
        Optional<User> user = userStorage.getById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        Set<Integer> friendsIds = user.get().getFriends();
        List<User> friendsList = new ArrayList<>();

        for (Integer friendId : friendsIds) {
            userStorage.getById(friendId).ifPresent(friendsList::add);
        }

        return friendsList;
    }

    public List<User> getCommonFriends(int id, int otherId) {
        Optional<User> user1 = userStorage.getById(id);
        Optional<User> user2 = userStorage.getById(otherId);

        if (user1.isEmpty() || user2.isEmpty()) {
            throw new NotFoundException("Один или оба пользователя не найдены");
        }

        Set<Integer> friends1 = user1.get().getFriends();
        Set<Integer> friends2 = user2.get().getFriends();

        Set<Integer> commonFriends = new HashSet<>(friends1);
        commonFriends.retainAll(friends2);
        List<User> commonFriendsList = new ArrayList<>();

        for (Integer friendId : commonFriends) {
            userStorage.getById(friendId).ifPresent(commonFriendsList::add);
        }

        return commonFriendsList;
    }

    private void validateUser(User user) {
        if (user == null) {
            log.warn("Ошибка валидации тело запроса (user) равно null");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            log.warn("Ошибка валидации email: {}", user.getEmail());
            throw new ValidationException("Email не может быть пустым и должен содержать '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации login: {}", user.getLogin());
            throw new ValidationException("Login не может быть пустым и не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации birthday: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
