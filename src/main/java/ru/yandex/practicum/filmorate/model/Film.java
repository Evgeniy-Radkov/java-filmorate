package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.enums.Genre;
import ru.yandex.practicum.filmorate.model.enums.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    private Set<Integer> likes = new HashSet<>();

    private Set<Genre> genres = new HashSet<>();

    @NotNull
    private MpaRating mpa;
}