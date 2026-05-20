package ro.unibuc.catalog.service;

import ro.unibuc.catalog.config.AppLogger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AuditService {

    private static final String FILE = "audit.csv";
    private static final Logger LOG = AppLogger.get(AuditService.class);
    private static AuditService instance;

    private AuditService() {
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void log(String action) {
        String timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String line = action + "," + timestamp + System.lineSeparator();
        try (FileWriter fw = new FileWriter(FILE, true)) {
            fw.write(line);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not write audit entry", e);
        }
    }
}
