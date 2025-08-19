package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        validateFilm(film);
        Film created = filmStorage.create(film);
        log.info("Добавлен фильм: {}", created);
        return created;
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        Film updated = filmStorage.update(film);
        log.info("Обновлен фильм: {}", updated);
        return updated;
    }

    public List<Film> getAllFilms() {
        log.info("Запрошен список всех фильмов");
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        Optional<Film> film = filmStorage.getById(filmId);
        Optional<User> user = userStorage.getById(userId);
        if (film.isEmpty()) {
            log.warn("Фильм {} не существует", filmId);
            throw new NotFoundException("Фильм не найден");
        }
        if (user.isEmpty()) {
            log.warn("Пользователь {} не существует", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        Film f = film.get();

        boolean added = f.getLikes().add(userId);

        if (added) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
        }
    }

    public void removeLike(int filmId, int userId) {
        Optional<Film> film = filmStorage.getById(filmId);
        Optional<User> user = userStorage.getById(userId);
        if (film.isEmpty()) {
            log.warn("Фильм {} не существует", filmId);
            throw new NotFoundException("Фильм не найден");
        }
        if (user.isEmpty()) {
            log.warn("Пользователь {} не существует", userId);
            throw new NotFoundException("Пользователь не найден");
        }

        Film f = film.get();

        boolean removed = f.getLikes().remove(userId);

        if (removed) {
            log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        } else {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = filmStorage.getAll();

        films.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));

        if (count > films.size()) {
            count = films.size();
        }

        log.info("Запрошен топ-{} популярных фильмов", count);
        return films.subList(0, count);
    }

    private void validateFilm(Film film) {
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Ошибка валидации releaseDate: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
    }
}