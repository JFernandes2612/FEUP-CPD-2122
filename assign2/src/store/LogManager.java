package store;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LogManager {

    private static final String logsDir = "/../logs/";
    private final String logFile;

    public LogManager(final String nodeID) {
        final String localDir = System.getProperty("user.dir");
        final String logDir = localDir + logsDir + nodeID;

        final Path logPath = Paths.get(logDir);
        try {
            Files.createDirectories(logPath);
        } catch (final IOException e) {
            System.out.println("Error creating log directory: " + e.getMessage());
            e.printStackTrace();
        }

        this.logFile = localDir + logsDir + nodeID + "/log.txt";
        final File file = new File(this.logFile);
        try {
            file.createNewFile();
        } catch (final IOException e) {
            System.out.println("Error creating log file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void writeToLog(final String logMessage) {
        final List<String> recentLogEntries = get32MostRecentLogMessages();
        final String[] individualLogMessages = logMessage.split("\n");
        for (final String individualMessage : individualLogMessages) {
            if (!recentLogEntries.contains(individualMessage)) {
                try {
                    final FileWriter fileWriter = new FileWriter(this.logFile, true);
                    final BufferedWriter writer = new BufferedWriter(fileWriter);
                    writer.write(individualMessage + "\n");
                    writer.close();
                } catch (final IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> get32MostRecentLogMessages() {
        final File logFile = new File(this.logFile);
        final List<String> log = new ArrayList<>();
        int counter = 0;
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String buf;
            while ((buf = reader.readLine()) != null && counter < 32) {
                log.add(buf);
                counter++;
            }
            reader.close();
            return log;
        } catch (final FileNotFoundException e) {
            System.out.println("Error reading log File: " + e.getMessage());
            e.printStackTrace();
        } catch (final IOException e) {
            System.out.println("Error closing log File: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
