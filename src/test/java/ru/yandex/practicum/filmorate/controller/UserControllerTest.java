package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserController controller;

    @Mock
    private UserService userService;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        controller = new UserController(userService);
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setName("Имя");
        user.setLogin("Логин");
        user.setEmail("fff@gmail.com");
        user.setBirthday(LocalDate.of(2003, 3, 5));
        user.setId(1);

        when(userService.createUser(any(User.class))).thenReturn(user);

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
        user.setId(2);

        when(userService.createUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getName().isBlank()) u.setName(u.getLogin());
            u.setId(2);
            return u;
        });

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

        when(userService.createUser(any(User.class)))
                .thenThrow(new ValidationException("Email не может быть пустым и должен содержать '@'"));

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

        when(userService.createUser(any(User.class)))
                .thenThrow(new ValidationException("Login не может быть пустым и не должен содержать пробелы"));

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

        when(userService.createUser(any(User.class)))
                .thenThrow(new ValidationException("Дата рождения не может быть в будущем"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createUser(user));
        assertTrue(e.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = new User();
        user.setName("Новое имя");
        user.setLogin("newlogin");
        user.setEmail("newfff@gmail.com");
        user.setBirthday(LocalDate.of(2000, 12, 31));
        user.setId(3);

        when(userService.updateUser(any(User.class))).thenReturn(user);

        User result = controller.updateUser(user);

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
        user1.setId(4);

        User user2 = new User();
        user2.setName("Имя2");
        user2.setLogin("user2");
        user2.setEmail("ggg@gmail.com");
        user2.setBirthday(LocalDate.of(2000, 12, 31));
        user2.setId(5);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userService.getAllUsers()).thenReturn(users);

        List<User> returnedUsers = controller.getAllUsers();

        assertEquals(2, returnedUsers.size());
        assertTrue(returnedUsers.contains(user1));
        assertTrue(returnedUsers.contains(user2));
    }

    @Test
    void shouldThrowIfUserIsNull() {
        when(userService.createUser(null)).thenThrow(new ValidationException("Пользователь не может быть null"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createUser(null));
        assertEquals("Пользователь не может быть null", e.getMessage());
    }
}
