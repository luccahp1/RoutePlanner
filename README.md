# Route Planner (Milestone 1)

This is Milestone 1 of the OnlyEavestroughs route planning CLI.

## What this milestone does
- Reads addresses from a TXT file (one per line)
- Requires a depot/start address (changes per run)
- Creates a timestamped output folder
- Writes a first `routes.txt` that echoes the depot + cleaned address list
- Writes a `routes.json` skeleton
- Writes a `debug_report.txt` with basic run stats

No geocoding, no matrix, no optimization yet.

## Requirements
- JDK 21
- Maven 3.9+

## Build
```bash
mvn test package
```

This produces a runnable shaded jar:
- `target/routeplanner-0.1.0.jar`

## Run
```bash
java -jar target/routeplanner-0.1.0.jar --depot "1560 Hartlet Street London Ontario" --input addresses.txt
```

Optional flags:
- `--out output` (default: `output`)
- `--cache cache` (default: `cache`)

## Output
A new folder is created under the out directory, for example:
- `output/20260119_203501/`

Files created:
- `routes.txt`
- `routes.json`
- `debug_report.txt`

