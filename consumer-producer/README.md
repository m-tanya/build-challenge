# Producer-Consumer Demo

Bounded blocking queue implemented with plain Java monitors. Producers drain a synchronized source container, consumers pull items out, and the queue announces completion once every registered producer finishes. All behavior is surfaced in the console logs so you can watch the system block, unblock, and verify work transfer.

## Quick Start
- Requires Java 11+ and a POSIX shell.
- Run once: `chmod +x consumer-producer/scripts/*.sh`.
- Demo: `./consumer-producer/scripts/run-demo.sh`.
- Tests: `./consumer-producer/scripts/run-tests.sh` (compiles sources, runs unit/edge/integration suites, prints pass/fail summaries).

### Docker Option
- Build once: `docker build -t consumer-producer ./consumer-producer`.
- Run the interactive demo: `docker run --rm consumer-producer`.
- Execute the full test suite inside the container: `docker run --rm consumer-producer ./scripts/run-tests.sh`.
- Using Compose from the repo root: `docker compose up consumer-producer` for the demo, or `docker compose run --rm consumer-producer-tests` for tests (the `consumer-producer-tests` service is hidden behind the `ci` profile so it only runs when explicitly requested).

## Architecture & Design
- **Source Container** – synchronized list that supports `removeFirst`, letting producers truly transfer ownership of each `WorkItem`.
- **Producer** – validates inputs, registers with the queue, drains the source, optionally sleeps between puts, and calls `producerDone` so consumers know when the pipeline is finished.
- **SharedQueue** – bounded `LinkedList`, guarded by `synchronized` plus `wait/notifyAll`, tracks `activeProducers`, and returns `null` to consumers after the last producer completes. Counters rely on `AtomicLong` to avoid overflow, and logging happens outside the critical section.
- **Consumer** – pulls until it reaches its assignment or the queue closes, storing each item in the destination container; interruption restores the thread flag for upstream callers.
- **Destination Container** – synchronized storage used to verify order and completeness at the end of each run.

## Runtime Experience
- Demo prints configuration (item count, queue capacity, delays), then a live timeline of producer/consumer actions, blocking events, queue stats, and a final verification banner (success/failure plus elapsed time and items-in-transit).
- Tests stream three labeled phases: queue unit tests, edge-case validations (nulls, capacity bounds, interruption, overflow), and integration scenarios that move 100–1000 items with different producer/consumer counts. Each scenario logs PASS/FAIL so results can be copied directly into an interview portal.
- No extra artifacts are written; configuration, progress, and metrics are visible in the terminal output.

## Key Behaviors
- Backpressure is observable: producers wait when the queue is full, consumers wait when empty, both wake via `notifyAll`, and the console logs show every transition.
- Lifecycle safety: producer registration plus automatic queue closure prevents stranded consumers and removes the need for poison pills or magic numbers.
- Defensive coding: synchronized containers, atomic counters, null validation, and graceful interruption handling keep the system stable even under heavy test loops.
- Coverage discipline: sixteen automated tests prove FIFO ordering, capacity enforcement, lifecycle closure, and integration correctness, making this implementation easy to discuss during interviews.
