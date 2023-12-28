package tech.c1ph3rj.view.video_kyc;

import android.os.Handler;

public class RecordingTimer {

    private static final long INTERVAL = 1000; // Update interval in milliseconds

    private final Handler handler;
    private boolean isTimerRunning;
    private TimerUpdateListener timerUpdateListener;

    private long elapsedTime;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime += INTERVAL;
            notifyTimerUpdate(elapsedTime);
            handler.postDelayed(this, INTERVAL);
        }
    };

    public RecordingTimer() {
        this.handler = new Handler();
        this.isTimerRunning = false;
        this.elapsedTime = 0;
    }

    public void setTimerUpdateListener(TimerUpdateListener listener) {
        this.timerUpdateListener = listener;
    }

    public void startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            elapsedTime = 0;
            handler.postDelayed(timerRunnable, INTERVAL);
        }
    }

    public void pauseTimer() {
        if (isTimerRunning) {
            isTimerRunning = false;
            handler.removeCallbacks(timerRunnable);
        }
    }

    public void resumeTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            handler.postDelayed(timerRunnable, INTERVAL);
        }
    }

    public void stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false;
            handler.removeCallbacks(timerRunnable);
            elapsedTime = 0;
            timerUpdateListener.onTimerDone();
        }
    }

    private void notifyTimerUpdate(long millisElapsed) {
        if (timerUpdateListener != null) {
            timerUpdateListener.onTimerUpdate(millisElapsed);
        }
    }

    public interface TimerUpdateListener {
        void onTimerUpdate(long millisElapsed);

        void onTimerDone();
    }
}
