package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rn) -> {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setLogin(rs.getString("login"));
        u.setName(rs.getString("name"));
        u.setBirthday(rs.getDate("birthday").toLocalDate());
        return u;
    };

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int updated = jdbc.update(sql, user.getEmail(), user.getLogin(), user.getName(),
                Date.valueOf(user.getBirthday()), user.getId());
        if (updated == 0) throw new NotFoundException("Пользователь не найден: " + user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", USER_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<User> getAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", USER_ROW_MAPPER);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "MERGE INTO friendships (user_id, friend_id, status) " +
                "KEY (user_id, friend_id) VALUES (?, ?, 'PENDING')";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET status = 'CONFIRMED' " +
                "WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        int updated = jdbc.update(sql, userId, friendId);
        if(updated == 0) {
            throw new NotFoundException("Заявка не найдена или уже подтверждена");
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM friendships f " +
                "JOIN users u ON u.id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.id";
        return jdbc.query(sql, USER_ROW_MAPPER, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        String sql = "SELECT u.* FROM friendships f1 " +
                "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                "JOIN users u ON u.id = f1.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "ORDER BY u.id";
        return jdbc.query(sql, USER_ROW_MAPPER, userId, friendId);
    }
}
