# 009 — TCP length-prefixed frame decoder

## Question

Implement a decoder for a stream of frames encoded as:

```text
[4-byte big-endian payload length][payload bytes]
```

`feed` may receive:

- part of a header
- part of a payload
- multiple complete frames
- any combination across calls

Requirements:

- preserve incomplete data between calls
- emit all complete frames
- reject negative or oversized lengths
- set a configurable maximum frame size
- avoid assuming one TCP read equals one application message

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
