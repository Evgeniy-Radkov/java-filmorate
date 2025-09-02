package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpaService {
    private final MpaDbStorage mpaStorage;

    public List<Mpa> getAll() {
        log.info("Запрошены все MPA");
        return mpaStorage.findAll();
    }

    public Mpa getById(int id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA не найден: " + id));
    }
}
