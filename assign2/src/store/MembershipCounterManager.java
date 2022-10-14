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

public class MembershipCounterManager {
    private static final String countersDir = "/../membership/";
    private final String counterFile;

    public MembershipCounterManager(final String nodeID) {
        final String localDir = System.getProperty("user.dir");
        final String counterDir = localDir + countersDir + nodeID;

        final Path counterPath = Paths.get(counterDir);
        try {
            Files.createDirectories(counterPath);
        } catch (final IOException e) {
            System.out.println("Error creating membership directory: " + e.getMessage());
            e.printStackTrace();
        }

        this.counterFile = localDir + countersDir + nodeID + "/counter";

        final File file = new File(this.counterFile);
        try {
            file.createNewFile();
        } catch (final IOException e) {
            System.out.println("Error creating counter file: " + e.getMessage());
            e.printStackTrace();
        }

        if (!this.hasContents()) {
            final int counter = -1;
            writeCounter(counter);
        }
    }

    private boolean hasContents() {
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(counterFile));
            boolean hasContents;
            String buf;
            buf = reader.readLine();
            if (buf != null) {
                hasContents = true;
            } else {
                hasContents = false;
            }
            reader.close();
            return hasContents;
        } catch (final FileNotFoundException e) {
            System.out.println("Error reading membership File: " + e.getMessage());
            e.printStackTrace();
        } catch (final IOException e) {
            System.out.println("Error closing membership File: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void writeCounter(int value) {
        try {
            final FileWriter fileWriter = new FileWriter(this.counterFile);
            final BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write(Integer.toString(value) + "\n");
            writer.close();
        } catch (final IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void incrementMembershipCounter() {
        int currentCounter = getMembershipCounter();
        currentCounter++;
        writeCounter(currentCounter);
    }

    public int getMembershipCounter() {
        String membershipCounter = "";
        final File counterFile = new File(this.counterFile);
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(counterFile));
            String buf;
            while ((buf = reader.readLine()) != null) {
                membershipCounter += buf;
            }
            reader.close();
            return Integer.parseInt(membershipCounter);
        } catch (final FileNotFoundException e) {
            System.out.println("Error reading membership File: " + e.getMessage());
            e.printStackTrace();
        } catch (final IOException e) {
            System.out.println("Error closing membership File: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}
