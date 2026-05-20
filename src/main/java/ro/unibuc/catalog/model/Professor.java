package ro.unibuc.catalog.model;

public class Professor extends Person implements Printable {

    private String title;
    private int departmentId;

    public Professor(int id, String firstName, String lastName, String email,
                     String title, int departmentId) {
        super(id, firstName, lastName, email);
        this.title = title;
        this.departmentId = departmentId;
    }

    public Professor(String firstName, String lastName, String email,
                     String title, int departmentId) {
        this(0, firstName, lastName, email, title, departmentId);
    }

    public String getTitle() {
        return title;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    @Override
    public String printDetails() {
        return "Professor #" + id + " | " + title + " " + getFullName() + " | " + email;
    }
}
