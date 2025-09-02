package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Genre> GENRE_ROW_MAPPER = (rs, rn) -> {
        Genre g = new Genre();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        return g;
    };

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbc.query(sql, GENRE_ROW_MAPPER);
    }

    public Optional<Genre> findById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        return jdbc.query(sql, GENRE_ROW_MAPPER, id).stream().findFirst();
    }
}
