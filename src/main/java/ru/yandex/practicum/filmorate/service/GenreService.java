package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreDbStorage genreStorage;

    public List<Genre> getAll() {
        log.info("Запрошены все жанры");
        return genreStorage.findAll();
    }

    public Genre getById(int id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр не найден: " + id));
    }
}
