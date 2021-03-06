package com.nchroniaris.ucinjector.io;

import com.nchroniaris.ucinjector.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for reading/writing to the fake names file. This file includes all the names that the user wants to inject in the usercache file.
 */
public class FakeNamesFile {

    // Relative to the jar file
    private static final String DEFAULT_PATH = Main.findJarWorkingDir() + File.separator + "fakenames.txt";

    private final File file;

    public FakeNamesFile(String filePath) {

        // If the filepath is null, use the default path and validate the file so the following checks don't fail (usually)
        if (filePath == null) {
            filePath = FakeNamesFile.DEFAULT_PATH;
            this.validateDefaultFile();
        }

        this.file = new File(filePath);

        if (!this.file.exists())
            throw new IllegalArgumentException("The file specified does not exist!");

        // By "short circuit", we know the file exists already.
        if (!this.file.isFile())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but it is not a file! Please choose another file path.", file));

        // Check read permissions
        if (!this.file.canRead())
            throw new IllegalArgumentException(String.format("The file path specified (%s) is valid, but does not have correct read permissions! Please use something like `chmod` to change the file permissions.", filePath));

    }

    /**
     * This method validates the file specified by the default location. In particular, it will create a new file if it does not exist. If it exists, it leaves it alone.
     */
    private void validateDefaultFile() {

        File defaultFile = new File(FakeNamesFile.DEFAULT_PATH);

        // If the file does not exist generate one
        if (!defaultFile.exists()) {

            System.out.println("[INFO]: No fake names file was specified, and the default one could not be found, so a new one was created. Please fill out the file with names or else the program will not do anything.");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(defaultFile))) {

                // Write comment and newline
                writer.write("# Every line is one username you want to spoof in the usercache. MC usernames are 3-16 characters, alphanumeric, no spaces, with underscores allowed.");
                writer.newLine();

            } catch (IOException e) {

                // TODO: 2020-11-24 do something useful here
                e.printStackTrace();
                System.exit(1);

            }

        }

    }

    /**
     * Gets all the names from the file specified in the constructor. It is assumed that this file has one username per line. Any lines that start with a `#` will be ignored.
     *
     * @return A <code>List</code> of <code>String</code>s that represent all the (valid) usernames to be injected.
     */
    public List<String> readNames() {

        List<String> nameList = new ArrayList<>();

        // try-with-resources on a new buffered reader.
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {

            // Read all the lines of the file and append them to the nameList array.
            while (true) {

                // Read one line from the file
                String line = reader.readLine();

                // If null, we've reached the end of the file so we break
                if (line == null)
                    break;

                // Trim spaces in front and back in case they were accidentally left in
                line = line.trim();

                // Skip over comments and empty lines
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;

                // https://help.minecraft.net/hc/en-us/articles/360034636712-Minecraft-Usernames
                // Usernames are 3-16 characters, alphanumeric, no spaces, with underscores allowed.
                if (!line.matches("[A-Za-z0-9_]{3,16}")) {
                    System.err.printf("[WARNING]: Ignoring username \"%s\" as it does not adhere to the proper username format. Refer to the README for details.%n", line);
                    continue;
                }

                nameList.add(line);

            }

        } catch (IOException e) {

            // TODO: 2020-11-23 Do something useful here
            e.printStackTrace();

        }

        return nameList;

    }

}
