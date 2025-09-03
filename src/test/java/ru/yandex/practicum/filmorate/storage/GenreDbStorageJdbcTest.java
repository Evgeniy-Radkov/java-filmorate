package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(GenreDbStorage.class)
class GenreDbStorageJdbcTest {

    private final GenreDbStorage genreStorage;

    @Test
    void getAll() {
        List<Genre> all = genreStorage.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all).allSatisfy(g -> {
            assertThat(g.getId()).isPositive();
            assertThat(g.getName()).isNotBlank();
        });
    }

    @Test
    void getById() {
        Optional<Genre> g1 = genreStorage.findById(1);
        assertThat(g1).isPresent();
        assertThat(g1.get().getName()).isNotBlank();

        Optional<Genre> missing = genreStorage.findById(999999);
        assertThat(missing).isEmpty();
    }
}
