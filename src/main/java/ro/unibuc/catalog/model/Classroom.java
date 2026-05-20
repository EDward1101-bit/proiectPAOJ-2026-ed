package ro.unibuc.catalog.model;

public class Classroom implements Printable {

    private int id;
    private String name;
    private int capacity;
    private String building;

    public Classroom(int id, String name, int capacity, String building) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.building = building;
    }

    public Classroom(String name, int capacity, String building) {
        this(0, name, capacity, building);
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

    public int getCapacity() {
        return capacity;
    }

    public String getBuilding() {
        return building;
    }

    @Override
    public String printDetails() {
        return "Classroom #" + id + " | " + building + " " + name + " | " + capacity + " seats";
    }
}
