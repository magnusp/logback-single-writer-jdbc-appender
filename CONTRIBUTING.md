# Contributing to logback-single-writer-jdbc-appender

Thank you for your interest in contributing to `logback-single-writer-jdbc-appender`! We welcome bug reports, improvements, documentation updates, and feature suggestions.

## Code of Conduct

Please follow our [Code of Conduct](CODE_OF_CONDUCT.md) in all community interactions.

## Getting Started

1. **Prerequisites**:
   - Install [mise](https://mise.jdx.dev/) (recommended) to automatically manage Amazon Corretto Java 25 and Maven 3.9+, or install them manually:
     ```bash
     mise install
     ```
2. **Fork and Clone**:
   - Fork this repository on GitHub.
   - Clone your fork locally and create a new feature branch:
     ```bash
     git checkout -b feature/my-new-feature
     ```

## Development Workflow

### Running Tests
Make sure all unit tests pass before submitting changes:
```bash
mvn clean test
```

### Running Benchmarks
Before and after making changes that could impact throughput or latency, run JMH benchmarks:
```bash
mvn test -Pbenchmark
```

## Pull Request Guidelines

1. Ensure the code compiles against Java 25.
2. Add unit tests for any bug fixes or new features.
3. Keep pull requests focused and atomic.
4. Ensure CI checks (compilation, unit tests, and JMH benchmark evaluation) pass.
