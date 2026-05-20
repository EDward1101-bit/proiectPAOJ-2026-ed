package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.DeleteNotAllowedException;
import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Professor;
import ro.unibuc.catalog.repository.CourseRepository;
import ro.unibuc.catalog.repository.DepartmentRepository;
import ro.unibuc.catalog.repository.ProfessorRepository;

import java.util.List;

public class ProfessorService {

    private final ProfessorRepository professors;
    private final DepartmentRepository departments;
    private final CourseRepository courses;
    private final AuditService audit;

    public ProfessorService(ProfessorRepository professors,
                            DepartmentRepository departments,
                            CourseRepository courses,
                            AuditService audit) {
        this.professors = professors;
        this.departments = departments;
        this.courses = courses;
        this.audit = audit;
    }

    public Professor add(String firstName, String lastName, String email,
                         String title, int departmentId) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new ValidationException("Professor name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Invalid email");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("Title is required");
        }
        if (departments.findById(departmentId) == null) {
            throw new EntityNotFoundException("Department not found: " + departmentId);
        }
        Professor p = new Professor(firstName.trim(), lastName.trim(), email.trim(),
                title.trim(), departmentId);
        professors.create(p);
        audit.log("ADD_PROFESSOR");
        return p;
    }

    public List<Professor> getAll() {
        audit.log("LIST_PROFESSORS");
        return professors.findAll();
    }

    public Professor getById(int id) {
        Professor p = professors.findById(id);
        if (p == null) {
            throw new EntityNotFoundException("Professor not found: " + id);
        }
        return p;
    }

    public void updateTitle(int id, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new ValidationException("Title is required");
        }
        if (professors.findById(id) == null) {
            throw new EntityNotFoundException("Professor not found: " + id);
        }
        professors.updateTitle(id, newTitle.trim());
        audit.log("UPDATE_PROFESSOR_TITLE");
    }

    public void delete(int id) {
        if (professors.findById(id) == null) {
            throw new EntityNotFoundException("Professor not found: " + id);
        }
        if (courses.existsByProfessor(id)) {
            throw new DeleteNotAllowedException(
                    "Professor " + id + " still teaches courses; reassign them first");
        }
        professors.delete(id);
        audit.log("DELETE_PROFESSOR");
    }
}
