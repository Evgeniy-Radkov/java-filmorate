package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(MpaDbStorage.class)
class MpaDbStorageJdbcTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void getAll() {
        List<Mpa> all = mpaStorage.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all).allSatisfy(m -> {
            assertThat(m.getId()).isPositive();
            assertThat(m.getName()).isNotBlank();
        });
    }

    @Test
    void getById() {
        Optional<Mpa> g1 = mpaStorage.findById(1);
        assertThat(g1).isPresent();
        assertThat(g1.get().getName()).isNotBlank();

        Optional<Mpa> missing = mpaStorage.findById(999999);
        assertThat(missing).isEmpty();
    }
}
