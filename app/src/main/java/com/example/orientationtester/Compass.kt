package com.example.orientationtester

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Gravity

class Compass(context: Context) : SensorEventListener {
    interface CompassListener {
        fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float)
        fun onNewAccuracy(geomagneticAccuracy: Int, gravityAccuracy: Int)
    }

    companion object {
        private const val TAG = "Compass"
    }

    private var listener: CompassListener? = null
    private val sensorManager: SensorManager
    private val gsensor: Sensor
    private val msensor: Sensor
    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)

    private var azimuth = 0f
    private var azimuthFix = 0f
    private var pitch = 0f
    private var roll = 0f

    private var macc = 0
    private var gacc = 0

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun start() {
        sensorManager.registerListener(
            this, msensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this, gsensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setAzimuthFix(fix: Float) {
        azimuthFix = fix
    }

    fun resetAzimuthFix() {
        setAzimuthFix(0f)
    }

    fun setListener(l: CompassListener?) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2]

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2]
                // Log.e(TAG, Float.toString(event.values[0]));
            }
            val success = SensorManager.getRotationMatrix(
                R, I, mGravity,
                mGeomagnetic
            )
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                azimuth =
                    Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + azimuthFix + 360) % 360
                pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                pitch = (pitch + 360) % 360
                roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
                roll = (roll + 360) % 360
                // Log.d(TAG, "azimuth (deg): " + azimuth);

                if (listener != null) {
                    listener!!.onNewOrientation(azimuth, pitch, roll)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        when (sensor) {
            msensor -> {
                macc = accuracy
            }
            gsensor -> {
                gacc = accuracy
            }
        }


        val accuracyType = {
                accuracy: Int ->
            when (accuracy) {
                0 -> "unreliable"
                1 -> "low"
                2 -> "medium"
                3 -> "high"
                else -> "unreliable"
            }
        }

        Log.d("ACCURACY", "Magnetic accuracy : ${accuracyType(macc)}")
        Log.d("ACCURACY", "Accelerometer accuracy : ${accuracyType(gacc)}")

        if (listener != null) {
            listener!!.onNewAccuracy(macc, gacc)
        }
    }

}