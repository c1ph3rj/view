package tech.c1ph3rj.view;

import android.os.Handler;

public class Timer {
    private final Handler handler = new Handler();
    private boolean isRunning = false;
    private long startTimeMillis;

    // Start the timer and return true if it's not already running
    public boolean startTimer() {
        if (!isRunning) {
            startTimeMillis = System.currentTimeMillis();
            isRunning = true;
            return true;
        }
        return false; // Timer was already running
    }

    // Stop the timer and return the elapsed time in the "Xs" format
    public String stopTimer() {
        if (isRunning) {
            long stopTimeMillis = System.currentTimeMillis();
            isRunning = false;
            long elapsedTimeMillis = stopTimeMillis - startTimeMillis;
            long elapsedTimeInSeconds = elapsedTimeMillis / 1000; // Convert milliseconds to seconds
            return "(" + elapsedTimeInSeconds + "s)";
        }
        return "0s"; // Timer was not running
    }
}
