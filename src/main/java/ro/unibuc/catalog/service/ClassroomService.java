package ro.unibuc.catalog.service;

import ro.unibuc.catalog.exception.EntityNotFoundException;
import ro.unibuc.catalog.exception.ValidationException;
import ro.unibuc.catalog.model.Classroom;
import ro.unibuc.catalog.repository.ClassroomRepository;

import java.util.List;

public class ClassroomService {

    private final ClassroomRepository repository;
    private final AuditService audit;

    public ClassroomService(ClassroomRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public Classroom add(String name, int capacity, String building) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Classroom name is required");
        }
        if (capacity <= 0) {
            throw new ValidationException("Capacity must be positive");
        }
        if (building == null || building.isBlank()) {
            throw new ValidationException("Building is required");
        }
        Classroom c = new Classroom(name.trim(), capacity, building.trim());
        repository.create(c);
        audit.log("ADD_CLASSROOM");
        return c;
    }

    public List<Classroom> getAll() {
        audit.log("LIST_CLASSROOMS");
        return repository.findAll();
    }

    public Classroom getById(int id) {
        Classroom c = repository.findById(id);
        if (c == null) {
            throw new EntityNotFoundException("Classroom not found: " + id);
        }
        return c;
    }
}
