package br.com.i42.controledeativos

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.android.synthetic.main.activity_beacon_detail.*

class BeaconDetailActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon_detail)

        val intentThatStartedThisActivity = intent


        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            val beaconMac = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT)

            initBle()

            handlePageContent(beaconMac)

            handleBleConnect(beaconMac)
        }
    }

    private fun initBle() {
        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .operateTimeout = 5000
    }

    @SuppressLint("SetTextI18n")
    private fun handlePageContent(beaconMac: String) {
        if (beaconMac == "0E:F3:EE:2A:0D:23") {
            beacon_item_id.text =
                    "O vendedor Eniac está vendendo na plataforma desde 2010, " +
                    "conta com mais de 2.000 produtos alocados em nosso galpão de fullfilment. E a categoria desse beacon para a loja é Tecnologia." +
                    "O responsável por esse setor é: Almeida."
        } else {
            beacon_item_id.text = "Dispositivo ainda não cadastrado."
        }
    }

    private fun handleBleConnect(beaconMac: String) {
        BleManager.getInstance().connect(beaconMac, object : BleGattCallback() {
            override fun onStartConnect() {
                Log.d("B", "Iniciando Conexão")
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                Log.d("B", "Desconectando")
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                Log.d("B", "Conectou, ble: $bleDevice | gatt $gatt | status: $status")
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                Log.d("B", "Falhou na conexão || ${BleException.ERROR_CODE_OTHER} || ${BleException.ERROR_CODE_GATT}")
            }

        })
    }
}
