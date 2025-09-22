package com.example.memestream

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonFetch = findViewById<Button>(R.id.btnFetchMeme)
        val txtDisplayMeme = findViewById<TextView>(R.id.txtViewMeme)
        val memeImg = findViewById<ImageView>(R.id.imageMeme)

        buttonFetch.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val giphyApiKey = "WicQSTYFbeZi8nbQblqrbdBYXecUeJ8V"
                    val giphyUrl = "https://api.giphy.com/v1/gifs/trending?api_key=$giphyApiKey&limit=25&rating=pg"

                    val connection = (URL(giphyUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 10_000
                        readTimeout = 10_000
                    }

                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        connection.disconnect()

                        val json = JSONObject(response)
                        val gifsArray = json.getJSONArray("data")

                        if (gifsArray.length() > 0) {
                            val randomIndex = Random.nextInt(gifsArray.length())
                            val gif = gifsArray.getJSONObject(randomIndex)
                            val title = gif.optString("title", "Untitled GIF")

                            val imageUrl = gif
                                .getJSONObject("images")
                                .getJSONObject("original")
                                .getString("url")

                            withContext(Dispatchers.Main) {
                                txtDisplayMeme.text = title
                                Glide.with(this@MainActivity)
                                    .asGif()
                                    .load(imageUrl)
                                    .into(memeImg)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                txtDisplayMeme.text = "No GIFs found!"
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            txtDisplayMeme.text = "Error: ${connection.responseCode}"
                        }
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        txtDisplayMeme.text = "Error: ${e.message}"
                    }
                }
            }
        }

    }
}