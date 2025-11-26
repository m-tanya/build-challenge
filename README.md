# Intuit Build Challenge

This repository contains two independent Java deliverables plus Docker/Compose wiring so you can run them with minimal setup:
- `consumer-producer`: a multithreaded producer/consumer demo that showcases synchronization, blocking queues, and comprehensive tests.
- `data-analysis`: a sales analytics toolkit that reads/generates CSVs, computes revenue/margin metrics, and ships with a rich test suite.

All outputs (demo logs, analytics summaries, test reports) print directly to the console.

## Quick Start

### Prerequisites
- Java 11+ if you want to run scripts locally.
- Docker 20.10+ (optional).
- `docker compose` (v2+) if you plan to use the provided compose file.

### Local Scripts
```bash
# Consumer/producer demo
cd consumer-producer
chmod +x scripts/*.sh
./scripts/run-demo.sh

# Consumer/producer tests
./scripts/run-tests.sh

# Data analysis tests (compiles, runs JUnit, and prints analytics results)
cd ../data-analysis
chmod +x run-tests.sh
./run-tests.sh
```

### Docker (per module)
```bash
# Build multi-stage images
docker compose build consumer-producer data-analysis

# Run producer-consumer demo in a container
docker compose up consumer-producer

# Run consumer-producer tests (service is behind the `ci` profile)
docker compose run --rm consumer-producer-tests

# Execute data-analysis test suite
docker compose up data-analysis
```

### Useful Overrides
- `docker run --rm consumer-producer ./scripts/run-tests.sh`
- `docker run --rm data-analysis ./compile.sh`
- `docker run --rm data-analysis java -cp out com.dataanalysis.app.Main analyze`
- Bind mount `data-analysis/data` if you want generated CSVs to persist: `docker run --rm -v "$(pwd)/data-analysis/data:/app/data" data-analysis …`

Compose logs include timestamps and are easy to export (`docker compose logs > run.log`) for interview portals.

## Expected Outputs to Capture

| Scenario | Command | What to Copy |
| --- | --- | --- |
| Producer/consumer demo | `./consumer-producer/scripts/run-demo.sh` or `docker compose up consumer-producer` | Configuration block, live queue logs, final statistics/verification |
| Producer/consumer tests | `./consumer-producer/scripts/run-tests.sh` or `docker compose run --rm consumer-producer-tests` | Unit/edge/integration PASS summaries |
| Data-analysis tests | `./data-analysis/run-tests.sh` or `docker compose up data-analysis` | Compilation results, JUnit summary, analytics console report |
| Custom analytics run | `java -cp out com.dataanalysis.app.Main analyze data/sales.csv` | High-level metrics printed by `Main` |

Use the table as a checklist when attaching outputs to submission forms.

### Sample Producer-Consumer Demo Log
```
╔════════════════════════════════════════╗
║         Running Demo                   ║
╚════════════════════════════════════════╝

╔════════════════════════════════════════╗
║   Producer-Consumer Pattern Demo      ║
╚════════════════════════════════════════╝

Configuration:
  Items to transfer: 20
  Queue capacity: 5
  Producer delay: 50ms
  Consumer delay: 100ms
  (Consumer is slower - expect queue to fill up)

Starting producer and consumer threads...

[23:36:45] INFO: [Producer-1] Started
[23:36:45] INFO: [Consumer-1] Started
[23:36:45] INFO: [Producer-1] Produced item. Queue size: 1/5
[23:36:45] INFO: [Consumer-1] Consumed item. Queue size: 0/5
… (live production/consumption log)
[23:36:47] INFO: [Producer-1] Finished - produced 20 items
[23:36:47] INFO: [Consumer-1] Finished - consumed 20 items
[23:36:47] INFO:
=== Queue Statistics ===
[23:36:47] INFO: Capacity: 5
[23:36:47] INFO: Items produced: 20
[23:36:47] INFO: Items consumed: 20
[23:36:47] INFO: Current size: 0
[23:36:47] INFO: Items in transit: 0
[23:36:47] INFO: Closed: true

=== Results ===
Time elapsed: 2079ms
Source size: 0
Destination size: 20
Queue size: 0

Verification: ✓ SUCCESS
All 20 items successfully transferred!
```

## Repository Layout
```
.
├── consumer-producer/      # Producer/consumer implementation, tests, Dockerfile, README
├── data-analysis/          # Sales analytics implementation, tests, Dockerfile, README, docs
├── docker-compose.yml      # Shared compose file for running demos/tests
└── README.md               # This file (high-level instructions + output log guidance)
```

Each module’s README dives into design details; this root README is focused on execution steps and artifacts you need to capture.

