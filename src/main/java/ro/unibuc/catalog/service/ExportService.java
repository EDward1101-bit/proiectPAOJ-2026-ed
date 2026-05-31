package ro.unibuc.catalog.service;

import ro.unibuc.catalog.config.AppLogger;
import ro.unibuc.catalog.model.Student;
import ro.unibuc.catalog.repository.StudentRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Exports reports to disk. Students are serialized to CSV and the aggregated
 * statistics (computed by {@link ReportService}) are serialized to JSON.
 *
 * <p>Files are written under the {@code exports/} directory in the project
 * root. The JSON writer is intentionally dependency-free.</p>
 */
public class ExportService {

    private static final Logger LOG = AppLogger.get(ExportService.class);
    private static final Path EXPORT_DIR = Path.of("exports");

    private final StudentRepository students;
    private final ReportService reports;
    private final AuditService audit;

    public ExportService(StudentRepository students, ReportService reports, AuditService audit) {
        this.students = students;
        this.reports = reports;
        this.audit = audit;
    }

    /** Writes all students to {@code exports/students.csv} and returns the path. */
    public Path exportStudentsToCsv() {
        Path file = EXPORT_DIR.resolve("students.csv");
        List<Student> all = students.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("id,first_name,last_name,email,registration_number,status\n");
        for (Student s : all) {
            sb.append(s.getId()).append(',')
                    .append(csv(s.getFirstName())).append(',')
                    .append(csv(s.getLastName())).append(',')
                    .append(csv(s.getEmail())).append(',')
                    .append(csv(s.getRegistrationNumber())).append(',')
                    .append(csv(s.getStatus().name())).append('\n');
        }

        write(file, sb.toString());
        audit.log("EXPORT_STUDENTS_CSV");
        LOG.info(() -> "Exported " + all.size() + " students to " + file.toAbsolutePath());
        return file;
    }

    /** Writes the aggregated statistics report to {@code exports/statistics.json}. */
    public Path exportStatisticsToJson() {
        Path file = EXPORT_DIR.resolve("statistics.json");

        String generatedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"generatedAt\": ").append(jsonString(generatedAt)).append(",\n");
        sb.append("  \"overallGradeAverage\": ").append(number(reports.overallGradeAverage())).append(",\n");
        sb.append("  \"studentCountByStatus\": ").append(longMapToJson(mapKeysToString(reports.studentCountByStatus()))).append(",\n");
        sb.append("  \"coursesPerDepartment\": ").append(longMapToJson(reports.coursesPerDepartment())).append(",\n");
        sb.append("  \"topStudents\": ").append(topStudentsToJson()).append('\n');
        sb.append("}\n");

        write(file, sb.toString());
        audit.log("EXPORT_STATISTICS_JSON");
        LOG.info(() -> "Exported statistics report to " + file.toAbsolutePath());
        return file;
    }

    private String topStudentsToJson() {
        List<ReportService.StudentAverage> top = reports.topStudentsByAverage(5);
        if (top.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < top.size(); i++) {
            ReportService.StudentAverage entry = top.get(i);
            sb.append("    { \"name\": ").append(jsonString(entry.student().getFullName()))
                    .append(", \"weightedAverage\": ").append(number(entry.weightedAverage()))
                    .append(" }");
            sb.append(i < top.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ]");
        return sb.toString();
    }

    private static <K> Map<String, Long> mapKeysToString(Map<K, Long> map) {
        java.util.LinkedHashMap<String, Long> result = new java.util.LinkedHashMap<>();
        map.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }

    private static String longMapToJson(Map<String, Long> map) {
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{ ");
        int i = 0;
        for (Map.Entry<String, Long> e : map.entrySet()) {
            sb.append(jsonString(e.getKey())).append(": ").append(e.getValue());
            if (++i < map.size()) {
                sb.append(", ");
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static void write(Path file, String content) {
        try {
            Files.createDirectories(EXPORT_DIR);
            Files.writeString(file, content);
        } catch (IOException e) {
            LOG.severe("Failed to write export file " + file + ": " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    /** Escapes a value for CSV (RFC 4180): quote when it contains , " or newlines. */
    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        boolean mustQuote = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!mustQuote) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    /** Escapes a value as a JSON string literal (including surrounding quotes). */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
