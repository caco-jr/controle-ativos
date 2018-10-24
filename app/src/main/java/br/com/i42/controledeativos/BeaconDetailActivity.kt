package br.com.i42.controledeativos

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_beacon_detail.*

class BeaconDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon_detail)

        val intentThatStartedThisActivity = getIntent()

        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            val beacon = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT)

            beacon_item_id.text = beacon
        }
    }
}
