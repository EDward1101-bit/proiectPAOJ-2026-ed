package ro.unibuc.catalog.repository;

import ro.unibuc.catalog.config.DatabaseConnection;
import ro.unibuc.catalog.model.ScheduleEntry;
import ro.unibuc.catalog.model.WeekDay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {

    public void create(ScheduleEntry s) {
        String sql = """
                INSERT INTO schedule_entries(course_id, classroom_id, week_day, start_hour, end_hour)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getCourseId());
            ps.setInt(2, s.getClassroomId());
            ps.setString(3, s.getWeekDay().name());
            ps.setTime(4, Time.valueOf(s.getStartHour()));
            ps.setTime(5, Time.valueOf(s.getEndHour()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ScheduleEntry> findByCourse(int courseId) {
        List<ScheduleEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM schedule_entries WHERE course_id = ? ORDER BY week_day, start_hour";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<ScheduleEntry> findAll() {
        List<ScheduleEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM schedule_entries ORDER BY week_day, start_hour";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public ScheduleEntry findById(int id) {
        String sql = "SELECT * FROM schedule_entries WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void delete(int id) {
        String sql = "DELETE FROM schedule_entries WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsByClassroom(int classroomId) {
        String sql = "SELECT 1 FROM schedule_entries WHERE classroom_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, classroomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ScheduleEntry map(ResultSet rs) throws SQLException {
        return new ScheduleEntry(
                rs.getInt("id"),
                rs.getInt("course_id"),
                rs.getInt("classroom_id"),
                WeekDay.valueOf(rs.getString("week_day")),
                rs.getTime("start_hour").toLocalTime(),
                rs.getTime("end_hour").toLocalTime()
        );
    }
}
