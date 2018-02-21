package com.example.pc_anonymous.aplicacionfinalkotlink


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import com.example.pc_anonymous.aplicacionfinalkotlink.R.id.button
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val logTag = MainActivity::class.java.simpleName
    private val connectRetriesCount = 5L

    private val outputMessagesSubject = PublishSubject.create<OutputMessage>()
    private val connectionDisposable = CompositeDisposable()


    private lateinit var progressBar: ProgressBar

    private lateinit var deviceNameMenuItem: MenuItem
    private lateinit var vccMenuItem: MenuItem

    private lateinit var boton: ImageButton
    private lateinit var boton2: ImageButton
    private lateinit var boton3: ImageButton
    private lateinit var boton4: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)


        progressBar = findViewById(R.id.progressBar)
        boton = findViewById(button)
        boton2 = findViewById(R.id.button2)
        boton3 = findViewById(R.id.button3)
        boton4 = findViewById(R.id.button4)
        boton.setOnClickListener({boton1()})
        boton2.setOnClickListener({boton2()})
        boton3.setOnClickListener({boton3()})
        boton4.setOnClickListener({boton4()})
    }
    fun boton1() {
        outputMessagesSubject.onNext(botonUno)
    }
    fun boton2() {
        outputMessagesSubject.onNext(botonDos)
    }
    fun boton3() {
        outputMessagesSubject.onNext(botonTres)
    }
    fun boton4() {
        outputMessagesSubject.onNext(botonCuatro)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        deviceNameMenuItem = menu.findItem(R.id.menu_main_device_name)
        vccMenuItem = menu.findItem(R.id.menu_main_vcc)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main_connect -> {
                connectionDisposable.clear() // close any existing connection beforehand
                showDevicesDialog { connectTo(it) }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        connectionDisposable.clear()
    }


    private fun showDevicesDialog(listener: (BluetoothDevice) -> Unit) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0)
            return
        }
        val devices = bluetoothAdapter.bondedDevices.sortedBy { it.name }.toTypedArray()
        if (devices.isEmpty()) {
            Toast.makeText(this, R.string.toast_no_paired_devices, Toast.LENGTH_LONG).show()
            return
        }
        showAlertDialog(this) {
            setTitle(R.string.dialog_title_choose_vehicle)
            setCancelable(true)
            setItems(devices.map { it.name }.toTypedArray(), { _, which -> listener(devices[which]) })
        }
    }

    private fun connectTo(device: BluetoothDevice) {
        connectionDisposable.clear() // close any still existing connection
        connectionDisposable.add(
                device.messages(outputMessagesSubject)
                        .subscribeOn(Schedulers.newThread())
                        .retry(connectRetriesCount)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { onInputMessage(it) },
                                {
                                    if (it !is IOException) {
                                        throw RuntimeException(it) // non-IO errors shouldn't be suppressed
                                    }
                                    Log.e(logTag, "Connection to " + device.name + " failed")
                                    Toast.makeText(this, getString(R.string.toast_connection_failed, device.name), Toast.LENGTH_SHORT).show()
                                    progressBar.isIndeterminate = false
                                },
                                { Log.e(logTag, "Connection stream ended") },
                                {
                                    // FIXME: the handler doesn't work on retries.
                                    Toast.makeText(this, getString(R.string.toast_connecting, device.name), Toast.LENGTH_SHORT).show()
                                    progressBar.isIndeterminate = true
                                }
                        )
        )
    }

    private fun onInputMessage(message: InputMessage) {
        Log.d(logTag, "Input message: %s".format(message))
        when (message) {
            is ConnectedMessage -> {
                deviceNameMenuItem.title = message.device_name
                Toast.makeText(this, getString(R.string.toast_connected, message.device_name), Toast.LENGTH_SHORT).show()
                progressBar.isIndeterminate = false
                outputMessagesSubject.onNext(botonTres) // FIXME
            }
            is DeprecatedTelemetryMessage -> vccMenuItem.title = "%.2fV".format(message.vcc)
        }
    }
}
