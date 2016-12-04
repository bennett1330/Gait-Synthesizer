package com.example.ziela.gaitsynthesizer;

import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

/**
 * Main app activity.
 * This is where we do all our audio playing and step detecting
 */
public class MainActivity extends AppCompatActivity
                          implements OnTouchListener, SensorEventListener {

    PowerManager.WakeLock wakeLock; // TODO private public?

    private static FrequencyBuffer[] bufferPool = new FrequencyBuffer[8];
    private static double[] scaleFrequencies = new double[8];
    private static final int[] majorScaleSteps = {0, 2, 4, 5, 7, 9, 11, 12};
    private static final int[] minorScaleSteps = {0, 2, 3, 5, 7, 8, 10, 12};

    private Timer timer = new Timer();
    private static int stepCount = 0;
    private static boolean firstStep = true;
    private static int lastStep;

    public static final int ROOT = 0;
    public static final int THIRD = 2;
    public static final int FIFTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //uses java class background changer as its layout instead of an xml layout
        View view = new MainGUI(this);
        setContentView(view);
        view.setOnTouchListener(this);
        int rootNote = InputActivity.getInputNote(); // starting note in scale

        scaleFrequencies = populateScale(rootNote, majorScaleSteps);
        createFrequencyBufferForEachScaleIndex(); //TODO
        initializeStepListener();
        configurePowerManager();
    }
    @Override
    /**
     * Simulated step detect event using button
     */
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            timer.onStep();
            advanceNoteSequence();
        }
        return false;
    }
    /**
     * Trigger timer and advance note sequence on step detection event
     *
     * @param event
     */
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            timer.onStep();
            advanceNoteSequence();
        }
    }
    /**
     * Initialize a listener for the step detector sensor by accessing the sensor manager and using
     * it to register an event listener for the phones default step detector at the sensors fastest
     * sampling speed.
     */
    public void initializeStepListener() {
        SensorManager sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        Sensor stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Required method to extend sensor event listener
    }

    /**
     * Stop previous note, play next one, and increment step count
     */
    public void advanceNoteSequence() {
//        if (stepCount > 8)
//            playChord();
        if (!firstStep)
            bufferPool[lastStep].stop();
        bufferPool[stepCount%8].play();
        lastStep = stepCount%8;
//        stepCountDisplay.setText("Step Detector Detected: " + stepCount);
        stepCount++; // TODO move step count outside music functionality
        firstStep = false; //TODO is this not just stepCount == 0?
    }
    public static void resetStepCount() {
        stepCount = 0;
    }
    public static int getStepCount() {
        return stepCount;
    }

    // TODO move all music to separate class
    public void playChord() {
        bufferPool[ROOT].play();
        bufferPool[THIRD].play();
        bufferPool[FIFTH].play();
    }
    /**
     * Iterates through 8-entry frequency array, populating with
     * scale degrees based on scaleStep array
     */
    public static double[] populateScale(int rootNote, int[] scaleSteps) {
        double[] scaleFrequencies = new double[8]; // wasteful? but i guess killed once we return?
        for (int i = 0; i < 8; i++) {
            scaleFrequencies[i] = midiNoteToFrequency(rootNote + scaleSteps[i]);
        }
        return scaleFrequencies;
    }
    /**
     * Returns frequency from input integer MIDI note
     */
    public static double midiNoteToFrequency(int midiNote) {
        return Math.pow(2, (double) (midiNote - 69) / 12) * 440;
    }
    /**
     * Creates FrequencyBuffer objects for each frequency in scaleFrequencies[],
     * then fills bufferPool[] with these objects.
     */
    public void createFrequencyBufferForEachScaleIndex() {
        for( int i = 0; i < 8; i++ ){
            bufferPool[ i ] = new FrequencyBuffer( scaleFrequencies[ i ] );
        }
    }

    //TODO make this PowerManager extension seperate class?
    /**
     * David, please rename
     */
    public void configurePowerManager() {
        PowerManager mgr = (PowerManager)getSystemService(this.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
    }
    /**
     * David's
     */
    protected void onResume() {
        super.onResume();
        if (wakeLock.isHeld())
            wakeLock.release(); // No longer need to force program to run since screen is on
    }
    /**
     * David's
     */
    protected void onStop() {
        super.onStop();
        if (!wakeLock.isHeld())
            wakeLock.acquire(); // Force program to run when the screen is off
    }
}