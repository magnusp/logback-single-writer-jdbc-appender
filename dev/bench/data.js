window.BENCHMARK_DATA = {
  "lastUpdate": 1789154294619,
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
          "id": "5d52b12211470e2fa9cd8086f7fcd5014fe62dfa",
          "message": "chore(deps): configure Dependabot groups for minor and patch updates (#21)",
          "timestamp": "2026-09-11T21:10:52+02:00",
          "tree_id": "54742cbff0191bd75251d0d3e4df7bf7134d8cce",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/5d52b12211470e2fa9cd8086f7fcd5014fe62dfa"
        },
        "date": 1789153915003,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf100",
            "value": 55989.037850880435,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf500",
            "value": 57531.54577407631,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf100",
            "value": 53232.30810116078,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf500",
            "value": 121291.72574472148,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "49699333+dependabot[bot]@users.noreply.github.com",
            "name": "dependabot[bot]",
            "username": "dependabot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e92f5667a000991d00c7e0ba8c0228db3ab33a65",
          "message": "build(deps): bump the minor-and-patch group with 8 updates (#22)\n\nBumps the minor-and-patch group with 8 updates:\n\n| Package | From | To |\n| --- | --- | --- |\n| [ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) | `1.5.18` | `1.6.3` |\n| org.slf4j:slf4j-api | `2.0.17` | `2.0.19` |\n| [org.assertj:assertj-core](https://github.com/assertj/assertj) | `3.27.3` | `3.27.7` |\n| [org.xerial:sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) | `3.49.1.0` | `3.53.4.0` |\n| [org.apache.maven.plugins:maven-compiler-plugin](https://github.com/apache/maven-compiler-plugin) | `3.14.0` | `3.16.0` |\n| [org.apache.maven.plugins:maven-surefire-plugin](https://github.com/apache/maven-surefire) | `3.5.2` | `3.6.0` |\n| [org.codehaus.mojo:build-helper-maven-plugin](https://github.com/mojohaus/build-helper-maven-plugin) | `3.6.0` | `3.6.1` |\n| [org.codehaus.mojo:exec-maven-plugin](https://github.com/mojohaus/exec-maven-plugin) | `3.5.0` | `3.6.3` |\n\n\nUpdates `ch.qos.logback:logback-classic` from 1.5.18 to 1.6.3\n- [Release notes](https://github.com/qos-ch/logback/releases)\n- [Commits](https://github.com/qos-ch/logback/compare/v_1.5.18...v_1.6.3)\n\nUpdates `org.slf4j:slf4j-api` from 2.0.17 to 2.0.19\n\nUpdates `org.assertj:assertj-core` from 3.27.3 to 3.27.7\n- [Release notes](https://github.com/assertj/assertj/releases)\n- [Commits](https://github.com/assertj/assertj/compare/assertj-build-3.27.3...assertj-build-3.27.7)\n\nUpdates `org.xerial:sqlite-jdbc` from 3.49.1.0 to 3.53.4.0\n- [Release notes](https://github.com/xerial/sqlite-jdbc/releases)\n- [Changelog](https://github.com/xerial/sqlite-jdbc/blob/master/CHANGELOG)\n- [Commits](https://github.com/xerial/sqlite-jdbc/compare/3.49.1.0...3.53.4.0)\n\nUpdates `org.apache.maven.plugins:maven-compiler-plugin` from 3.14.0 to 3.16.0\n- [Release notes](https://github.com/apache/maven-compiler-plugin/releases)\n- [Commits](https://github.com/apache/maven-compiler-plugin/compare/maven-compiler-plugin-3.14.0...maven-compiler-plugin-3.16.0)\n\nUpdates `org.apache.maven.plugins:maven-surefire-plugin` from 3.5.2 to 3.6.0\n- [Release notes](https://github.com/apache/maven-surefire/releases)\n- [Commits](https://github.com/apache/maven-surefire/compare/surefire-3.5.2...surefire-3.6.0)\n\nUpdates `org.codehaus.mojo:build-helper-maven-plugin` from 3.6.0 to 3.6.1\n- [Release notes](https://github.com/mojohaus/build-helper-maven-plugin/releases)\n- [Commits](https://github.com/mojohaus/build-helper-maven-plugin/compare/3.6.0...3.6.1)\n\nUpdates `org.codehaus.mojo:exec-maven-plugin` from 3.5.0 to 3.6.3\n- [Release notes](https://github.com/mojohaus/exec-maven-plugin/releases)\n- [Commits](https://github.com/mojohaus/exec-maven-plugin/compare/3.5.0...3.6.3)\n\n---\nupdated-dependencies:\n- dependency-name: ch.qos.logback:logback-classic\n  dependency-version: 1.6.3\n  dependency-type: direct:production\n  update-type: version-update:semver-minor\n  dependency-group: minor-and-patch\n- dependency-name: org.slf4j:slf4j-api\n  dependency-version: 2.0.19\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n  dependency-group: minor-and-patch\n- dependency-name: org.assertj:assertj-core\n  dependency-version: 3.27.7\n  dependency-type: direct:development\n  update-type: version-update:semver-patch\n  dependency-group: minor-and-patch\n- dependency-name: org.xerial:sqlite-jdbc\n  dependency-version: 3.53.4.0\n  dependency-type: direct:development\n  update-type: version-update:semver-minor\n  dependency-group: minor-and-patch\n- dependency-name: org.apache.maven.plugins:maven-compiler-plugin\n  dependency-version: 3.16.0\n  dependency-type: direct:development\n  update-type: version-update:semver-minor\n  dependency-group: minor-and-patch\n- dependency-name: org.apache.maven.plugins:maven-surefire-plugin\n  dependency-version: 3.6.0\n  dependency-type: direct:development\n  update-type: version-update:semver-minor\n  dependency-group: minor-and-patch\n- dependency-name: org.codehaus.mojo:build-helper-maven-plugin\n  dependency-version: 3.6.1\n  dependency-type: direct:development\n  update-type: version-update:semver-patch\n  dependency-group: minor-and-patch\n- dependency-name: org.codehaus.mojo:exec-maven-plugin\n  dependency-version: 3.6.3\n  dependency-type: direct:development\n  update-type: version-update:semver-minor\n  dependency-group: minor-and-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2026-09-11T19:13:22Z",
          "tree_id": "007bdcb87ea1d5a3ffc63def5217e1c2592b20eb",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/e92f5667a000991d00c7e0ba8c0228db3ab33a65"
        },
        "date": 1789154074326,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf100",
            "value": 214424.3477919236,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf500",
            "value": 268829.35647778836,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf100",
            "value": 273992.0396225836,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf500",
            "value": 317351.81531089864,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "49699333+dependabot[bot]@users.noreply.github.com",
            "name": "dependabot[bot]",
            "username": "dependabot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5d461955d34aba95e3d98e6850b612661a9fd794",
          "message": "build(deps-dev): bump org.junit.jupiter:junit-jupiter (#8)\n\nBumps [org.junit.jupiter:junit-jupiter](https://github.com/junit-team/junit-framework) from 5.12.0 to 6.1.3.\n- [Release notes](https://github.com/junit-team/junit-framework/releases)\n- [Commits](https://github.com/junit-team/junit-framework/compare/r5.12.0...r6.1.3)\n\n---\nupdated-dependencies:\n- dependency-name: org.junit.jupiter:junit-jupiter\n  dependency-version: 6.1.3\n  dependency-type: direct:development\n  update-type: version-update:semver-major\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2026-09-11T19:17:04Z",
          "tree_id": "1baaf96ad4022ee14e10831a9e82a1bf8db02826",
          "url": "https://github.com/magnusp/logback-single-writer-jdbc-appender/commit/5d461955d34aba95e3d98e6850b612661a9fd794"
        },
        "date": 1789154294153,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf100",
            "value": 237908.30007121956,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteJsonBatchOf500",
            "value": 278098.2385110191,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf100",
            "value": 284532.0916947289,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.github.magnusp.logback.resilientjdbc.ResilientJdbcAppenderBenchmark.sqliteRelationalBatchOf500",
            "value": 323579.81412611017,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}