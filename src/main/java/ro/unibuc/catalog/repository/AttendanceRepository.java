package ro.unibuc.catalog.repository;

import ro.unibuc.catalog.config.DatabaseConnection;
import ro.unibuc.catalog.model.Attendance;
import ro.unibuc.catalog.model.AttendanceStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {

    public void create(Attendance a) {
        String sql = """
                INSERT INTO attendances(student_id, course_id, attendance_date, status)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getCourseId());
            ps.setDate(3, Date.valueOf(a.getDate()));
            ps.setString(4, a.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Attendance> findByCourseAndDate(int courseId, LocalDate date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendances WHERE course_id = ? AND attendance_date = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ps.setDate(2, Date.valueOf(date));
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

    public List<Attendance> findByStudent(int studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendances WHERE student_id = ? ORDER BY attendance_date DESC";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
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

    private Attendance map(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getInt("course_id"),
                rs.getDate("attendance_date").toLocalDate(),
                AttendanceStatus.valueOf(rs.getString("status"))
        );
    }
}
