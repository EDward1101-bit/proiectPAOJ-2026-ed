package ro.unibuc.catalog.repository;

import ro.unibuc.catalog.config.DatabaseConnection;
import ro.unibuc.catalog.model.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GradeRepository {

    public void create(Grade g) {
        String sql = """
                INSERT INTO grades(student_id, course_id, value, weight, evaluation_type)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getCourseId());
            ps.setDouble(3, g.getValue());
            ps.setDouble(4, g.getWeight());
            ps.setString(5, g.getEvaluationType());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    g.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Grade> findAll() {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grades ORDER BY student_id, course_id";
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

    public List<Grade> findByStudent(int studentId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE student_id = ? ORDER BY course_id";
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

    public List<Grade> findByStudentAndCourse(int studentId, int courseId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
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

    public Grade findById(int id) {
        String sql = "SELECT * FROM grades WHERE id = ?";
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
        String sql = "DELETE FROM grades WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Grade map(ResultSet rs) throws SQLException {
        return new Grade(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getInt("course_id"),
                rs.getDouble("value"),
                rs.getDouble("weight"),
                rs.getString("evaluation_type")
        );
    }
}
