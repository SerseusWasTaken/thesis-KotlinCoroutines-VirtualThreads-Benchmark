# Kotlin Coroutines/Virtual Threads Benchmark
This repository contains the source code belonging to the bachelor thesis "Quantifizierung und Gegenüberstellung von Kotlin Coroutines und Virtual Threads". 

## Overview
This repository contains two seperate Maven projects: [coroutinesBench](./coroutinesBench/) and [loomBench](./loomBench/). 

### Requirements
- At least Java version 19 (to run Virtual Threads)
- Maven
- A file on your filesystem for the benchmark to read
- A robust local HTTP server 

You can change both, the path of the file to read and the port/URL the benchmarks are sending requests to in the files [CoroutineBenchmark.kt](./coroutinesBench/src/main/kotlin/de/inovex/CoroutineBenchmark.kt) and [ThreadsBenchmark.java](./loomBench/src/main/java/de/inovex/ThreadsBenchmark.java)

### Running the benchmarkss
To run either, simply navigate into the respective folder and execute
> mvn clean install

After that you can run the Kotlin Coroutines benchmarks using
> java -jar target/benchmarks.jar -prof gc

And the Virtual Thread benchmarks using
> java --enable-preview target/benchmarks.jar -prof gc


