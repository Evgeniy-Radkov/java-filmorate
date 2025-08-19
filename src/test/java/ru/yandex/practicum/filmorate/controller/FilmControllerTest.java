package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FilmControllerTest {

    private FilmController controller;

    @Mock
    private FilmService filmService;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        controller = new FilmController(filmService);
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Название фильма");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2003, 3, 5));
        film.setDuration(120);

        Film createdFilm = new Film();
        createdFilm.setId(1);
        createdFilm.setName(film.getName());
        createdFilm.setDescription(film.getDescription());
        createdFilm.setReleaseDate(film.getReleaseDate());
        createdFilm.setDuration(film.getDuration());

        when(filmService.createFilm(film)).thenReturn(createdFilm);

        Film result = controller.createFilm(film);

        assertNotNull(result.getId());
        assertEquals(120, result.getDuration());
        assertEquals("Название фильма", result.getName());
        assertEquals("Описание фильма", result.getDescription());
        assertEquals(LocalDate.of(2003, 3, 5), result.getReleaseDate());

        verify(filmService).createFilm(film);
    }

    @Test
    void shouldThrowIfDescriptionTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2010, 10, 10));
        film.setDuration(100);

        when(filmService.createFilm(film)).thenThrow(new ValidationException("Описание не может быть длиннее 200 символов"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Описание не может быть длиннее 200 символов"));

        verify(filmService).createFilm(film);
    }

    @Test
    void shouldThrowIfNameIsBlank() {
        Film film = new Film();
        film.setName("  ");
        film.setDescription("Название из пробелов");
        film.setReleaseDate(LocalDate.of(2012, 12, 12));
        film.setDuration(100);

        when(filmService.createFilm(film)).thenThrow(new ValidationException("Название фильма не может быть пустым"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Название фильма не может быть пустым"));

        verify(filmService).createFilm(film);
    }

    @Test
    void shouldThrowIfReleaseDateIsTooEarly() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Дата релиза раньше 28.12.1895");
        film.setReleaseDate(LocalDate.of(1890, 12, 12));
        film.setDuration(100);

        when(filmService.createFilm(film)).thenThrow(new ValidationException("Дата релиза не может быть раньше 28.12.1895"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Дата релиза не может быть раньше 28.12.1895"));

        verify(filmService).createFilm(film);
    }

    @Test
    void shouldThrowIfDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Отрицательная продолжительность");
        film.setReleaseDate(LocalDate.of(2012, 12, 12));
        film.setDuration(-2);

        when(filmService.createFilm(film)).thenThrow(new ValidationException("Продолжительность фильма должна быть положительным числом"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertTrue(e.getMessage().contains("Продолжительность фильма должна быть положительным числом"));

        verify(filmService).createFilm(film);
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film filmToUpdate = new Film();
        filmToUpdate.setId(1);
        filmToUpdate.setName("Новое название");
        filmToUpdate.setDescription("Новое описание");
        filmToUpdate.setReleaseDate(LocalDate.of(2020, 1, 1));
        filmToUpdate.setDuration(150);

        when(filmService.updateFilm(filmToUpdate)).thenReturn(filmToUpdate);

        Film result = controller.updateFilm(filmToUpdate);

        assertEquals("Новое название", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());

        verify(filmService).updateFilm(filmToUpdate);
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentFilm() {
        Film film = new Film();
        film.setId(999);
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2010, 10, 10));
        film.setDuration(120);

        when(filmService.updateFilm(film)).thenThrow(new ValidationException("Фильм с таким id не найден"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.updateFilm(film));
        assertTrue(e.getMessage().contains("Фильм с таким id не найден"));

        verify(filmService).updateFilm(film);
    }

    @Test
    void shouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("Название фильма1");
        film1.setDescription("Описание фильма1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Название фильма2");
        film2.setDescription("Описание фильма2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(130);

        Film film3 = new Film();
        film3.setName("Название фильма3");
        film3.setDescription("Описание фильма3");
        film3.setReleaseDate(LocalDate.of(2002, 1, 1));
        film3.setDuration(140);

        List<Film> films = new ArrayList<>();
        films.add(film1);
        films.add(film2);
        films.add(film3);

        when(filmService.getAllFilms()).thenReturn(films);

        List<Film> result = controller.getAllFilms();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(film1));
        assertTrue(result.contains(film2));
        assertTrue(result.contains(film3));

        verify(filmService).getAllFilms();
    }

    @Test
    void shouldThrowIfFilmIsNull() {
        when(filmService.createFilm(null)).thenThrow(new ValidationException("Фильм не может быть null"));

        ValidationException e = assertThrows(ValidationException.class, () -> controller.createFilm(null));
        assertEquals("Фильм не может быть null", e.getMessage());

        verify(filmService).createFilm(null);
    }
}
