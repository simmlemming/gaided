package com.gaided.engine.api

import com.gaided.logger.Logger
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

internal open class HttpApi(
    protected val openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection },
) {

    internal fun <T> post(request: PostRequest<T>.() -> Unit): T {
        val postRequest = PostRequest<T>().apply(request)
        return call(postRequest)
    }

    private fun <T> call(request: Request<T>): T {
        request.validate()
        var connection: HttpURLConnection? = null
        val startTime = System.currentTimeMillis()

        try {
            Logger.d("> ${request.asString()}")

            connection = openConnection(request.url!!).apply {
                doOutput = request is PostRequest
                request.headers.forEach { setRequestProperty(it.key, it.value) }
            }

            if (request is PostRequest) {
                Logger.v("> ${request.body}")
                DataOutputStream(connection.outputStream).use {
                    it.writeBytes(request.body!!)
                }
            }

            val response = connection.inputStream.readAdString()
            val duration = System.currentTimeMillis() - startTime

            Logger.d("< ${request.asString()} ($duration ms)")
            Logger.v("< $response")
            return request.parse!!.invoke(response)
        } catch (e: Exception) {
            val errorMessage = connection?.errorStream?.readAdString()
            throw IOException(errorMessage, e)
        } finally {
            connection?.disconnect()
        }
    }

    internal sealed class Request<T> {
        internal var url: URL? = null
        internal val headers = mutableMapOf<String, String>()
        internal var parse: ((String) -> T)? = null
        internal var asString: () -> String = { this@Request.toString() }

        internal open fun validate() {
            checkNotNull(url) { "url is null" }
            checkNotNull(parse) { "parse is null" }
        }
    }

    internal class PostRequest<T> : Request<T>() {
        internal var body: String? = null
            set(value) {
                field = value
                headers["Content-Length"] = (value?.length ?: 0).toString()
            }

        override fun validate() {
            super.validate()
            checkNotNull(body) { "body is null" }
        }

        override fun toString(): String {
            return "POST ${url.toString()}"
        }
    }

    private fun InputStream.readAdString(): String {
        val result = StringBuilder()

        BufferedReader(InputStreamReader(this)).use {
            var line = it.readLine()
            while (line != null) {
                result.append(line)
                line = it.readLine()
            }
        }

        return result.toString()
    }
}