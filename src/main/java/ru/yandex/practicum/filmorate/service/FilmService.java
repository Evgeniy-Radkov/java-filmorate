package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final @Qualifier("filmDbStorage") FilmStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;

    @Transactional
    public Film createFilm(Film film) {
        validateFilm(film);
        validateMpa(film);
        validateGenre(film);
        Film created = filmStorage.create(film);
        log.info("Добавлен фильм: {}", created);
        return created;
    }

    @Transactional
    public Film updateFilm(Film film) {
        validateFilm(film);
        filmStorage.getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден: " + film.getId()));
        validateMpa(film);
        validateGenre(film);
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
        checkFilmsAndUsersExist(filmId, userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        checkFilmsAndUsersExist(filmId, userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) count = 10;
        log.info("Запрошен топ-{} популярных фильмов", count);
        return filmStorage.getPopular(count);
    }

    private void validateFilm(Film film) {
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Ошибка валидации releaseDate: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
    }

    private void validateMpa(Film film) {
        int mpaId = film.getMpa().getId();
        mpaStorage.findById(mpaId)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA не найден: " + mpaId));
    }

    private void validateGenre(Film film) {
        if (film.getGenres() == null) return;

        film.getGenres().forEach(genre -> {
            int id = genre.getId();
            genreStorage.findById(id)
                    .orElseThrow(() -> new NotFoundException("Жанр не найден: " + id));
        });
    }

    private void checkFilmsAndUsersExist(int filmId, int userId) {
        filmStorage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден: " + filmId));
        userStorage.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }
}