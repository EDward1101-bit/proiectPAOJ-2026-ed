package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.InvalidStateException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.ScheduleEntry;
import ro.unibuc.catalog.model.WeekDay;
import ro.unibuc.catalog.repository.ClassroomRepository;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.ScheduleRepository;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleService {

    private final ScheduleRepository schedule;
    private final CourseRepository courses;
    private final ClassroomRepository classrooms;
    private final AuditService audit;

    public ScheduleService(ScheduleRepository schedule,
                           CourseRepository courses,
                           ClassroomRepository classrooms,
                           AuditService audit) {
        this.schedule = schedule;
        this.courses = courses;
        this.classrooms = classrooms;
        this.audit = audit;
    }

    public ScheduleEntry add(int courseId, int classroomId, WeekDay day,
                             LocalTime start, LocalTime end) {
        if (day == null || start == null || end == null) {
            throw new ValidationException("Day and hours are required");
        }
        if (!end.isAfter(start)) {
            throw new InvalidStateException("End hour must be after start hour");
        }
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        if (classrooms.findById(classroomId) == null) {
            throw new EntityNotFoundException("Classroom not found: " + classroomId);
        }

        ScheduleEntry s = new ScheduleEntry(courseId, classroomId, day, start, end);
        schedule.create(s);
        audit.log("ADD_SCHEDULE_ENTRY");
        return s;
    }

    public void delete(int scheduleId) {
        if (schedule.findById(scheduleId) == null) {
            throw new EntityNotFoundException("Schedule entry not found: " + scheduleId);
        }
        schedule.delete(scheduleId);
        audit.log("DELETE_SCHEDULE_ENTRY");
    }

    public List<ScheduleEntry> getByCourse(int courseId) {
        if (courses.findById(courseId) == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        audit.log("LIST_SCHEDULE_BY_COURSE");
        return schedule.findByCourse(courseId);
    }

    public void printFullSchedule() {
        List<ScheduleEntry> entries = schedule.findAll();

        Map<Integer, Course> courseLookup = new HashMap<>();
        for (Course c : courses.findAll()) {
            courseLookup.put(c.getId(), c);
        }

        for (ScheduleEntry e : entries) {
            Course c = courseLookup.get(e.getCourseId());
            String courseLabel = (c == null) ? "?" : c.getCode() + " " + c.getName();
            System.out.println(e.getWeekDay() + " " + e.getStartHour() + "-" + e.getEndHour()
                    + " | room #" + e.getClassroomId() + " | " + courseLabel);
        }
        audit.log("PRINT_FULL_SCHEDULE");
    }
}
