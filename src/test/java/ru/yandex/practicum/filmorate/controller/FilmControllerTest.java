package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//Нужно ли обновлять существующие и добавлять новые тесты, если сейчас тестируется в постмане?
public class FilmControllerTest {
    private FilmController controller;

    @BeforeEach
    void beforeEach() {
        controller = new FilmController();
    }

    @Test
    void shouldCreateValidFilm() throws Exception {
        Film film = new Film();
        film.setName("Название фильма");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2003, 3, 5));
        film.setDuration(120);

        Film created = controller.createFilm(film);

        assertNotNull(created.getId());
        assertEquals(120, created.getDuration());
        assertEquals("Название фильма", created.getName());
        assertEquals("Описание фильма", created.getDescription());
        assertEquals(LocalDate.of(2003, 3, 5), created.getReleaseDate());
    }

    @Test
    void shouldThrowIfDescriptionTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2010, 10, 10));
        film.setDuration(100);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Описание не может быть длиннее 200 символов"));
    }

    @Test
    void shouldThrowIfNameIsBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Название из пробелов");
        film.setReleaseDate(LocalDate.of(2012, 12,12));
        film.setDuration(100);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void shouldThrowIfReleaseDateIsTooEarly() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Дата релиза раньше 28.12.1895");
        film.setReleaseDate(LocalDate.of(1890, 12, 12));
        film.setDuration(100);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Дата релиза не может быть раньше 28.12.1895"));
    }

    @Test
    void shouldThrowIfDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Отрицательная продолжительность");
        film.setReleaseDate(LocalDate.of(2012, 12, 12));
        film.setDuration(-2);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film film = new Film();
        film.setName("Название фильма");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2003, 3, 5));
        film.setDuration(120);

        Film created = controller.createFilm(film);

        Film updated = new Film();
        updated.setId(created.getId());
        updated.setName("Новое название");
        updated.setDescription("Новое описание");
        updated.setReleaseDate(LocalDate.of(2020, 1, 1));
        updated.setDuration(150);

        Film result = controller.updateFilm(updated);

        assertEquals("Новое название", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentFilm() {
        Film film = new Film();
        film.setId(999);
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 10, 10));
        film.setDuration(120);

        ValidationException e = assertThrows(ValidationException.class, () -> controller.updateFilm(film));
        assertTrue(e.getMessage().contains("Фильм с таким id не найден"));
    }


    @Test
    void shouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("Название фильма1");
        film1.setDescription("Описание фильма1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        controller.createFilm(film1);

        Film film2 = new Film();
        film2.setName("Название фильма2");
        film2.setDescription("Описание фильма2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(130);
        controller.createFilm(film2);

        Film film3 = new Film();
        film3.setName("Название фильма3");
        film3.setDescription("Описание фильма3");
        film3.setReleaseDate(LocalDate.of(2002, 1, 1));
        film3.setDuration(140);
        controller.createFilm(film3);

        List<Film> films = controller.getAllFilms();

        assertNotNull(films);
        assertEquals(3, films.size());
        assertTrue(films.contains(film1));
        assertTrue(films.contains(film2));
        assertTrue(films.contains(film3));
    }

    @Test
    void shouldThrowIfFilmIsNull() {
        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(null));
        assertEquals("Фильм не может быть null", e.getMessage());
    }
}
