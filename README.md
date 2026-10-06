# Jover IntelliJ Plugin

IntelliJ IDEA support for the operator semantics implemented by Jover.

## Semantics

Jover transforms:

- `a + b` -> `a.add(b)`
- `a - b` -> `a.subtract(b)`
- `a * b` -> `a.multiply(b)`
- `a / b` -> `a.divide(b)`

The plugin mirrors these rules in Java PSI.

It intentionally does not reinterpret:

- primitive left operands;
- `String` left operands.

## Requirements

- IntelliJ IDEA 2026.1.3
- Java 21
- Gradle 9+
- IntelliJ Platform Gradle Plugin 2.x

## Run

```bash
./gradlew runIde
```

The Gradle task launches a development instance of IntelliJ IDEA with the plugin installed.

## Tests

```bash
./gradlew test
```

## Build plugin

```bash
./gradlew buildPlugin
```

The ZIP is produced under `build/distributions`.

## Architecture

`JoverOperatorService`
: single source of truth for operator -> method mapping and method resolution.

`JoverReferenceContributor`
: exposes `a + b` as a PSI reference to `add(Money)`. This is important because it makes normal IDEA method-reference searches see operator usages.

`JoverHighlightInfoFilter`
: removes the Java "operator cannot be applied" error only when the Jover method resolves successfully.

`JoverImplicitUsageProvider`
: marks Jover operator method names as alternative names, forcing IDEA's unused analysis to perform a reference search. The actual usage is supplied by `JoverReferenceContributor`.

## Important limitation

This is intentionally an MVP. Java's complete overload/generic/varargs resolution is complicated. The resolver handles common inheritance, overloads, assignability, boxing/widening and varargs, but should be extended with dedicated tests if Jover itself is expected to support advanced generic method inference.
