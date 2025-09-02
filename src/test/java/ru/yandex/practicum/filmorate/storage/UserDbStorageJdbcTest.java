package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
}
