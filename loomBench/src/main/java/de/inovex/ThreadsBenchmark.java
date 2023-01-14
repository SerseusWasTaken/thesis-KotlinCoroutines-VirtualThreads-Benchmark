package de.inovex;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.CompilerProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class ThreadsBenchmark {

    @Param({"1", "10", "100", "1000", "4000"})
    int levelOfConcurrency = 1;
    
    @Benchmark
    public List<Thread> testVirtualSleep(Blackhole bh) {
        var threads = IntStream.range(0, levelOfConcurrency)
        .mapToObj(i -> Thread.ofVirtual().unstarted(() -> {
                        Methods.sleep(bh, 100);
                    })
        ).toList();
        threads.forEach( t1 -> t1.start());
        threads.forEach( t1 -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return threads;
    }
    
    
    // Limiting the number of HTTP requests here is most likely mandatory because of ephemeral port exhaustion
    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(batchSize = 1, iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public List<Thread> testVirtualNetworkIO(Blackhole bh) throws InterruptedException {
        var threads = IntStream.range(0, levelOfConcurrency)
        .mapToObj(i -> {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:8080/"))
            .build();
            return Thread.ofVirtual().unstarted(() -> {
                        try {
                            Methods.httpRequest(bh, request);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }).toList();
        threads.forEach( t1 -> t1.start());
        threads.forEach( t1 -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return threads;
    }
    

    /*
     * Will increase the parallelism of the Virtual Thread scheduler up to 256 since file IO isn't compatible with VT unmounting yet
     * This can have a substantial impact on throughput depending on how long the reading of the file takes
     */
    @Benchmark
    public List<Thread> testVirtualFileIO(Blackhole bh) {
        var threads = IntStream.range(0, levelOfConcurrency)
        .mapToObj(i -> {
            var buffer = ByteBuffer.allocate(250_000);
            return Thread.ofVirtual().unstarted(() -> { Methods.readFile(bh, buffer);});
                }).toList();
        threads.forEach( t1 -> t1.start());
        threads.forEach( t1 -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return threads;
    }

    @Benchmark
    public List<Thread> testPlattformSleep(Blackhole bh) {
        var threads = IntStream.range(0, levelOfConcurrency)
        .mapToObj(i -> Thread.ofPlatform().unstarted(() -> {
                        Methods.sleep(bh, 100);
                    })
        ).toList();
        threads.forEach( t1 -> t1.start());
        threads.forEach( t1 -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return threads;
    }
     
    // Limiting the number of HTTP requests here is most likely mandatory because of ephemeral port exhaustion
    @Benchmark
    @Fork(value = 1)
    @Warmup(iterations = 1)
    @Measurement(batchSize = 1, iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public List<Thread> testPlattformNetworkIO(Blackhole bh) throws InterruptedException {
        var threads = IntStream.range(0, levelOfConcurrency)
        .mapToObj(i -> {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:8080/"))
            .build();
            return Thread.ofPlatform().unstarted(() -> {
                        try {
                            Methods.httpRequest(bh, request);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }).toList();
        threads.forEach( t1 -> t1.start());
        threads.forEach( t1 -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return threads;
    }
    
    //@Benchmark
    public List<Thread> testPlattformFileIO(Blackhole bh) {
        var threads = IntStream.range(0, levelOfConcurrency)
        .mapToObj(i -> {
            var buffer = ByteBuffer.allocate(250_000);
            return Thread.ofPlatform().unstarted(() -> { Methods.readFile(bh, buffer); });
                }).toList();
        threads.forEach( t1 -> t1.start());
        threads.forEach( t1 -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return threads;
    }
    
    @TearDown(Level.Trial)
    public void teardown() {
        Methods.exec.shutdown();
    }
    
    public static void main(String[] args) throws RunnerException, InterruptedException {
        var opt = new OptionsBuilder()
            .addProfiler(GCProfiler.class)
            .addProfiler(StackProfiler.class)
            .addProfiler(CompilerProfiler.class)
            .build();
        
        new Runner(opt).run();
    }
    
}
