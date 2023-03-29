package com.awcology

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.awcology.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {

    var mainBinding: ActivityMainBinding? = null
    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()

        mainBinding?.let {
            setContentView(it.root)

            it.cvEasy.setOnClickListener {
                //showMaintenanceDialog()
                startActivity(
                    Intent(mContext, FillTheWordActivity::class.java)
                        .putExtra(
                            "length",
                            3
                        )
                        .putExtra(
                            "level",
                            getString(R.string.easy_score_key)
                        )
                        .putExtra(
                            "tryKey",
                            getString(R.string.easy_tries_key)
                        )
                )
            }

            it.cvMedium.setOnClickListener {
              // showMaintenanceDialog()
                startActivity(
                    Intent(mContext, FillTheWordActivity::class.java)
                        .putExtra(
                            "length",
                            5
                        )
                        .putExtra(
                            "level",
                            getString(R.string.medium_score_key)
                        )
                        .putExtra(
                            "tryKey",
                            getString(R.string.medium_tries_key)
                        )
                )
            }

            it.cvHard.setOnClickListener {
               // showMaintenanceDialog()
                startActivity(
                    Intent(mContext, FillTheWordActivity::class.java)
                        .putExtra(
                            "length",
                            7
                        )
                        .putExtra(
                            "level",
                            getString(R.string.hard_score_key)
                        )
                        .putExtra(
                            "tryKey",
                            getString(R.string.hard_tries_key)
                        )
                )
            }

        }
    }

    private fun init() {
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        mContext = this
        initializeAndLoadAd()
    }

    private fun showMaintenanceDialog(){
        AlertDialog.Builder(mContext)
            .setTitle("INFORMATION")
            .setMessage(
                "Sorry for the inconvenience. We are making things better for you. Thank you for your patience."
            )
            .setCancelable(false)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, unit_ ->
                    dialog.cancel()
                    dialog.dismiss()
                })
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun initializeAndLoadAd() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        mainBinding?.adView?.loadAd(adRequest)
    }
}