package br.com.i42.controledeativos

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.android.synthetic.main.activity_find_beacon.*



class FindBeaconActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    var beaconList = ArrayList<BeaconData>()

    var isScanning = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_beacon)
        setSupportActionBar(toolbar)

        handleFindBeacon()

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

        if (BleManager.getInstance().isBlueEnable) {
            initBle()

        } else {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 0x01)

            initBle()
        }

    }

    private fun initBle() {
        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .operateTimeout = 5000
    }

    private fun beaconItemClicked(beaconItem: BeaconData) {
        Toast.makeText(this, "Clicked: ${beaconItem.key}", Toast.LENGTH_LONG).show()

        // Launch second activity, pass part ID as string parameter
        val showDetailActivityIntent = Intent(this, BeaconDetailActivity::class.java)

        showDetailActivityIntent.putExtra(Intent.EXTRA_TEXT, beaconItem.mac)
        startActivity(showDetailActivityIntent)
    }

    private fun getDistance(rssi: Int, txPower: Int): Double {
        val number: Double = Math.pow(10.0, (txPower.toDouble() - rssi) / (10 * 2))
        val number3digits: Double = String.format("%.3f", number).toDouble()

        val solution: Double = String.format("%.2f", number3digits).toDouble()

        return solution
    }

    private fun handleFindBeacon() {
        var feedbackMessage: String

        fab.setOnClickListener { view ->
            if (isScanning) {
                feedbackMessage = "Parando busca"
                findBeaconCancel()
            } else {
                feedbackMessage = "Buscando Beacons"
                findBeacon()
            }

            Snackbar.make(view, feedbackMessage, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun findBeaconCancel() {
        isScanning = false

        BleManager.getInstance().cancelScan()
    }

    private fun findBeacon() {
        isScanning = true

        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setScanTimeOut(10000)
            .build()

        BleManager.getInstance().initScanRule(scanRuleConfig)

        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                Log.d("B", "==> onScanStarted: $success")

                beaconList.clear()

                // Mudando o botão para pausar
                fab.setImageResource(android.R.drawable.ic_media_pause)
                fab.supportBackgroundTintList = ContextCompat.getColorStateList(this@FindBeaconActivity, R.color.button_search_stop)
            }

            override fun onLeScan(bleDevice: BleDevice?) {
            }

            override fun onScanning(bleDevice: BleDevice) {
                val distance: Double = getDistance(bleDevice.rssi, -71)
                val distanceText = "${distance} metros"

                val beaconName: String
                val beaconCategory: String

                if (bleDevice.mac == "0E:F3:EE:2A:0D:23") {
                    beaconName = "Eniac"
                    beaconCategory = "Tecnologia"
                } else {
                    beaconName = "Dispositivo Não Cadastrado"
                    beaconCategory = ""
                }

                val beacon = BeaconData(
                    bleDevice.mac,
                    bleDevice.key,
                    distanceText,
                    beaconName,
                    beaconCategory
                )

                Log.d("B", "==> onScanning: $bleDevice")
                Log.d("B", "==> Distancia: $distance | Mac: " + bleDevice.mac + " | RSSI: " + bleDevice.rssi)

                beaconList.add(beacon)

                // Atualizando Listagem de Beacon
                viewAdapter.notifyDataSetChanged()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                Log.d("B", "==> onScanFinished: $scanResultList")

                fab.setImageResource(android.R.drawable.ic_media_play)
                fab.supportBackgroundTintList = ContextCompat.getColorStateList(this@FindBeaconActivity, R.color.button_search_play)
                isScanning = false
            }
        })
    }
}
