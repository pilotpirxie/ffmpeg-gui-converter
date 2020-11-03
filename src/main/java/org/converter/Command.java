package org.converter;

import javafx.concurrent.Task;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Command extends Task<String> {
    private String[] cmd;

    public Command(String[] command) {
        cmd = command;
    }

    @Override protected String call() throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        String output = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("progress=end")) return output;
            output += line;
        }

        process.waitFor();
        reader.close();
        process.destroyForcibly();
        return output;
    }
}
