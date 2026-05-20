package ro.unibuc.catalog.repository;

import ro.unibuc.catalog.config.DatabaseConnection;
import ro.unibuc.catalog.model.Enrollment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentRepository {

    public void create(Enrollment e) {
        String sql = "INSERT INTO enrollments(student_id, course_id, academic_year) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getCourseId());
            ps.setString(3, e.getAcademicYear());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Enrollment> findByCourse(int courseId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    public boolean exists(int studentId, int courseId) {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Enrollment findById(int id) {
        String sql = "SELECT * FROM enrollments WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    public void delete(int id) {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean existsByStudent(int studentId) {
        String sql = "SELECT 1 FROM enrollments WHERE student_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Enrollment map(ResultSet rs) throws SQLException {
        return new Enrollment(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getInt("course_id"),
                rs.getString("academic_year")
        );
    }
}
