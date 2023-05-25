package com.example.imagegenerator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.imagegenerator.ui.theme.ImageGeneratorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture.AsynchronousCompletionTask

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageGeneratorTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Alex")
                }
            }
        }
    }
}

fun ButtonClicked(s: String): String
{
    return "clicked"
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(false) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    Surface() {
        Column {
            Text(text = "Cute anime baby", modifier = modifier.padding(24.dp))
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Generated image",
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                )
            }
            Button(
                onClick = {
                    isLoading = true
                    buttonClickHandler { bitmap ->
                        imageBitmap = bitmap?.asImageBitmap()
                        isLoading = false
                    }
                },
                modifier = modifier
                    .height(35.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
            ) {
                Text("Click me!")
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = modifier.padding(24.dp))
            }
        }
    }
}

class NetworkTask : AsyncTask<Unit, Unit, Bitmap?>() {
    override fun doInBackground(vararg params: Unit?): Bitmap? {
        val openaiUrl = "https://api.openai.com/v1/images/generations"
        val openaiApiKey = "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"
        val prompt = "A cute anime baby"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $openaiApiKey"
        )
        val payload = """{
            "prompt": "$prompt",
            "n": 1,
            "size": "1024x1024"
        }""".trimIndent()
        val url = URL(openaiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        connection.doOutput = true
        connection.outputStream.write(payload.toByteArray())
        val responseJson = JSONObject(connection.inputStream.bufferedReader().readText())
        val imageUrl = responseJson.getJSONArray("data").getJSONObject(0).getString("url")
        return BitmapFactory.decodeStream(URL(imageUrl).openStream())
    }
}

fun buttonClickHandler(onComplete: (Bitmap?) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        val bitmap = NetworkTask().execute().get()
        withContext(Dispatchers.Main) {
            onComplete(bitmap)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImageGeneratorTheme {
        Greeting("Alex")
    }
}