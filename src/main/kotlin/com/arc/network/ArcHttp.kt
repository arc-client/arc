
package com.arc.network

import com.arc.Arc
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import java.io.File
import java.io.OutputStream

val ArcHttp = HttpClient {
    install(ContentNegotiation) {
        // Use our gson instance
        register(ContentType.Application.Json, GsonConverter(Arc.gson))
    }
}

suspend inline fun HttpClient.download(url: String, file: File, block: HttpRequestBuilder.() -> Unit = {}) {
    val response = get(url, block)
    check(response.status.isSuccess()) { "Download for $url failed with non 2xx status code" }

    file.writeBytes(response.readRawBytes())
}

suspend inline fun HttpClient.download(url: String, output: OutputStream, block: HttpRequestBuilder.() -> Unit = {}) {
    val response = get(url, block)
    check(response.status.isSuccess()) { "Download for $url failed with non 2xx status code" }

    output.write(response.readRawBytes())
}

suspend inline fun HttpClient.download(url: String, block: HttpRequestBuilder.() -> Unit) =
    get(url, block).readRawBytes()

