package com.github.lobakov.componentcounter.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CCFileReader {

    public static List<String> read(String file) {
        List<String> contents = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contents.add(line);
            }
        } catch (Exception ex) {
            System.out.println("File " + file + " was not found. Or maybe something else :)");
        }
        return contents;
    }
}
