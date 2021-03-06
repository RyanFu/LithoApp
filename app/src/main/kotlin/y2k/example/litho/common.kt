package y2k.example.litho

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.*


/**
 * Created by y2k on 07/07/2017.
 **/

object Net {

    private val client = OkHttpClient()

    suspend fun readText(url: URL): String = task {
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unexpected code " + response)

        response.body()!!.string()
    }
}

private val htmlRegex = Regex("&#(\\d+);")

fun String.unescapeHtml(): String =
    replace(htmlRegex) {
        it.groupValues[1].toInt().toChar().toString()
    }

suspend fun <T> task(action: () -> T): T =
    suspendCoroutine { con ->
        object : AsyncTask<Unit, Unit, Result<T>>() {

            override fun doInBackground(vararg ignore: Unit?): Result<T> =
                try {
                    Ok(action())
                } catch (e: Exception) {
                    Error(e)
                }

            override fun onPostExecute(result: Result<T>) = when (result) {
                is Ok<T> -> con.resume(result.value)
                is Error -> con.resumeWithException(result.error)
            }
        }.execute()
    }

sealed class Result<out T>
class Error(val error: Exception) : Result<Nothing>()
class Ok<out T>(val value: T) : Result<T>()

private val scheduler by lazy { Executors.newScheduledThreadPool(1) }
private val uiHandler by lazy { Handler(Looper.getMainLooper()) }

suspend fun wait(time: Long) {
    return suspendCoroutine { con ->
        scheduler.schedule({
            uiHandler.post { con.resume(Unit) }
        }, time, TimeUnit.MILLISECONDS)
    }
}

fun launch(action: suspend () -> Unit) {
    action.startCoroutine(object : Continuation<Unit> {
        override val context: CoroutineContext = EmptyCoroutineContext
        override fun resume(value: Unit) = Unit
        override fun resumeWithException(exception: Throwable) = throw exception
    })
}