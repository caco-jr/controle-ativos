package br.com.i42.controledeativos

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    var beaconList = ArrayList<BeaconData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = BeaconAdapter(beaconList) { beaconItem: BeaconData -> beaconItemClicked(beaconItem) }

        recyclerView = findViewById<RecyclerView>(R.id.rv_beacons).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Esse app precisa de permissão de acesso")
                builder.setMessage("Aceite a permissão para que o app possa detectar beacons.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )
                }
                builder.show()
            }
        }

        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .operateTimeout = 5000

        val buttonFindBeacon: Button = findViewById<Button>(R.id.beaconScanner)
        buttonFindBeacon.setOnClickListener {
            findBeacon()
        }
    }

    private fun beaconItemClicked(beaconItem: BeaconData) {
        Toast.makeText(this, "Clicked: ${beaconItem.key}", Toast.LENGTH_LONG).show()

        // Launch second activity, pass part ID as string parameter
        val showDetailActivityIntent = Intent(this, BeaconDetailActivity::class.java)

        showDetailActivityIntent.putExtra(Intent.EXTRA_TEXT, beaconItem.key)
        startActivity(showDetailActivityIntent)

//        BleManager.getInstance().connect(beaconItem.beacon, object : BleGattCallback() {
//            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onStartConnect() {
//                Log.d("s", "==> Iniciando a conexão")
//            }
//
//            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
//                Log.d("beacon", "===> CONECTADO: ${bleDevice.device} | ${bleDevice.timestampNanos} | ${status}")
//            }
//
//            override fun onDisConnected(
//                isActiveDisConnected: Boolean,
//                bleDevice: BleDevice,
//                gatt: BluetoothGatt,
//                status: Int
//            ) {
//            }
//        })
    }

    private fun getDistance(rssi: Int, txPower: Int): Double {
        return Math.pow(10.0, (txPower.toDouble() - rssi) / (10 * 2))
    }

    private fun findBeacon() {
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setScanTimeOut(10000)
            .build()

        BleManager.getInstance().initScanRule(scanRuleConfig)

        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                Log.d("B", "==> onScanStarted: $success")
            }

            override fun onLeScan(bleDevice: BleDevice?) {
            }

            override fun onScanning(bleDevice: BleDevice) {
                val distance: Double = getDistance(bleDevice.rssi, -71)

                val beaconName: String = if (bleDevice.mac == "0E:F3:EE:2A:0D:23") {
                    "Beacon"
                } else {
                    bleDevice.name
                }

                val beacon = BeaconData(
                    bleDevice.mac,
                    bleDevice.key,
                    distance.toString(),
                    beaconName
                )

                Log.d("B", "==> onScanning: $bleDevice")
                Log.d("B", "==> Distancia: ${distance} | Mac: " + bleDevice.mac + " | RSSI: " + bleDevice.rssi)

                beaconList.add(beacon)

                // Atualizando Listagem de Beacon
                viewAdapter.notifyDataSetChanged()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                Log.d("B", "==> onScanFinished: $scanResultList")
            }
        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Teste", "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }

                    builder.show()
                }
                return
            }
        }
    }
}