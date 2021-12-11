package com.example.orientationtester

import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.orientationtester.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    val FOLDERPICKER_CODE = 1000

    private lateinit var binding: ActivityMainBinding
    private val viewModel: OrientationViewModel by viewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: OrientationRecyclerAdapter

    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        uri ->
            thread(start=true) {
                try {
                    runOnUiThread {
                        findViewById<Button>(R.id.writeButton).isEnabled = false
                    }
                    val stream = contentResolver.openOutputStream(uri)
                    val printer = PrintStream(stream)

                    val orientationList = viewModel.orientationList.value
                    val orientationStableList = viewModel.orientationStableList.value

                    var content = ""

                    val progress = findViewById<ProgressBar>(R.id.writeProgressBar)
                    // progress
                    runOnUiThread {
                        progress.visibility = View.VISIBLE
                        progress.isIndeterminate = false
                        val max = (orientationStableList!!.size + orientationList!!.size)
                        val min = 0
                        progress.max = max

                        progress.progress = 0
                    }

                    content += "position;timestamp;pitch;roll;azimuth\n"
                    if (orientationStableList != null) {
                        for ((pos, orientation) in orientationStableList.withIndex()) {

                            val timestamp = orientation[0]
                            val pitch = orientation[1]
                            val roll = orientation[2]
                            val azimuth = orientation[3]

                            content += "$pos;$timestamp;$pitch;$roll;$azimuth\n"

                            runOnUiThread { progress.incrementProgressBy(1) }
                        }

                    }
/*
                    content += "position;timestamp;pitch;roll;azimuth\n"
                    if (orientationList != null) {
                        for ((pos, orientation) in orientationList.withIndex()) {
                            val timestamp = orientation[0]
                            val pitch = orientation[1]
                            val roll = orientation[2]
                            val azimuth = orientation[3]

                            content += "$pos;$timestamp;$pitch;$roll;$azimuth\n"

                            runOnUiThread { progress.incrementProgressBy(1) }
                        }

                    }
*/
                    runOnUiThread {
                        progress.isIndeterminate = true
                    }

                    printer.println(content)

                    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    toneGen1.release()

                    printer.close()

                    runOnUiThread {
                        progress.visibility = View.INVISIBLE
                        Toast.makeText(applicationContext, "Data saved", Toast.LENGTH_LONG).show()
                        findViewById<Button>(R.id.writeButton).isEnabled = true
                    }

                } catch (e: Exception) {
                    Log.d("URI", e.toString())
                }
            }
    }

    private val selectDirectory = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        uri ->
            //Log.d("CALIBRATION", "$uri")
            //viewModel.writeDirectory.postValue(uri)
    }

    private fun hideNotUsed() {
        findViewById<Button>(R.id.fixAzimuthButton).visibility = View.GONE
        findViewById<EditText>(R.id.delayTime).visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // START ORIENTATION LISTENER
        OrientationHandler.init(applicationContext, viewModel)
        OrientationHandler.start()

        // CREATE ORIENTATIONS LIST
        linearLayoutManager = LinearLayoutManager(this)
        var recyclerView = findViewById<RecyclerView>(R.id.orientationRecyclerView)
        recyclerView.layoutManager = linearLayoutManager
        adapter = OrientationRecyclerAdapter(viewModel)
        recyclerView.adapter = adapter


        // UPDATE CURRENT ORIENTATION
        viewModel.pitch.observe(this, Observer { angle ->
            findViewById<TextView>(R.id.pitchAngleValue).text = String.format("%+.4f rad | %+.2f", angle, Math.toDegrees(angle) )
        })
        viewModel.roll.observe(this, Observer { angle ->
            findViewById<TextView>(R.id.rollAngleValue).text = String.format("%+.4f rad | %+.2f", angle, Math.toDegrees(angle) )
        })
        viewModel.azimuth.observe(this, Observer { angle ->
            findViewById<TextView>(R.id.yawAngleValue).text = String.format("%+.4f rad | %+.2f", angle, Math.toDegrees(angle) )
        })
        viewModel.folderName.observe(this, Observer { folder_name ->
            findViewById<TextView>(R.id.folderNameTextView).text = if(folder_name.isNotEmpty()) folder_name else "No name selected"
        })

        // CALIBRATE BUTTON
        findViewById<Button>(R.id.fixAzimuthButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sure?")
                .setMessage("Do you really want to calibrate?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { _, _ ->
                    OrientationHandler.calibrate()
                    Toast.makeText(applicationContext, "Calibrated", Toast.LENGTH_LONG).show()
                })
                .setNegativeButton(android.R.string.no, null).show()
        }

        // SAVE CURRENT ORIENTATION IN VIEW_MODEL
        findViewById<Button>(R.id.saveOrientationButton).setOnClickListener {
            viewModel.addOrientation()

            adapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(viewModel.orientationList.value?.size?.minus(1) ?: 0)
            Toast.makeText(applicationContext, "New orientation added", Toast.LENGTH_LONG).show()
        }

        // CLEAR ORIENTATION LIST
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sure?")
                .setMessage("Do you really want to clear orientation list?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { _, _ ->
                    viewModel.clearOrientations()

                    adapter.notifyDataSetChanged()
                    Toast.makeText(applicationContext, "List cleared", Toast.LENGTH_LONG).show()
                })
                .setNegativeButton(android.R.string.no, null).show()
        }

        // Write data in file
        findViewById<Button>(R.id.writeButton).setOnClickListener {
            createDocument.launch("data.csv")
        }

        findViewById<EditText>(R.id.delayTime).addTextChangedListener {
            text ->
                val valueStr = text.toString()
                if(valueStr.isNotEmpty()) viewModel.delayTime.postValue(valueStr.toDouble())
        }

        findViewById<ToggleButton>(R.id.captureToggleButton).setOnCheckedChangeListener { _, value ->

            if(value) { // Enciende
                if(!viewModel.capture_on.value!!) {

                    viewModel.startCapture()
                    adapter.notifyDataSetChanged()

                    val job =
                        GlobalScope.launch(Dispatchers.Default) {
                            while (viewModel.capture_on.value!!) {
                                val t = thread(start=true) {  viewModel.capture() }
                                t.join()
                            }
                        }

                    job.start()
                }

            } else { // Apaga
                if(viewModel.capture_on.value!!) {
                    viewModel.stopCapture()
                    adapter.notifyDataSetChanged()
                }
            }

        }

        hideNotUsed()

        findViewById<EditText>(R.id.captureTime).addTextChangedListener {
                text ->
            val valueStr = text.toString()
            if(valueStr.isNotEmpty()) viewModel.captureTime.postValue(valueStr.toDouble())
        }

        findViewById<Button>(R.id.setNameFileButton).setOnClickListener {
            val builder : AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Choose folder name")
            val nameInput = EditText(this)
            nameInput.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(nameInput)

            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                val date = Calendar.getInstance()
                val hour = date.get(Calendar.HOUR_OF_DAY)
                val min = date.get(Calendar.MINUTE)
                val sec = date.get(Calendar.SECOND)
                val day = date.get(Calendar.DAY_OF_MONTH)
                val month = date.get(Calendar.MONTH)
                val year = date.get(Calendar.YEAR)
                val result = "${nameInput.text}_${hour}_${min}_${sec}_${year}_${month}_${day}"

                viewModel.folderName.postValue(result)

                findViewById<Button>(R.id.loopCaptureButton).isEnabled = true
            })
            builder.setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.cancel()
            })

            viewModel.captureNumber.postValue(0)

            builder.show()
        }

        val loopButton = findViewById<Button>(R.id.loopCaptureButton)
        loopButton.setOnClickListener {

            thread(start=true) {
                runOnUiThread{loopButton.isEnabled = false}

                if (!viewModel.folderName.value.isNullOrEmpty() && !viewModel.capture_on.value!!) {

                    Log.d("CAPTURENOTIFY", "CAPTURE START ${viewModel.captureNumber.value}")

                    viewModel.clearOrientations()

                    viewModel.captureLoop()


                    val file = File(
                        getExternalFilesDir(null),
                        "${viewModel.folderName.value}/file${viewModel.captureNumber.value}.csv"
                    )
                    Log.d("CALIBRATION", "--> ${file.toString()} ${viewModel.orientationList.value!!.size}")
                    file.parentFile.mkdirs()
                    file.createNewFile()

                    var content = ""
                    content += "position;timestamp;pitch;roll;azimuth\n"
                    if (viewModel.orientationList != null) {
                        for ((pos, orientation) in viewModel.orientationList.value!!.withIndex()) {
                            val timestamp = orientation[0]
                            val pitch = orientation[1]
                            val roll = orientation[2]
                            val azimuth = orientation[3]

                            content += "$pos;$timestamp;$pitch;$roll;$azimuth\n"
                        }
                    }
                    file.writeText(content)

                    Log.d("CAPTURENOTIFY", "CAPTURE STOP ${viewModel.captureNumber.value}")

                    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    toneGen1.release()

                    runOnUiThread {
                        viewModel.captureNumber.value = viewModel.captureNumber.value?.plus(1)
                    }

                }

                runOnUiThread{loopButton.isEnabled = true}
            }
        }

        findViewById<Button>(R.id.calibrateButton).setOnClickListener {
            CalibrateDialog(viewModel).show(supportFragmentManager, "CalibrateDialog")
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        OrientationHandler.stop()
        OrientationHandler.destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == FOLDERPICKER_CODE && resultCode == RESULT_OK) {
            val folderLocation = intent?.extras!!.getString("data")
            Log.d("folderLocation", folderLocation!!)
            findViewById<Button>(R.id.loopCaptureButton).isEnabled = true
        }
    }

}