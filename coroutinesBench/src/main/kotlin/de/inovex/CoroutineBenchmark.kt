package de.inovex

import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.profile.CompilerProfiler
import org.openjdk.jmh.profile.GCProfiler
import org.openjdk.jmh.profile.StackProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.lang.management.ManagementFactory
import java.net.URI
import java.net.http.HttpRequest
import java.nio.ByteBuffer
import java.util.stream.IntStream

@State(Scope.Thread)
open class CoroutineBenchmark {

    @Param("1", "10", "100", "1000", "4000")
    var levelOfConcurrency: Int = 1

    @Benchmark
    fun testSleep(bh: Blackhole): List<Job> = runBlocking {
        var jobs = IntStream.range(0, levelOfConcurrency).mapToObj {
            launch(start = CoroutineStart.LAZY) {
                sleep(bh, 100)
            }
        }.toList()
        jobs.forEach { job -> job.start() }
        jobs.forEach { job -> job.join() }
        return@runBlocking jobs
    }
    
    //Limiting the number of HTTP requests here is most likely mandatory because of ephemeral port exhaustion
    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(batchSize = 1, iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    fun testNetworkIO(bh: Blackhole): List<Job> = runBlocking {
        var jobs = IntStream.range(0, levelOfConcurrency).mapToObj {
            var request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/"))
                .build()
            launch(start = CoroutineStart.LAZY) {
                makeHttpRequest(bh, request)
            }
        }.toList()
        jobs.forEach { job -> job.start() }
        jobs.forEach { job -> job.join() }
        return@runBlocking jobs
    }


    @Benchmark
    fun testFileIO(bh: Blackhole): List<Job> = runBlocking(context = Dispatchers.Default) {
        var jobs = IntStream.range(0, levelOfConcurrency).mapToObj {
            val buffer = ByteBuffer.allocate(250_000)
            launch(start = CoroutineStart.LAZY) {
                readFile(bh, buffer)
            }
        }.toList()
        jobs.forEach { job -> job.start() }
        jobs.forEach { job -> job.join() }
        return@runBlocking jobs
    }

    @TearDown(Level.Trial)
    fun closeExecutor() {
        executor.shutdown()
    }
}

fun main(args: Array<String>) {
    val opt = OptionsBuilder()
        .addProfiler(GCProfiler::class.java)
        .addProfiler(StackProfiler::class.java)
        .addProfiler(CompilerProfiler::class.java)
        .build()
    Runner(opt).run()
}
