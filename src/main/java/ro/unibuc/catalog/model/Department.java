package ro.unibuc.catalog.model;

public class Department implements Printable {

    private int id;
    private String name;
    private String code;

    public Department(int id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Department(String name, String code) {
        this(0, name, code);
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

    @Override
    public String printDetails() {
        return "Department #" + id + " | " + code + " - " + name;
    }
}
