window.BENCHMARK_DATA = {
  "lastUpdate": 1789151453160,
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
      }
    ]
  }
}