package de.inovex

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <A> AsynchronousSocketChannel.aConnect(adress: SocketAddress, attachment: A) = suspendCoroutine<A> { cont ->
    connect(adress, attachment, object : CompletionHandler<Void, A> {
        override fun completed(result: Void?, attachment: A) {
            cont.resume(attachment)
        }

        override fun failed(exc: Throwable, attachment: A) {
            cont.resumeWithException(exc)
        }

    })
}

suspend fun AsynchronousFileChannel.aRead(buffer: ByteBuffer): ByteBuffer = suspendCoroutine { cont ->
    read(buffer, 0L, buffer, object : CompletionHandler<Int, ByteBuffer> {
        override fun completed(result: Int, attachment: ByteBuffer) {
            cont.resume(attachment)
        }

        override fun failed(exc: Throwable, attachment: ByteBuffer) {
            cont.resumeWithException(exc)
        }

    })
}