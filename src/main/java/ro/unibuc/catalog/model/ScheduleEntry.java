package ro.unibuc.catalog.model;

import java.time.LocalTime;

public class ScheduleEntry implements Printable {

    private int id;
    private int courseId;
    private int classroomId;
    private WeekDay weekDay;
    private LocalTime startHour;
    private LocalTime endHour;

    public ScheduleEntry(int id, int courseId, int classroomId, WeekDay weekDay,
                         LocalTime startHour, LocalTime endHour) {
        this.id = id;
        this.courseId = courseId;
        this.classroomId = classroomId;
        this.weekDay = weekDay;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public ScheduleEntry(int courseId, int classroomId, WeekDay weekDay,
                         LocalTime startHour, LocalTime endHour) {
        this(0, courseId, classroomId, weekDay, startHour, endHour);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public int getClassroomId() {
        return classroomId;
    }

    public WeekDay getWeekDay() {
        return weekDay;
    }

    public LocalTime getStartHour() {
        return startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    @Override
    public String printDetails() {
        return "Schedule #" + id + " | course " + courseId + " in classroom " + classroomId
                + " | " + weekDay + " " + startHour + "-" + endHour;
    }
}
