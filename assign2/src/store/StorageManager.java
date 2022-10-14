package store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private static final String STORAGE_DIR = "/../storage/";
    private final String ROOT_DIR;

    public StorageManager(String nodeDir) {
        final String localDir = System.getProperty("user.dir");
        this.ROOT_DIR = localDir + STORAGE_DIR + nodeDir;
        try {
            Path path = Paths.get(this.ROOT_DIR);
            Files.createDirectories(path);
            System.out.println("Storage directory has been created");
        } catch (IOException e) {
            System.out.println("Error creating directories" + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean deleteFile(String fileName) {
        File file = new File(this.ROOT_DIR + "/" + fileName);

        if (file.delete()) {
            System.out.println("Deleted file " + fileName);
            return true;
        } else {
            System.out.println("Error deleting file " + fileName);
            return false;
        }
    }

    public String readFile(String fileName) {
        try {
            Path path = Paths.get(this.ROOT_DIR + "/" + fileName);
            byte[] data = Files.readAllBytes(path);
            return new String(data);
        } catch (IOException e) {
            System.out.println("Error reading file " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    public boolean writeFile(String fileName, String fileContents) {
        try {
            Path filePath = Paths.get(this.ROOT_DIR + "/" + fileName);
            Files.write(filePath, fileContents.getBytes());
            System.out.println("Successfully wrote file " + fileName);
            return true;
        } catch (IOException e) {
            System.out.println("Error writing file " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public List<String> getFiles(){
        List<String> fileNameList = new ArrayList<String>();
        File directory = new File(ROOT_DIR);
        for (File file : directory.listFiles()) {
            if(!file.isDirectory()){
                fileNameList.add(file.getName());
            }
        }
        return fileNameList;
    }

}
