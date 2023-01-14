package de.inovex

import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.openjdk.jmh.infra.Blackhole
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors

val path = Paths.get(URI.create("file:///Users/jschlecht/Documents/Thesis/FileToRead"))
val executor = Executors.newSingleThreadExecutor()
val client = HttpClient.newBuilder().executor(executor).build()

suspend fun readFile(bh: Blackhole, buffer: ByteBuffer) = AsynchronousFileChannel.open(path, StandardOpenOption.READ).use {
    it.aRead(buffer)
    buffer.flip()
    bh.consume(buffer)
}

suspend fun sleep(bh: Blackhole, duration: Long) {
    delay(duration)
    bh.consume(Thread.currentThread())
}

suspend fun makeHttpRequest(bh: Blackhole, request: HttpRequest) {
    bh.consume(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await())
}
