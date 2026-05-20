package ro.unibuc.catalog.model;

import java.time.LocalDate;

public class Attendance implements Printable {

    private int id;
    private int studentId;
    private int courseId;
    private LocalDate date;
    private AttendanceStatus status;

    public Attendance(int id, int studentId, int courseId, LocalDate date, AttendanceStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.date = date;
        this.status = status;
    }

    public Attendance(int studentId, int courseId, LocalDate date, AttendanceStatus status) {
        this(0, studentId, courseId, date, status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public LocalDate getDate() {
        return date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    @Override
    public String printDetails() {
        return "Attendance #" + id + " | student " + studentId + " | course " + courseId
                + " | " + date + " | " + status;
    }
}
