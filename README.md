# TradeSim: Algorithmic Stock Market Paper Trading System
**Course:** Programming in Java (25CSE0202)  
**Milestone:** CE-III: Project Milestone Evaluation (Phase 1 Minimal Viable Product)  
**Institution:** Chitkara University, School of Computing Science and Engineering  

---

## 1. Problem Statement (Evaluation Component 1 - 3 Marks)

### Context & Real-World Need
Retail investors, engineering students, and algorithmic trading enthusiasts frequently face substantial financial risk when participating in live equity markets without practical experience. Live stock exchanges impose strict execution mechanics: double-sided Limit Order Books (LOB), continuous price-time priority (FIFO), and instantaneous margin/holding validation. 

### Target Users & Problem Addressed
Existing commercial stock simulators are often bloated, closed-source, or hide the underlying order-matching data structures behind simplified market orders. **TradeSim** addresses this gap by providing an open, highly educational, in-memory algorithmic paper trading platform built from first principles in Java. It enables students and traders to:
1. Experience true Level-2 market depth with resting bids and asks.
2. Understand atomic cash/share settlement and order-queue priorities.
3. Track dynamic portfolio valuations and unrealized P&L without real financial exposure.

---

## 2. Project Objectives & Scope (Evaluation Component 2 - 2 Marks)

### Project Objectives
- **Objective 1:** Implement a double-sided Limit Order Book (LOB) matching engine adhering to strict Price-Time priority.
- **Objective 2:** Enforce pre-trade margin (cash) and position (shares) verification to prevent illegal trades.
- **Objective 3:** Implement atomic trade settlement, generating immutable transaction receipts in an audit ledger.
- **Objective 4:** Provide live dynamic portfolio valuation that recalculates net worth when market prices tick.

### Scope Boundaries: Phase 1 (MVP) vs. Phase 2

| Feature Area | Phase 1 (CE-III Milestone - Current MVP) | Phase 2 (Planned Post-Milestone) |
|---|---|---|
| **Execution Engine** | Limit Orders (BUY/SELL) with resting orders & partial fills | Market Orders, Stop-Loss, IOC/GTC expiry |
| **Order Book** | Paired `PriorityQueue` Max-Heap (Bids) & Min-Heap (Asks) | Multi-exchange order books & depth charting |
| **Data Storage** | In-memory `HashMap` & `ArrayList` data structures | Database persistence (JDBC with MySQL/SQLite) & CSV export |
| **User Management** | Predefined trader sessions (Alice & Bob) with instant switching | User authentication, password hashing, and login sessions |
| **Interface** | Interactive, formatted command-line console application | Graphical User Interface (JavaFX/Swing Desktop Dashboard) |
| **Market Data** | Simulated tick movements and manual price updates | Live market WebSocket feeds and automated trading bots |

---

## 3. Project Design & Prototype (Evaluation Component 3 - 9 Marks)

### Package & Architecture Structure
The application is organized into a modular 4-tier package architecture:

```
src/tradesim/
├── Main.java                          # Console UI, Menu Navigation & Demonstration Runner
├── exceptions/                        # Domain Exception Hierarchy
│   ├── TradeSimException.java          # Root unchecked exception (RuntimeException)
│   ├── InsufficientFundsException.java # Thrown when buyer cash < margin required
│   ├── InsufficientSharesException.java# Thrown when seller shares < order quantity
│   ├── InvalidOrderException.java      # Thrown on non-positive quantity, price, or nulls
│   ├── StockNotFoundException.java     # Thrown when querying an unregistered ticker
│   └── UserNotFoundException.java      # Thrown when user/portfolio lookup fails
├── models/                            # Data Models & Limit Order Book Structures
│   ├── OrderType.java                  # Enum (BUY, SELL)
│   ├── OrderStatus.java                # Enum (PENDING, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED)
│   ├── Stock.java                      # Equities model with live/base prices, volume & high/low
│   ├── User.java                       # Trader account entity with debit/credit balance operations
│   ├── PortfolioHolding.java           # Position tracking with weighted average buy price & realized P&L
│   ├── Portfolio.java                  # Container managing trader holdings via HashMap
│   ├── Order.java                      # Abstract base order implementing Comparable<Order>
│   ├── BuyOrder.java                   # Max-Heap priority (Highest Price first; tie-breaker: FIFO timestamp)
│   ├── SellOrder.java                  # Min-Heap priority (Lowest Price first; tie-breaker: FIFO timestamp)
│   ├── OrderBook.java                  # Paired PriorityQueue<BuyOrder> and PriorityQueue<SellOrder>
│   └── TransactionReceipt.java         # Immutable settled trade record with microsecond timestamp
└── services/                          # Algorithmic Business Logic Engine
    ├── OrderBookManager.java           # Registry mapping stock tickers to OrderBooks
    ├── TradeSettlementService.java     # Atomic debit/credit of funds & holdings + ledger recording
    ├── OrderMatchingEngine.java        # Price-time matching, resting order price rule & partial fill handling
    └── PortfolioValuationService.java  # Real-time stock valuation & P&L calculation without Stream API
```

### Application Flow & Core Functionality
1. **Order Submission:** User submits a Limit Order (e.g., BUY 10 RELIANCE @ Rs. 2,900.00).
2. **Pre-Trade Validation:** `TradeSettlementService` verifies cash margin for buys or share positions for sells; rejects invalid orders fast.
3. **Queue Placement:** Order is routed to the ticker's `OrderBook` and enqueued in the respective `PriorityQueue`.
4. **Matching Engine:**
   - Evaluates `bestBid` vs. `bestAsk`.
   - Matches when `bestBid.getLimitPrice() >= bestAsk.getLimitPrice()`.
   - **Resting Order Rule:** The order that was already resting in the book dictates the execution price.
   - **Partial Fill Rule:** Fills `Math.min(bidQty, askQty)`. The fully filled order is polled (`FILLED`); remaining quantity stays at queue head (`PARTIALLY_FILLED`).
5. **Settlement & Audit:** Buyer cash is debited, seller cash is credited, holdings are updated, and an immutable `TransactionReceipt` is logged.

---

## 4. Integration of All Course Concepts (Evaluation Component 4 - 6 Marks)

The prototype strictly integrates core Object-Oriented Programming concepts from the course syllabus:

| Syllabus Concept | Implementation in TradeSim |
|---|---|
| **Classes, Objects & Constructors** | Encapsulated models (`Stock`, `User`, `PortfolioHolding`, `Order`) with private state and parameter validation. |
| **Static Members** | `static int orderCounter` in `Main` and `static int receiptCounter` in `TradeSettlementService` for unique sequence IDs. |
| **Inheritance & Abstraction** | `public abstract class Order` extended by `BuyOrder` and `SellOrder`. |
| **Polymorphism & Interfaces** | `Order implements Comparable<Order>`, overriding `compareTo()` for heap ordering; overridden `toString()`. |
| **String & StringBuilder** | Extensive string formatting and `StringBuilder` in `PortfolioValuationService` for tabular reports. |
| **Custom Exceptions** | Hierarchical custom unchecked exceptions (`TradeSimException` $\rightarrow$ `InsufficientFundsException`, etc.) with `try-catch`. |
| **Java Collections Framework** | - **`ArrayList`**: Dynamic lists of top bids, top asks, and transaction ledger entries.<br>- **`HashMap`**: Fast key-value lookups for registered stocks, order books, users, and holdings.<br>- **`PriorityQueue`**: Dual priority queues implementing LOB Max-Heap (Bids) and Min-Heap (Asks).<br>- **`Collections.sort()`**: Sorting order book depth snapshots based on `Comparable`. |
| **Iterators & Loops** | Standard, clean `for` and `while` loops (strictly zero Stream API). |

---

## 5. Code Quality, Setup & Milestone Evidence (Evaluation Component 5 - 5 Marks)

### Setup & Compilation

#### Step 1: Compile All Java Classes
```powershell
& 'C:\Program Files\Java\jdk-21.0.11\bin\javac.exe' -encoding UTF-8 -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
```

#### Step 2: Run Interactive Console Paper Trading (MVP)
```powershell
& 'C:\Program Files\Java\jdk-21.0.11\bin\java.exe' -cp bin tradesim.Main
```

### Verified Milestone Evidence (Interactive Console Paper Trading)
The interactive console provides a complete end-to-end paper trading experience:
1. **Market Watch & Level-2 Depth:** View live quotes across 5 NSE equities with resting bid/ask queues.
2. **Instant Order Execution:** Placed BUY/SELL orders match against resting counterparties in real time.
3. **Dynamic Balance & Position Updates:** Cash balance debits/credits automatically upon execution, and position holdings update immediately.
4. **Resting Orders & Partial Fills:** Orders with limit prices that do not cross counterparties rest cleanly in the PriorityQueue books with status tracking.
5. **Dynamic Portfolio Valuation:** Real-time Net Worth, invested capital, and unrealized P&L recalculate dynamically when market prices tick (strictly zero Stream API).
6. **Audit Ledger:** Every settled trade is immutably recorded in the transaction ledger with unique receipt IDs and execution timestamps.
