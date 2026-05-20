CREATE TABLE IF NOT EXISTS departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    registration_number VARCHAR(30) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    department_id INT NOT NULL,
    CONSTRAINT fk_student_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS professors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    title VARCHAR(40) NOT NULL,
    department_id INT NOT NULL,
    CONSTRAINT fk_professor_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS classrooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    capacity INT NOT NULL,
    building VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    credits INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    department_id INT NOT NULL,
    professor_id INT NULL,
    CONSTRAINT fk_course_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_course_professor FOREIGN KEY (professor_id) REFERENCES professors(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    academic_year VARCHAR(15) NOT NULL,
    CONSTRAINT uq_enrollment UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS grades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    value DECIMAL(4,2) NOT NULL,
    weight DECIMAL(4,2) NOT NULL,
    evaluation_type VARCHAR(40) NOT NULL,
    CONSTRAINT chk_grade_value CHECK (value >= 1.0 AND value <= 10.0),
    CONSTRAINT chk_grade_weight CHECK (weight > 0.0 AND weight <= 1.0),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendances (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS schedule_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    classroom_id INT NOT NULL,
    week_day VARCHAR(15) NOT NULL,
    start_hour TIME NOT NULL,
    end_hour TIME NOT NULL,
    CONSTRAINT fk_schedule_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);
