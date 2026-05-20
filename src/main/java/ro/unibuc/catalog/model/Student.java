package ro.unibuc.catalog.model;

public class Student extends Person implements Printable {

    private String registrationNumber;
    private StudentStatus status;
    private int departmentId;

    public Student(int id, String firstName, String lastName, String email,
                   String registrationNumber, StudentStatus status, int departmentId) {
        super(id, firstName, lastName, email);
        this.registrationNumber = registrationNumber;
        this.status = status;
        this.departmentId = departmentId;
    }

    public Student(String firstName, String lastName, String email,
                   String registrationNumber, int departmentId) {
        this(0, firstName, lastName, email, registrationNumber, StudentStatus.ACTIVE, departmentId);
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    @Override
    public String printDetails() {
        return "Student #" + id + " | " + getFullName() + " (" + registrationNumber + ") | "
                + email + " | " + status;
    }
}
