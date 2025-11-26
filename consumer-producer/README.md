# Producer-Consumer

A classic producer/consumer pipeline implemented with Java monitors. Producers drain a synchronized source container, consumers read from a bounded queue, and the queue announces completion once every producer finishes.

## What You Get
- **Languages/Tools**: Java 17, JUnit 5 Console Launcher, Docker, simple shell scripts.
- **Outputs**: `test-result/demo-output.txt`, `test-result/junit-output.txt`, and `test-result/ci-run.log` contain the demo timeline, unit-test tree, and CI run respectively.

## System Design
1. **Containers** – `Container` acts as both source and destination. It is synchronized so producers truly transfer ownership and consumers can validate ordering at the end.
2. **Shared Queue** – `SharedQueue` wraps a bounded `LinkedList`, protects access with `synchronized` + `wait/notifyAll`, and keeps track of `activeProducers`. When the last producer calls `producerDone`, waiting consumers receive `null` so threads exit naturally without poison pills.
3. **Thread Roles** – `Producer` registers with the queue, pulls from source, sleeps between puts (configurable), and logs activity. `Consumer` drains items until it meets its quota or the queue closes. Both honor interruption and preserve thread flags.
4. **Orchestration** – `ProducerConsumerDemo` wires everything together, prints configuration, starts threads, waits for completion, and prints queue statistics plus verification results.

## Running Locally
```bash
cd consumer-producer
chmod +x scripts/*.sh       # once
./scripts/run-demo.sh       # interactive demo + stats
./scripts/run-tests.sh      # compile + unit/edge/integration suites
```

The test script compiles both main and test sources into `bin/`, then launches the JUnit console runner with tree output.

## Docker / Compose
```bash
docker build -t consumer-producer .
docker run --rm consumer-producer                   # demo
docker run --rm consumer-producer ./scripts/run-tests.sh

# From repo root
docker compose up consumer-producer                 # demo service
docker compose run --rm consumer-producer-tests     # test service (ci profile)
```

## Key Behaviors
- **Backpressure** – Producers block when the queue is full; consumers block when empty. Logs show each transition so you can trace timing easily.
- **Lifecycle Safety** – Queue closure is automatic once all producers finish, eliminating race conditions and stranded consumers.
- **Defensive Coding** – Null validation, bound checks, and atomic counters protect against overflow and misuse.
- **Test Coverage** – Sixteen tests verify FIFO ordering, capacity enforcement, interruption behavior, and integration runs with up to 1,000 items.

## Choices & Assumptions
- Java 17, standard library only. No Executors/BlockingQueue so the synchronization logic stays visible via explicit `wait/notify`.
- Source/destination containers are simple synchronized lists to highlight ownership transfer; they are not meant for production-scale persistence.
- Demo delays (producer 50 ms, consumer 100 ms) intentionally offset to showcase queue backpressure. Feel free to tweak via script parameters.
- Tests rely on the bundled `lib/junit-platform-console-standalone.jar`; GitHub Actions downloads it automatically if missing.
- Docker image is multi-stage but still uses shell scripts for parity with local runs.

## Artifacts & Logs
- `test-result/demo-output.txt` – sample demo transcript.
- `test-result/junit-output.txt` – detailed test tree.
- `test-result/output-*.png` – screenshots for visual reference.
- `test-result/ci-run.log` – latest CI run mirroring GitHub Actions.

## Output
Check the `test-result/` folder for captured console logs referenced above.

Refer to the repository root README for cross-project tooling (GitHub Actions, compose instructions). This file focuses on the producer-consumer implementation itself.
