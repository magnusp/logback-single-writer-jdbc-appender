window.BENCHMARK_DATA = {
  "lastUpdate": 1789153654785,
  "repoUrl": "https://github.com/magnusp/logback-single-writer-jdbc-appender",
  "entries": {
    "JMH Benchmarks (SQLite Appender)": [
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1a070be1df84c6fb86a7ee89193fc8867bb6c62a",
          "message": "feat: implement resilient JDBC Logback appender with JMH benchmarks and tests (#2)",
          "timestamp": "2026-09-11T20:06:48+02:00",
          "tree_id": "9069f8c6560dde6253525d33fb1aac9968caf492",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/1a070be1df84c6fb86a7ee89193fc8867bb6c62a"
        },
        "date": 1789150047998,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.benchmarkAppend",
            "value": 46819998.66558317,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5f67de8d11b2e7a113dd36399aed8c3d92fcb70a",
          "message": "refactor: generalize to single-writer JDBC and SQLite, removing Turso-specific naming (#15)",
          "timestamp": "2026-09-11T20:13:19+02:00",
          "tree_id": "bddf7d9b0e041bb4cf058e3d952dbc318f57f46c",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/5f67de8d11b2e7a113dd36399aed8c3d92fcb70a"
        },
        "date": 1789150440980,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.benchmarkAppend",
            "value": 48334074.6317314,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5362f1941d396f85e43748ef919b3ba5de72c565",
          "message": "feat: add JsonColumnEventSqlBinder for SQLite / LibSQL JSON1 support (#16)",
          "timestamp": "2026-09-11T20:15:23+02:00",
          "tree_id": "5c3a59fd59003d0e4a969e11065f8b4befdcf589",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/5362f1941d396f85e43748ef919b3ba5de72c565"
        },
        "date": 1789150562318,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.benchmarkAppend",
            "value": 48909252.24235574,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "fa40fe0194fec0bdeeaa67f6981f0be96fdc3185",
          "message": "perf(appender): drain bursts in loops, debounce flush submissions, and await active flush on shutdown (#17)",
          "timestamp": "2026-09-11T20:24:09+02:00",
          "tree_id": "86a6cbf0189a030df210fd999bc9237f09b2a907",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/fa40fe0194fec0bdeeaa67f6981f0be96fdc3185"
        },
        "date": 1789151090271,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.benchmarkAppend",
            "value": 32044412.602880746,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "da51c84b4f66b33cdb1a8861090444ab5c397fef",
          "message": "ci: set comment-always: true so benchmark comparison tables are posted on every PR (#18)",
          "timestamp": "2026-09-11T20:25:35+02:00",
          "tree_id": "2372b2fbafbd90174cf0b3868844687760f21198",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/da51c84b4f66b33cdb1a8861090444ab5c397fef"
        },
        "date": 1789151174007,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.benchmarkAppend",
            "value": 51419654.299286395,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "a41780967bca1715802ee70aa3990729a4e146a6",
          "message": "perf(benchmarks): measure realistic SQLite batch write throughput across relational and JSON schemas (#19)",
          "timestamp": "2026-09-11T20:29:42+02:00",
          "tree_id": "3f3e216852f0e465e4f4aa3f9b516910f289f4d6",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/a41780967bca1715802ee70aa3990729a4e146a6"
        },
        "date": 1789151452539,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf100",
            "value": 64321.81160926009,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf500",
            "value": 153614.10821100976,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf100",
            "value": 128105.0860378538,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf500",
            "value": 109509.80359938306,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "1431685+magnusp@users.noreply.github.com",
            "name": "Magnus Persson",
            "username": "magnusp"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3d95a029e71606533c7a11f58a5bc647dcd110d4",
          "message": "fix: poison-pill bisection, shutdown race, escapeJson, autoCommit restore (#20)\n\n* fix: poison-pill bisection, shutdown race, escapeJson, autoCommit restore\n\n- sendWithRetry replaced with recursive sendBatch that bisects failing batches\n  in half until isolating a single unwritable event (poison pill), which is\n  then discarded after one backoff attempt. Chronological order is preserved\n  throughout: first half is always written before second half.\n- stop() now guards downstreamFlushOnShutdown() with isFlushing.compareAndSet\n  to prevent a concurrent write if the background flusher is still active after\n  the 2s deadline (preserves single-writer invariant).\n- downstreamFlushOnShutdown() restores the connection's autoCommit state in a\n  finally block so pooled connections are returned cleanly.\n- DefaultSqliteEventSqlBinder.escapeJson() upgraded to full RFC-8259 compliance\n  (\\b, \\f, \\n, \\r, \\t, and \\uXXXX for U+0000–U+001F). Previously only \\\n  and \" were escaped, producing invalid JSON for MDC values with newlines.\n\n* fix: transient error handling vs poison-pill bisection, warning throttling, shutdown interrupt, and tests\n\n- Distinguish transient database/connectivity failures (SQLITE_BUSY, database locked, pool timeout)\n  from data/payload errors (constraint violations, data too long). Transient failures retry with backoff\n  without bisecting or discarding valid log entries.\n- Added warning rate-limiting (logThrottledWarn) with a 2000ms window to prevent status listener\n  recursion storms and console flooding during outages.\n- Enhanced stop() to interrupt any active flusher thread sleeping in backoff so shutdown completes\n  promptly and allows downstreamFlushOnShutdown to drain unwritten events cleanly.\n- Added comprehensive unit tests:\n  1. testPoisonPillBisectionAndChronologicalOrder: verifies that a corrupt/poison payload in a batch\n     is isolated and discarded while valid events are inserted in exact chronological order.\n  2. testTransientDatabaseErrorRetriesWithoutDiscard: verifies that transient SQLITE_BUSY / lock errors\n     back off and recover without discarding any valid events.\n\n* fix: eliminate extreme condition failure modes and false benchmark regressions\n\n- Prevent false-positive poison-pill discards during disk-full or read-only conditions\n  by adding disk, space, full, readonly, ioerr, and sqlite_cantopen to transient error classification.\n- Guard against flusher thread starvation from systematic batch-wide failures by capping bisection\n  at MAX_BISECTION_DEPTH (6). If a sub-batch fails at max depth, it is discarded in bulk rather\n  than exhaustively sleeping event-by-event for hours.\n- Prevent heap exhaustion during prolonged outages by dropping low-priority events before\n  materializing heavy stack traces and caller data via prepareForDeferredProcessing().\n- Configure benchmark-action with alert-mode: 'larger-is-better' in ci.yml so throughput\n  improvements are recognized as wins rather than regressions.",
          "timestamp": "2026-09-11T21:06:32+02:00",
          "tree_id": "83fbce2771864adc9d5f47571cfa758b47109010",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/3d95a029e71606533c7a11f58a5bc647dcd110d4"
        },
        "date": 1789153654241,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf100",
            "value": 241683.65789406808,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf500",
            "value": 246562.2204409217,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf100",
            "value": 294396.5365754673,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf500",
            "value": 332721.70562697714,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}