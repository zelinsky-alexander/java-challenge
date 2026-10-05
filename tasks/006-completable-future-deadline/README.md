# 006 — CompletableFuture aggregation with one deadline

## Question

Implement `loadDashboard` which starts three independent backend calls in parallel and combines their results.

Requirements:

- all calls share one overall deadline
- do not wait the full timeout separately for each call
- cancel unfinished work after deadline/failure where practical
- preserve the original failure cause
- do not block a common ForkJoinPool thread with artificial sleeps in orchestration code

Be ready to compare this approach with virtual threads.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
