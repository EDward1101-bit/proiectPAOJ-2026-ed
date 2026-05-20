package ro.unibuc.catalog.model;

public class Course implements Printable {

    private int id;
    private String name;
    private String code;
    private int credits;
    private CourseType type;
    private int departmentId;
    private Integer professorId;

    public Course(int id, String name, String code, int credits, CourseType type,
                  int departmentId, Integer professorId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.type = type;
        this.departmentId = departmentId;
        this.professorId = professorId;
    }

    public Course(String name, String code, int credits, CourseType type, int departmentId) {
        this(0, name, code, credits, type, departmentId, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getCredits() {
        return credits;
    }

    public CourseType getType() {
        return type;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public Integer getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Integer professorId) {
        this.professorId = professorId;
    }

    @Override
    public String printDetails() {
        String prof = (professorId == null) ? "no professor" : "prof #" + professorId;
        return "Course #" + id + " | " + code + " - " + name + " | " + credits
                + " credits | " + type + " | " + prof;
    }
}
