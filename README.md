# TradeSim: Algorithmic Stock Market Paper Trading & Portfolio Tracker

TradeSim is a console-based, high-performance execution engine that simulates live buy and sell limit orders for NSE/BSE stocks. The system provides a risk-free environment for algorithmic paper trading, utilizing background multithreading to simulate real-time intraday market volatility and an advanced order matching engine powered by priority queues to settle trades asynchronously based on strict price-time priority.

---

## Key Features

1. **Dual PriorityQueue Order Matching Engine**:
   - Maintains Level-2 Limit Order Books (LOB) per ticker.
   - **Bids (Buy Queue)**: Max-Heap prioritized by highest limit price.
   - **Asks (Sell Queue)**: Min-Heap prioritized by lowest limit price.
   - **Tie-Breaker**: Strict Price-Time priority (FIFO) using high-resolution timestamps.

2. **Real-Time Intraday Volatility Simulator**:
   - Employs a background `ScheduledExecutorService` ticking every 2–3 seconds without blocking the terminal session.
   - Mathematical volatility models (Gaussian Random Walk $\pm 1.0\%$, Jump-Diffusion shocks).
   - Thread-safe design leveraging `volatile` price states and `ConcurrentHashMap`.

3. **Dynamic Portfolio Valuation (Stream API)**:
   - Instantaneous portfolio net worth and unrealized P&L calculations via functional streams (`.entrySet().stream().mapToDouble().sum()`).

4. **Robust Persistence & Design Patterns**:
   - **Persistence**: Relational MySQL schema via the Data Access Object (DAO) pattern with atomic transaction rollbacks (`conn.setAutoCommit(false)`).
   - **Command Pattern**: Decoupled terminal interface (`com.tradesim.ui`).
   - **Strategy Pattern**: Interchangeable volatility algorithms and SEBI/flat-fee brokerage models.
   - **Exception Handling**: 10+ custom domain exceptions.

---

## Project Structure

```
TradeSim/
├── bin/                       # Compiled bytecode
├── src/
│   └── tradesim/
│       ├── exceptions/        # Custom domain & concurrency exceptions
│       ├── models/            # Core state entities (User, Stock, Order, OrderBook)
│       ├── services/          # Matching engine, simulation, valuation
│       ├── dao/               # JDBC database persistence layer
│       ├── strategies/        # Volatility & brokerage strategies
│       ├── ui/                # Command pattern terminal interface
│       ├── utils/             # Helpers (Security, AnsiConsole, TableFormatter)
│       └── Main.java          # Entry point
├── .gitignore
└── README.md
```

---

## Two-Phase Execution Roadmap

- **Phase 1: The Offline Algorithmic MVP (Days 1–4)**
  - PriorityQueue matching mechanics, price-time sorting, partial fills.
  - Functional Stream API portfolio valuation.
  - In-memory data collections.
- **Phase 2: Enterprise Scaling & Persistence (Days 5–10)**
  - MySQL relational schema and JDBC DAO persistence.
  - Multithreaded background volatility simulation.
  - Strategy & Command design patterns and Level-2 market depth display.

---

## Compilation & Execution

```powershell
# Compile all Java sources
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName

# Run TradeSim
java -cp bin tradesim.Main
```
