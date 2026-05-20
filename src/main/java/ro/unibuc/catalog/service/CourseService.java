package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.InvalidStateException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Course;
import ro.unibuc.catalog.model.CourseType;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.ProfessorRepository;

import java.util.Map;
import java.util.TreeMap;

public class CourseService {

    private final CourseRepository courses;
    private final DepartmentRepository departments;
    private final ProfessorRepository professors;
    private final AuditService audit;

    public CourseService(CourseRepository courses,
                         DepartmentRepository departments,
                         ProfessorRepository professors,
                         AuditService audit) {
        this.courses = courses;
        this.departments = departments;
        this.professors = professors;
        this.audit = audit;
    }

    public Course add(String name, String code, int credits, CourseType type, int departmentId) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Course name is required");
        }
        if (code == null || code.isBlank()) {
            throw new ValidationException("Course code is required");
        }
        if (credits <= 0) {
            throw new ValidationException("Credits must be greater than zero");
        }
        if (type == null) {
            throw new ValidationException("Course type is required");
        }
        if (departments.findById(departmentId) == null) {
            throw new EntityNotFoundException("Department not found: " + departmentId);
        }
        Course c = new Course(name.trim(), code.trim().toUpperCase(), credits, type, departmentId);
        courses.create(c);
        audit.log("ADD_COURSE");
        return c;
    }

    public Map<String, Course> getAllSortedByCode() {
        TreeMap<String, Course> sorted = new TreeMap<>();
        for (Course c : courses.findAll()) {
            sorted.put(c.getCode(), c);
        }
        audit.log("LIST_COURSES");
        return sorted;
    }

    public Course getById(int id) {
        Course c = courses.findById(id);
        if (c == null) {
            throw new EntityNotFoundException("Course not found: " + id);
        }
        return c;
    }

    public void assignProfessor(int courseId, int professorId) {
        Course c = courses.findById(courseId);
        if (c == null) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        if (professors.findById(professorId) == null) {
            throw new EntityNotFoundException("Professor not found: " + professorId);
        }
        if (c.getProfessorId() != null && c.getProfessorId() == professorId) {
            throw new InvalidStateException("Professor is already assigned to this course");
        }
        courses.assignProfessor(courseId, professorId);
        audit.log("ASSIGN_PROFESSOR_TO_COURSE");
    }

    public void delete(int id) {
        if (courses.findById(id) == null) {
            throw new EntityNotFoundException("Course not found: " + id);
        }
        courses.delete(id);
        audit.log("DELETE_COURSE");
    }
}
