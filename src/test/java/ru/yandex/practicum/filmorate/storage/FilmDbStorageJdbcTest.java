package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageJdbcTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void createAndGetById() {
        Film f = new Film();
        f.setName("F");
        f.setDescription("d");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(100);
        Mpa m = new Mpa(); m.setId(1); f.setMpa(m);

        LinkedHashSet<Genre> gs = new LinkedHashSet<>();
        Genre g2 = new Genre(); g2.setId(2); gs.add(g2);
        Genre g1 = new Genre(); g1.setId(1); gs.add(g1);
        gs.add(g2);
        f.setGenres(gs);

        Film created = filmStorage.create(f);

        Optional<Film> foundOpt = filmStorage.getById(created.getId());
        assertThat(foundOpt).isPresent();
        Film found = foundOpt.get();

        assertThat(found.getGenres()).extracting(Genre::getId)
                .containsExactly(1, 2); // отсортировано и без дублей
    }

    @Test
    void getAll() {
        // A с жанром 1
        Film a = new Film();
        a.setName("A");
        a.setDescription("desc A");
        a.setReleaseDate(LocalDate.of(2000, 1, 1));
        a.setDuration(90);
        Mpa m1 = new Mpa();
        m1.setId(1);
        a.setMpa(m1);
        LinkedHashSet<Genre> genresA = new LinkedHashSet<>();
        Genre g1 = new Genre();
        g1.setId(1);
        genresA.add(g1);
        a.setGenres(genresA);
        a = filmStorage.create(a);

        // B без жанров
        Film b = new Film();
        b.setName("B");
        b.setDescription("desc B");
        b.setReleaseDate(LocalDate.of(2000, 1, 1));
        b.setDuration(80);
        Mpa m2 = new Mpa();
        m2.setId(1);
        b.setMpa(m2);
        b.setGenres(new LinkedHashSet<>());
        b = filmStorage.create(b);

        // C с жанром 2
        Film c = new Film();
        c.setName("C");
        c.setDescription("desc C");
        c.setReleaseDate(LocalDate.of(2000, 1, 1));
        c.setDuration(100);
        Mpa m3 = new Mpa();
        m3.setId(1);
        c.setMpa(m3);
        LinkedHashSet<Genre> genresC = new LinkedHashSet<>();
        Genre g2 = new Genre();
        g2.setId(2);
        genresC.add(g2);
        c.setGenres(genresC);
        c = filmStorage.create(c);

        List<Film> all = filmStorage.getAll();

        final int aId = a.getId();
        final int bId = b.getId();
        final int cId = c.getId();

        Film A = all.stream().filter(x -> x.getId() == aId).findFirst().orElseThrow();
        Film B = all.stream().filter(x -> x.getId() == bId).findFirst().orElseThrow();
        Film C = all.stream().filter(x -> x.getId() == cId).findFirst().orElseThrow();

        assertThat(A.getGenres()).extracting(Genre::getId).containsExactly(1);
        assertThat(B.getGenres()).isNotNull().isEmpty();
        assertThat(C.getGenres()).extracting(Genre::getId).containsExactly(2);
    }

    @Test
    void update() {
        Film f = new Film();
        f.setName("F"); f.setDescription("description");
        f.setReleaseDate(LocalDate.of(2003,3,5)); f.setDuration(110);
        Mpa m = new Mpa(); m.setId(1); f.setMpa(m);
        LinkedHashSet<Genre> gset = new LinkedHashSet<>();
        Genre g1 = new Genre(); g1.setId(1); gset.add(g1);
        f.setGenres(gset);

        Film created = filmStorage.create(f);

        LinkedHashSet<Genre> newSet = new LinkedHashSet<>();
        Genre g3 = new Genre(); g3.setId(3); newSet.add(g3);
        created.setGenres(newSet);

        Film updated = filmStorage.update(created);
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(3);

        Film reloaded = filmStorage.getById(updated.getId()).orElseThrow();
        assertThat(reloaded.getGenres()).extracting(Genre::getId).containsExactly(3);
    }

    @Test
    void popular() {
        User u1 = new User();
        u1.setEmail("u1@test"); u1.setLogin("u1"); u1.setName("u1");
        u1.setBirthday(LocalDate.of(1990,1,1));
        u1 = userStorage.create(u1);

        User u2 = new User();
        u2.setEmail("u2@test"); u2.setLogin("u2"); u2.setName("u2");
        u2.setBirthday(LocalDate.of(1990,1,1));
        u2 = userStorage.create(u2);

        Film f1 = new Film();
        f1.setName("F1"); f1.setDescription("d1");
        f1.setReleaseDate(LocalDate.of(2000,1,1)); f1.setDuration(100);
        Mpa m1 = new Mpa(); m1.setId(1); f1.setMpa(m1);
        f1.setGenres(new LinkedHashSet<>());
        f1 = filmStorage.create(f1);

        Film f2 = new Film();
        f2.setName("F2"); f2.setDescription("d2");
        f2.setReleaseDate(LocalDate.of(2000,1,1)); f2.setDuration(100);
        Mpa m2 = new Mpa(); m2.setId(1); f2.setMpa(m2);
        f2.setGenres(new LinkedHashSet<>());
        f2 = filmStorage.create(f2);

        Film f3 = new Film();
        f3.setName("F3"); f3.setDescription("d3");
        f3.setReleaseDate(LocalDate.of(2000,1,1)); f3.setDuration(100);
        Mpa m3 = new Mpa(); m3.setId(1); f3.setMpa(m3);
        f3.setGenres(new LinkedHashSet<>());
        f3 = filmStorage.create(f3);

        filmStorage.addLike(f1.getId(), u1.getId());
        filmStorage.addLike(f1.getId(), u2.getId());
        filmStorage.addLike(f2.getId(), u1.getId());

        List<Film> top = filmStorage.getPopular(10);
        assertThat(top).extracting(Film::getId)
                .containsSubsequence(f1.getId(), f2.getId(), f3.getId());
    }
}
