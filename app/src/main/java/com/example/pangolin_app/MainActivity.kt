package com.example.pangolin_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pangolin_app.ui.theme.Pangolin_appTheme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class ClientSo(host: String, port: Int) : Socket(host, port) {
    var reader = BufferedReader(InputStreamReader(inputStream))
    private val writer = BufferedWriter(OutputStreamWriter(outputStream))
    var text = ""

    fun run(): String {
        val line = reader.readLine()
        if(line!= null)
            return line
        else
            return "fail"
    }

    fun sendData(data: String) {
        writer.write(data)
        writer.newLine() // 寫入換行符，確保伺服器可以正確讀取行
        writer.flush() // 確保資料被送出
    }
}


class MainActivity : ComponentActivity() {
    val host = "192.168.1.218"
    val port = 8000
    private lateinit var client: ClientSo
    var axisMap = mutableMapOf<String, Float?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pangolin_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("穿山甲")
                }
            }
        }
        axisMap["xAxis"] = 0.0f
        axisMap["yAxis"] = 0.0f
        axisMap["zAxis"] = 0.0f
        axisMap["rzAxis"] = 0.0f

        Thread(Runnable {
            client = ClientSo(host, port)
        }).start()
        Thread(Runnable { socketSend() }).start()
    }

    fun socketSend() {
        println("socket")
        while (true){
            val jsonAxis = Json.encodeToString(axisMap)
            Thread.sleep(100)
                    Thread(Runnable {
                        client.sendData(jsonAxis)
                    }).start()
        }
//        Thread(Runnable {
//            client.sendData(jsonAxis)
//        }).start()
    }
    @SuppressLint("RestrictedApi")
    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        val zAxis: Float? = event?.getAxisValue(MotionEvent.AXIS_Z)
        axisMap["zAxis"] = zAxis
        return super.dispatchGenericMotionEvent(event)
    }
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event != null) {
            if (event.keyCode == KEYCODE_DPAD_UP) {
                if (event.action == MotionEvent.ACTION_DOWN) axisMap["yAxis"] = -1.0f
                else if (event.action == MotionEvent.ACTION_UP) axisMap["yAxis"] = 0.0f
            }
            if (event.keyCode == KEYCODE_DPAD_DOWN) {
                if (event.action == MotionEvent.ACTION_DOWN) axisMap["yAxis"] = 1.0f
                else if (event.action == MotionEvent.ACTION_UP) axisMap["yAxis"] = 0.0f
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Pangolin_appTheme {
        Greeting("Android")
    }
}