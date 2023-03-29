package com.awcology

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.awcology.databinding.ActivityFillTheWordBinding
import com.awcology.extensions.disableDarkTheme
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.streams.asSequence


class FillTheWordActivity : AppCompatActivity() {

    var binding: ActivityFillTheWordBinding? = null
    private val client = WordsService.create()
    lateinit var mContext: Context
    private var length = 0
    private var levelScore = ""
    private var tryKey = ""
    private var wordToGuess = ""
    private var numOfHiddenLetters = 0
    private var allViews = ArrayList<TextView>()
    var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableDarkTheme()
        init()

        binding?.let { view ->
            setContentView(view.root)
            view.progressBar.visibility = VISIBLE
            view.ivInfo.setOnClickListener {
                showAlertDialog(
                    "Information",
                    "Guess the HIDDEN WORD by tapping the LETTERS shown at the bottom"
                ) {

                }
            }



            getIntentData()
            view.tvScore.text = retrieveDataFromSharedPref(levelScore, 0).toString()
            view.tvChances.text = retrieveDataFromSharedPref(tryKey, 10).toString()
            fetchWord()
        }

    }

    private fun showAlertDialog(title: String, description: String, continueListener: () -> Unit) {
        AlertDialog.Builder(mContext)
            .setTitle(title)
            .setMessage(
                description
            )
            .setCancelable(false)
            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, unit_ ->
                    dialog.cancel()
                    dialog.dismiss()
                    continueListener.invoke()
                })
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    override fun onPause() {
        if (Integer.parseInt(binding?.tvChances?.text.toString()) <= 0) {
            saveDataToSharedPref(levelScore, 0)
            saveDataToSharedPref(tryKey, 10)
        } else {
            saveDataToSharedPref(levelScore, Integer.parseInt(binding?.tvScore?.text.toString()))
            saveDataToSharedPref(tryKey, Integer.parseInt(binding?.tvChances?.text.toString()))
        }

        super.onPause()

    }

    private fun init() {
        binding = ActivityFillTheWordBinding.inflate(layoutInflater)
        mContext = this
        initializeAndLoadAd()
    }

    private fun getIntentData() {
        if (intent.hasExtra("length")) {
            length = intent.getIntExtra("length", 0)
        }
        if (intent.hasExtra("level")) {
            levelScore = intent.getStringExtra("level")!!
        }
        if (intent.hasExtra("tryKey")) {
            tryKey = intent.getStringExtra("tryKey")!!
        }

    }


    private fun fetchWord() {

        if (isNetworkAvailable()) {
            binding?.let { view ->
                CoroutineScope(Dispatchers.IO).launch {
                    var result = client.getWords(1, length)
                    if (result.isNotEmpty()) {
                        for (word in result) {
                            wordToGuess = word
                            CoroutineScope(Dispatchers.Main).launch {
                                view.progressBar.visibility = GONE
                                setupView(wordToGuess)
                            }
                        }
                    } else {
                        showAlertDialog(
                            "ERROR",
                            "There is some issue in your connection. Click OK after fixing it."
                        ) {
                            fetchWord()
                        }
                    }
                }
            }
        } else {
            showAlertDialog("ERROR", "Please turn internet on and press ok") {
                fetchWord()
            }
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun setupView(word: String) {
        var current = 0
        var angle = 10F
        val source = "abcdefghijklmnopqrstuvwxyz"


        var suggestionString = ""
        var rightCharacter = ""
        word.forEachIndexed { index, character ->

            val view: View = layoutInflater.inflate(R.layout.item_word, null)
            val text = view.findViewById<TextView>(R.id.tv_letter)
            if (index % 2 == 0) {
                text.text = character.toString()
            } else {
                text.text = ""
                numOfHiddenLetters++
                suggestionString = suggestionString.plus(character)
            }

            binding?.llHiddenWord?.addView(view)

            allViews.add(text)

        }

        rightCharacter = suggestionString


        val size = suggestionString.length + 2

        while (suggestionString.length < size) {
            val character = java.util.Random().ints(1, 0, source.length)
                .asSequence()
                .distinct()
                .map(source::get)
                .joinToString("")

            if (!suggestionString.contains(character)) {
                suggestionString = suggestionString.plus(character)
            }
        }


        while (current < size) {
            angle *= -1
            var character = ""
            character = if (current + 1 != size) {
                java.util.Random().ints(1, 0, suggestionString.length)
                    .asSequence()
                    .distinct()
                    .map(suggestionString::get)
                    .joinToString("")
            } else {
                suggestionString
            }


            val view: View = layoutInflater.inflate(R.layout.item_word, null)
            val text = view.findViewById<TextView>(R.id.tv_letter)
            text.text = character
            suggestionString = suggestionString.replaceFirst(character, "")

            binding?.llSuggestion?.addView(view)
            view.setPadding(10, 10, 10, 10)
            view?.animate()?.rotation(angle)

            view.setOnClickListener {
                if (rightCharacter.contains(text.text)) {
                    view.visibility = GONE
                    var index = wordToGuess.indexOf(text.text.toString())
                    var view = allViews[index]
                    view.text = text.text

                    numOfHiddenLetters--
                    if (numOfHiddenLetters == 0) {
                        binding?.tvScore?.text =
                            (Integer.parseInt(binding?.tvScore?.text.toString()) + 1).toString()
                        wordToGuess = wordToGuess.capitalize()
                        showAlertDialog(
                            "Congratulations",
                            "The word was : $wordToGuess"
                        ) {
                            binding?.progressBar?.visibility = VISIBLE
                            binding?.llSuggestion?.removeAllViews()
                            binding?.llHiddenWord?.removeAllViews()
                            allViews.clear()
                            fetchWord()
                        }
                    }

                } else {

                    var tries = Integer.parseInt(binding?.tvChances?.text.toString()) - 1
                    binding?.tvChances?.text =
                        (tries).toString()
                    if (tries <= 0) {
                        showAlertDialog(
                            "Looser Detected",
                            "You need to work on you vocabulary buddy."
                        ) {
                            showFullScreenAd()
                        }
                    } else {
                        showAlertDialog("INFORMATION", "Wrong Word. Remaining Tries : $tries") {

                        }
                    }

                }
            }

            current++

        }
    }

    private fun saveDataToSharedPref(key: String, value: Int) {
        val sharedPref = this?.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref?.edit()) {
            this?.putInt(key, value)
            this?.apply()
        }
    }

    private fun retrieveDataFromSharedPref(key: String, defaultValue: Int): Int? {
        val sharedPref = this?.getPreferences(Context.MODE_PRIVATE)

        return sharedPref?.getInt(key, defaultValue)
    }

    override fun onBackPressed() {
        showFullScreenAd()
    }

    private fun showFullScreenAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        mInterstitialAd = null
                        finish()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        finish()
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        mInterstitialAd = null
                        finish()

                    }
                }
        } else {
            finish()
        }

    }

    private fun initializeAndLoadAd() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding?.adView?.loadAd(adRequest)

        InterstitialAd.load(
            this,
            getString(R.string.ADMOB_INTERSTITIAL_AD_ID),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("ADD_TAG", adError?.toString()!!)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("ADD_TAG", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })


    }

}