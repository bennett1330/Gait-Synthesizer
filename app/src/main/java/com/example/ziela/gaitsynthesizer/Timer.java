package com.example.ziela.gaitsynthesizer;

/**
 * This class acts as a stopwatch, by polling system time on each step event.
 * It compares each pair of consecutive steps with each other, and progresses the user
 * through the musical sequence depending on how regular their ratio is.
 */
public class Timer {
    private static double tolerance = 0.15;

    private static boolean TIMER_IDLE = true;
    private static long startTime;
    private static long[] stepIntervals = {0, 0};
    private static double deviation;

    /**
     * Timer routine called every time a step is detected
     */
    public void onStep() {
        if (TIMER_IDLE) // i.e. there's no active timer we have to stop
            start();
        else {
            stop();
            compareStepIntervals();
            start();
        }
    }

    /**
     * Records start time, and puts down TIMER_IDLE flag
     */
    public void start() {
        startTime = System.currentTimeMillis();
        TIMER_IDLE = false;
    }

    /**
     * Right shifts array contents to make room for new time,
     * then places the new step interval in index zero
     */
    public void stop() { // this is not a memory effienct way of doing this, should be able to
                         // use only one write each time
        stepIntervals[1] = stepIntervals[0]; // shift right to vacate index 0
        stepIntervals[0] = System.currentTimeMillis() - startTime;
        updateTimerDisplays();
    }

    /**
     * Gets the ratio between the current, and previous step intervals,
     * then compares it against a tolerance value.
     * If outside the tolerance, all times are cleared, and the stepcount is reset.
     */
    public void compareStepIntervals() {
        if (bufferFilled()) {
            deviation = (double) stepIntervals[0] / stepIntervals[1];
       //     MainActivity.setDeviationDisplay("Deviation: " +
       //             String.format("%.1f", (deviation * 100) - 100) + "%");
            if (outsideTolerance()) {
                resetMetrics();
            }
        }
    }

    public boolean outsideTolerance() {
        return Math.abs(1 - deviation) > tolerance;
    }
    public boolean bufferFilled() {
        return stepIntervals[1] != 0;
    }

    /**
     * Protocol for handling steps that fall outside of the regularity tolerance
     */
    public void resetMetrics() {
        MainActivity.resetStepCount();
        resetTimerBuffer();
        updateTimerDisplays();
        TIMER_IDLE = true;
    }
    /**
     * Sets both indices back to 0
     */
    public void resetTimerBuffer() {
        stepIntervals[0] = 0;
        stepIntervals[1] = 0;
    }

    public static double getTimer1() {
        return (double) stepIntervals[0];
    }
    public static double getTimer2() {
        return (double) stepIntervals[1];
    }

    /**
     * Updates the TextView instances in MainActivity
     */
    public void updateTimerDisplays() {
       //MainActivity.setTimer1Display("Timer 1:" + stepIntervals[0]);
       //MainActivity.setTimer2Display("Timer 2: " + stepIntervals[1]);
    }
}