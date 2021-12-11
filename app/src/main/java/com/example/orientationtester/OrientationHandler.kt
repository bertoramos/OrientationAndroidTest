package com.example.orientationtester

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.concurrent.thread
import kotlin.math.PI

/*
object OrientationHandler : SensorEventListener {

    var orientation_view_model: OrientationViewModel? = null

    var mSensorManager: SensorManager? = null
    var mOrientation: Sensor? = null

    var calibrated = false

    var last_yaw = 0.0
    var yaw_diff = 0.0

    fun init(context: Context, orientation_view_model: OrientationViewModel) {
        this.orientation_view_model = orientation_view_model

        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mOrientation = mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    fun destroy() {
        if(mSensorManager != null && mOrientation != null) {
            mSensorManager = null
            mOrientation = null
        }
    }

    fun start() {
        if(mSensorManager != null && mOrientation != null) {
            mSensorManager!!.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL)
            this.calibrate()
        }
    }

    fun stop() {
        if(mSensorManager != null && mOrientation != null) {
            mSensorManager!!.unregisterListener(this)
        }
    }

    fun calibrate() {
        //var sg = if (last_azi > 180) +1 else -1
        //var degrees = minOf(last_azi, 360-last_azi)
        //azi_diff = sg*degrees
        //yaw_diff = -last_yaw
        calibrated = true

        var sg = if (last_yaw > PI) +1.0 else -1.0
        var degrees = minOf(last_yaw, 2*PI - last_yaw)
        yaw_diff = sg*degrees
    }

    private fun convertToYaw(azimuth: Double): Double {
        return if (azimuth < 0)
            2 * PI + azimuth
        else
            azimuth
    }

    private fun adjustCalibration(yaw: Double): Double {
        var rotationDiff = yaw - last_yaw
        var newYaw = yaw + yaw_diff - rotationDiff

        if(newYaw > 2*PI) {
            newYaw -= 2*PI
        } else if(newYaw < 0) {
            newYaw += 2*PI
        }

        return newYaw
    }

    data class Tilt(val yaw: Double, val pitch: Double, val roll: Double)
    private fun getOrientation(values: FloatArray): Tilt
    {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val tilt = Tilt(
            orientation[0].toDouble(),
            orientation[1].toDouble(),
            orientation[2].toDouble()
        )

        val azimuth = -tilt.yaw
        val pitch = -tilt.pitch
        val roll = tilt.roll

        var yaw = convertToYaw(azimuth)

        var newYaw = yaw
        if(calibrated) {
            newYaw = adjustCalibration(yaw)
        }

        last_yaw = yaw

        return Tilt(newYaw, pitch, roll)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return

        val tilt = getOrientation(values)

        //Log.d("UDPSERVER", " ${tilt.yaw} ; ${tilt.pitch} ; ${tilt.roll}")

        /*
        thread(start=true) {
            ConnectionHandler.sendTrace(
                tilt.yaw,
                tilt.pitch,
                tilt.roll,
            )
        }
        */
        orientation_view_model?.setOrientation(tilt.pitch, tilt.roll, tilt.yaw)
    }

    override fun onAccuracyChanged(event: Sensor?, accuracy: Int) {

    }

}
*/

object OrientationHandler : Compass.CompassListener {

    var orientation_view_model: OrientationViewModel? = null
    var compass: Compass? = null

    override fun onNewOrientation(azimuth: Float, pitch: Float, roll: Float) {
        val az = (360 - azimuth) % 360
        orientation_view_model?.setOrientation(Math.toRadians(pitch.toDouble()), Math.toRadians(roll.toDouble()), Math.toRadians(az.toDouble()))
    }

    override fun onNewAccuracy(geomagneticAccuracy: Int, gravityAccuracy: Int) {
        orientation_view_model?.setAccuracyLevel(geomagneticAccuracy, gravityAccuracy)
    }

    private fun setupCompass(context: Context) {
        compass = Compass(context)
        compass!!.setListener(this)
    }

    fun init(context: Context, orientation_view_model: OrientationViewModel) {
        this.orientation_view_model = orientation_view_model
        setupCompass(context)
    }

    fun destroy() {
        compass?.stop()
    }

    fun start() {
        compass?.start()
    }

    fun stop() {
        compass?.stop()
    }

    fun calibrate() {

    }

}