package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageJdbcTest {

    private final UserDbStorage userStorage;

    @Test
    void createAndGetById() {
        User u = new User();
        u.setEmail("test@example.com");
        u.setLogin("tester");
        u.setName("Test User");
        u.setBirthday(LocalDate.of(2003, 3, 5));

        User created = userStorage.create(u);

        Optional<User> foundOpt = userStorage.getById(created.getId());

        assertThat(foundOpt)
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isEqualTo(created.getId());
                    assertThat(found.getLogin()).isEqualTo("tester");
                    assertThat(found.getEmail()).isEqualTo("test@example.com");
                });
    }

    @Test
    void update() {
        User u = new User();
        u.setEmail("test@example.com"); u.setLogin("u"); u.setName("U");
        u.setBirthday(LocalDate.of(2003,5,3));
        User created = userStorage.create(u);

        created.setName("B");
        created.setEmail("test@abc.com");
        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("B");
        assertThat(updated.getEmail()).isEqualTo("test@abc.com");

        User u2 = new User(); //Обновление не существующего пользователя
        u2.setId(999);
        u2.setEmail("u2@cba"); u2.setLogin("u2"); u2.setName("U2");
        u2.setBirthday(LocalDate.of(1990,1,1));

        assertThatThrownBy(() -> userStorage.update(u2))
                .isInstanceOf(NotFoundException.class);
    }
}
