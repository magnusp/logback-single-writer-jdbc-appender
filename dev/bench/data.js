window.BENCHMARK_DATA = {
  "lastUpdate": 1789150441535,
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
      }
    ]
  }
}