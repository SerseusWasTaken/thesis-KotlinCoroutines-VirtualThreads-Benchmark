package de.inovex;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openjdk.jmh.infra.Blackhole;

public class Methods {
    public static Path path = Paths.get(URI.create("file:///Users/jschlecht/Documents/Thesis/FileToRead"));
    public static ExecutorService exec = Executors.newSingleThreadExecutor();
    public static HttpClient client = HttpClient.newBuilder().executor(exec).build();

    public static void readFile(Blackhole bh, ByteBuffer buffer) {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            try {
                channel.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.flip();
            bh.consume(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sleep(Blackhole bh, int duration) {
        try {
            Thread.sleep(duration);
            bh.consume(Thread.currentThread());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void httpRequest(Blackhole bh, HttpRequest request) throws IOException, InterruptedException {
        bh.consume(client.send(request, HttpResponse.BodyHandlers.ofString()));
    }
}
