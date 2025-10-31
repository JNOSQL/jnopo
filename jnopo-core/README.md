# JNOPO Core

> The core engine for the JNOPO game platform — robust, modular, and ready for integration.

---

## Overview

JNOPO Core provides the essential logic and domain model for building multiplayer, turn-based games. Designed for extensibility and performance, it powers the main game loop, player management, and state transitions for the JNOPO ecosystem.

> [!TIP]
> This module is intended to be used as a library by higher-level applications (such as web, cloud, or desktop frontends).

---

## Features

- **Immutable domain model** using Java Records
- **Game state management** for multiplayer sessions
- **Pattern matching** and modern Java 21 features
- **Extensible player and movement logic**
- **Comprehensive unit tests** with JUnit 5 and AssertJ
- **No external runtime dependencies** (pure Java)

---

## Getting Started

### Prerequisites

- Java 21 or newer
- Maven 3.8+ (recommended)

### Build & Test

```bash
# Build the module
cd jnopo-core
./mvnw clean install

# Run unit tests
./mvnw test
```

### Usage Example

```java
import br.org.soujava.coffewithjava.jnopo.core.*;

Player alice = Player.of("1", "Alice");
Player bob = Player.of("2", "Bob");

Game game = new Game();
GameState waiting = game.newGame(alice);
GameState ready = game.newGame(bob);

GameState result = game.playGame(ready.gameId(), alice, Movement.ROCK);
```

---

## Project Structure

```
jnopo-core/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── br/org/soujava/coffewithjava/jnopo/core/
│   └── test/
│       └── java/
│           └── br/org/soujava/coffewithjava/jnopo/core/
├── pom.xml
```

- **Game.java**: Main game engine and state transitions
- **Player.java**: Immutable player record
- **Movement.java**: Enum for game moves (Rock, Paper, Scissors)
- **GameState.java**: Interface for all game states
- **GameReady, GameRunning, GameOver, WaitingPlayers, GameAbandoned**: State records

---

## Design Highlights

- **Immutability**: All domain objects are immutable for thread safety and predictability.
- **Pattern Matching**: Uses Java 21's switch expressions for concise state handling.
- **Extensibility**: Easily add new game states or movement types.
- **Test Coverage**: All core logic is covered by unit tests.

---

## Admonitions

> [!IMPORTANT]
> JNOPO Core does not provide networking, persistence, or UI. It is designed to be embedded in your own application stack.

> [!NOTE]
> For integration examples, see the `jnopo-game` and `jnopo-talk` modules in the main repository.

---

## Community & Support

- [JNOSQL Repository](https://github.com/jnosql)
- Issues and feature requests: Use the GitHub Issues tab

---

## Quick Links

- [API Documentation](#) <!-- Add link if available -->
- [JNOSQL Main Repository](https://github.com/jnosql/jnopo)
- [Java 21 Migration Guide](.github/instructions/java-17-to-java-21-upgrade.instructions.md)

---

_Enjoy building with JNOPO Core!_
