package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    private static final RowMapper<Film> FILM_ROW_MAPPER = (rs, rn) -> {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        Date date = rs.getDate("release_date");
        if (date != null) f.setReleaseDate(date.toLocalDate());
        f.setDuration(rs.getInt("duration"));

        Mpa m = new Mpa();
        m.setId(rs.getInt("mpa_id"));
        m.setName(rs.getString("mpa_name"));
        f.setMpa(m);
        return f;
    };

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveGenres(film.getId(), film.getGenres());
        return getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден: " + film.getId()));
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";
        int updated = jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        if (updated == 0) throw new NotFoundException("Фильм не найден: " + film.getId());
        saveGenres(film.getId(), film.getGenres());
        return getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм после обновления не найден: " + film.getId()));
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.name AS mpa_name FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";
        List<Film> films = jdbc.query(sql, FILM_ROW_MAPPER, id);
        if (films.isEmpty()) return Optional.empty();

        Film film = films.get(0);
        Map<Integer, Set<Genre>> genresMap = loadGenres(List.of(film.getId()));
        film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));

        return Optional.of(film);
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "m.name AS mpa_name FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "ORDER BY f.id";

        List<Film> films = jdbc.query(sql, FILM_ROW_MAPPER);
        if (films.isEmpty()) return films;

        List<Integer> ids = films.stream().map(Film::getId).toList();
        Map<Integer, Set<Genre>> byFilm = loadGenres(ids);

        films.forEach(f -> f.setGenres(byFilm.getOrDefault(f.getId(), new LinkedHashSet<>())));
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa m ON m.id = f.mpa_id " +
                "LEFT JOIN film_likes fl ON fl.film_id = f.id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY likes_count DESC, f.id " +
                "LIMIT ?";

        List<Film> films = jdbc.query(sql, FILM_ROW_MAPPER, count);
        if (films.isEmpty()) return films;

        List<Integer> ids = films.stream().map(Film::getId).toList();
        Map<Integer, Set<Genre>> byFilm = loadGenres(ids);

        films.forEach(f -> f.setGenres(byFilm.getOrDefault(f.getId(), new LinkedHashSet<>())));
        return films;
    }


    private Map<Integer, Set<Genre>> loadGenres(Collection<Integer> filmIds) {
        Map<Integer, Set<Genre>> result = new HashMap<>();
        if (filmIds == null || filmIds.isEmpty()) {
            return result;
        }

        NamedParameterJdbcTemplate named = new NamedParameterJdbcTemplate(jdbc);

        String sql = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON g.id = fg.genre_id " +
                "WHERE fg.film_id IN (:ids) " +
                "ORDER BY fg.film_id, g.id";

        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);

        named.query(sql, params, rs -> {
            int filmId = rs.getInt("film_id");

            Genre g = new Genre();
            g.setId(rs.getInt("genre_id"));
            g.setName(rs.getString("genre_name"));

            Set<Genre> set = result.get(filmId);
            if (set == null) {
                set = new LinkedHashSet<>();
                result.put(filmId, set);
            }
            set.add(g);
        });

        return result;
    }

    private void saveGenres(int filmId, Set<Genre> genres) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) return;

        List<Integer> genreIds = genres.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .distinct()
                .sorted()
                .toList();

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Integer genreId : genreIds) {
            jdbc.update(sql, filmId, genreId);
        }
    }
}
