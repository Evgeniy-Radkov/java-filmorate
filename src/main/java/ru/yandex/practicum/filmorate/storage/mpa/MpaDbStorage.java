package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Mpa> MPA_ROW_MAPPER = (rs, rn) -> {
        Mpa m = new Mpa();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        return m;
    };

    public List<Mpa> findAll() {
        String sql = "SELECT id, name FROM mpa ORDER BY id";
        return jdbc.query(sql, MPA_ROW_MAPPER);
    }

    public Optional<Mpa> findById(int id) {
        String sql = "SELECT id, name FROM mpa WHERE id = ?";
        return jdbc.query(sql, MPA_ROW_MAPPER, id).stream().findFirst();
    }
}
