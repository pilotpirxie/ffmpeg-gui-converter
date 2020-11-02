package org.converter;

import java.io.IOException;
import java.util.Arrays;

public class Command implements Runnable  {
    private String[] cmd;
    private boolean finished = false;

    public Command(String[] command) {
        cmd = command;
    }

    @Override
    public void run() {
        System.out.println(Arrays.toString(cmd));
        try {
            finished = false;
            Runtime.getRuntime().exec(cmd);
            finished = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
