package ro.unibuc.catalog.model;

public class Grade implements Printable {

    private int id;
    private int studentId;
    private int courseId;
    private double value;
    private double weight;
    private String evaluationType;

    public Grade(int id, int studentId, int courseId, double value, double weight, String evaluationType) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.value = value;
        this.weight = weight;
        this.evaluationType = evaluationType;
    }

    public Grade(int studentId, int courseId, double value, double weight, String evaluationType) {
        this(0, studentId, courseId, value, weight, evaluationType);
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

    public double getValue() {
        return value;
    }

    public double getWeight() {
        return weight;
    }

    public String getEvaluationType() {
        return evaluationType;
    }

    @Override
    public String printDetails() {
        return "Grade #" + id + " | student " + studentId + " | course " + courseId
                + " | " + value + " (w=" + weight + ") | " + evaluationType;
    }
}
