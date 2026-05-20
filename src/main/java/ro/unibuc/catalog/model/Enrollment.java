package ro.unibuc.catalog.model;

public class Enrollment implements Printable {

    private int id;
    private int studentId;
    private int courseId;
    private String academicYear;

    public Enrollment(int id, int studentId, int courseId, String academicYear) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.academicYear = academicYear;
    }

    public Enrollment(int studentId, int courseId, String academicYear) {
        this(0, studentId, courseId, academicYear);
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

    public String getAcademicYear() {
        return academicYear;
    }

    @Override
    public String printDetails() {
        return "Enrollment #" + id + " | student " + studentId + " -> course " + courseId
                + " | " + academicYear;
    }
}
