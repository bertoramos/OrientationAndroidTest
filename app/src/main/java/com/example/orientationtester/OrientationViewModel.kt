package com.example.orientationtester

import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.absoluteValue

class OrientationViewModel : ViewModel() {

    val orientationList = MutableLiveData<ArrayList<ArrayList<Double>>>(ArrayList())
    val orientationStableList = MutableLiveData<ArrayList<ArrayList<Double>>>(ArrayList())

    val timestamp = MutableLiveData<Double>()
    val pitch   = MutableLiveData<Double>()
    val roll    = MutableLiveData<Double>()
    val azimuth = MutableLiveData<Double>()

    val capture_on = MutableLiveData<Boolean>(false)
    val delayTime = MutableLiveData<Double>(2.0)
    val lastTime = MutableLiveData<Double>(-1.0)

    val captureTime = MutableLiveData<Double>(5.0)
    val folderName = MutableLiveData<String>("")
    val captureNumber = MutableLiveData<Int>(0)

    val geomagneticAccuracy = MutableLiveData<Int>(0)
    val gravityAccuracy = MutableLiveData<Int>(0)

    fun setOrientation(pitch: Double, roll: Double, azimuth: Double)
    {
        this.timestamp.postValue(System.currentTimeMillis().toDouble())
        this.pitch.postValue(pitch)
        this.roll.postValue(roll)
        this.azimuth.postValue(azimuth)
    }

    fun setAccuracyLevel(geomagneticAccuracy: Int, gravityAccuracy: Int) {
        this.geomagneticAccuracy.postValue(geomagneticAccuracy)
        this.gravityAccuracy.postValue(gravityAccuracy)
    }

    fun addOrientation()
    {
        if(orientationList.value == null) {
            orientationList.value = ArrayList()
        }

        val orientation = ArrayList<Double>()

        if (pitch.value != null && roll.value != null && azimuth.value != null) {
            orientation.add(timestamp.value!!)
            orientation.add(pitch.value!!)
            orientation.add(roll.value!!)
            orientation.add(azimuth.value!!)

            orientationList.value?.add(orientation)
        }
    }

    fun startCapture()
    {
        orientationList.value = ArrayList()
        orientationStableList.value = ArrayList()

        // Set last time
        lastTime.postValue(System.currentTimeMillis().toDouble())
        capture_on.value = true

    }

    fun capture()
    {
        if(capture_on.value == true) {
            val orientation = ArrayList<Double>()

            if (timestamp.value != null && pitch.value != null && roll.value != null && azimuth.value != null && lastTime.value!! > 0) {
                orientation.add(timestamp.value!!)
                orientation.add(pitch.value!!)
                orientation.add(roll.value!!)
                orientation.add(azimuth.value!!)

                orientationList.value?.add(orientation)

                val delayedTime = abs(System.currentTimeMillis().toDouble() - lastTime.value!!)
                val delayMillis = this.delayTime.value?.times(1000)!!
                //Log.d("CAPTURE", "Time ${timestamp.value!!} :: $delayedTime :: $delayMillis :: ${delayTime.value}")
                if ( delayedTime >= delayMillis) {
                    //Log.d("CAPTURE", "Stable capture added: ${timestamp.value!!} :: $delayedTime :: $delayMillis :: ${delayTime.value}")
                    orientationStableList.value?.add(orientation)
                    lastTime.postValue(System.currentTimeMillis().toDouble())

                    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                }
            }
        }
    }

    fun stopCapture()
    {
        capture_on.value = false
    }

    fun clearOrientations()
    {
        orientationList.value?.clear()
    }

    fun captureLoop()
    {
        val lastTimestamp = 0.0
        val startTime = System.currentTimeMillis()
        while (abs((System.currentTimeMillis()) - startTime) <= captureTime.value!!*1000)
        {
            val lastElem = if(orientationList.value!!.isNotEmpty()) orientationList.value?.last() else null

            if(lastElem != null && timestamp.value != lastElem[0]) addOrientation()
            if(lastElem == null) addOrientation()
        }
    }

}