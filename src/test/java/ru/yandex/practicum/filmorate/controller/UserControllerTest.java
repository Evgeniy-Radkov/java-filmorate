package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void beforeEach() {
        controller = new UserController();
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setName("Имя");
        user.setLogin("Логин");
        user.setEmail("fff@gmail.com");
        user.setBirthday(LocalDate.of(2003, 3, 5));

        User createUser = controller.createUser(user);

        assertNotNull(createUser.getId());
        assertEquals("Имя", createUser.getName());
        assertEquals("Логин", createUser.getLogin());
        assertEquals("fff@gmail.com", createUser.getEmail());
        assertEquals(LocalDate.of(2003, 3, 5), createUser.getBirthday());

    }

    @Test
    void shouldSetLoginAsNameIfNameIsBlank() {
        User user = new User();
        user.setName("   ");
        user.setLogin("Имя1");
        user.setEmail("fff@gmail.com");
        user.setBirthday(LocalDate.of(2003, 3, 5));

        User created = controller.createUser(user);

        assertEquals("Имя1", created.getName());
    }

    @Test
    void shouldThrowIfEmailIsInvalid() {
        User user = new User();
        user.setName("Имя");
        user.setLogin("Имя2");
        user.setEmail("fff.gmail.com");
        user.setBirthday(LocalDate.of(2003, 3, 5));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(e.getMessage().contains("Email не может быть пустым и должен содержать '@'"));
    }

    @Test
    void shouldThrowIfLoginIsBlankOrWithSpaces() {
        User user = new User();
        user.setName("Имя");
        user.setLogin("Логин с пробелами");
        user.setEmail("fff@gmail.com");
        user.setBirthday(LocalDate.of(2003, 3, 5));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(e.getMessage().contains("Login не может быть пустым и не должен содержать пробелы"));
    }

    @Test
    void shouldThrowIfBirthdayInFuture() {
        User user = new User();
        user.setName("Имя");
        user.setLogin("Имя2");
        user.setEmail("fff@gmail.com");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(e.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = new User();
        user.setName("Имя");
        user.setLogin("Имя2");
        user.setEmail("fff@gmail.com");
        user.setBirthday(LocalDate.of(2003, 3, 5));

        User created = controller.createUser(user);

        User updated = new User();
        updated.setId(created.getId());
        updated.setName("Новое имя");
        updated.setLogin("newlogin");
        updated.setEmail("newfff@gmail.com");
        updated.setBirthday(LocalDate.of(2000, 12, 31));

        User result = controller.updateUser(updated);

        assertEquals("newlogin", result.getLogin());
        assertEquals("newfff@gmail.com", result.getEmail());
        assertEquals("Новое имя", result.getName());
        assertEquals(LocalDate.of(2000, 12, 31), result.getBirthday());
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User();
        user1.setName("Имя1");
        user1.setLogin("user1");
        user1.setEmail("fff@gmail.com");
        user1.setBirthday(LocalDate.of(2003, 3, 5));
        controller.createUser(user1);

        User user2 = new User();
        user2.setName("Имя2");
        user2.setLogin("user2");
        user2.setEmail("ggg@gmail.com");
        user2.setBirthday(LocalDate.of(2000, 12, 31));
        controller.createUser(user2);

        List<User> users = controller.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    void shouldThrowIfUserIsNull() {
        ValidationException e = assertThrows(ValidationException.class, () -> controller.createUser(null));
        assertEquals("Пользователь не может быть null", e.getMessage());
    }
}
