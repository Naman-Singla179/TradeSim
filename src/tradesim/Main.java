package tradesim;

import tradesim.exceptions.*;
import tradesim.models.*;
import tradesim.services.*;

import java.util.*;

public class Main {
    private static int orderCounter = 100;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            runInteractiveConsole(scanner);
        }
    }

    private static void runInteractiveConsole(Scanner scanner) {
        OrderBookManager bookManager = new OrderBookManager();
        TradeSettlementService settlementService = new TradeSettlementService();
        OrderMatchingEngine matchingEngine = new OrderMatchingEngine(bookManager, settlementService);
        PortfolioValuationService valuationService = new PortfolioValuationService();

        Map<String, Stock> marketQuotes = new HashMap<>();
        registerStock(bookManager, marketQuotes, new Stock("RELIANCE", "Reliance Industries Ltd", "NSE", 2900.00));
        registerStock(bookManager, marketQuotes, new Stock("TCS", "Tata Consultancy Services Ltd", "NSE", 3800.00));
        registerStock(bookManager, marketQuotes, new Stock("INFY", "Infosys Ltd", "NSE", 1500.00));
        registerStock(bookManager, marketQuotes, new Stock("HDFCBANK", "HDFC Bank Ltd", "NSE", 1650.00));
        registerStock(bookManager, marketQuotes, new Stock("TATAMOTORS", "Tata Motors Ltd", "NSE", 980.00));

        User marketDesk = new User("USR-MARKET", "Market Liquidity Desk", 10000000.00);
        Portfolio marketPort = new Portfolio(marketDesk.getUserId());
        marketPort.recordBuy("RELIANCE", 1000, 2900.00);
        marketPort.recordBuy("TCS", 1000, 3800.00);
        marketPort.recordBuy("INFY", 1000, 1500.00);
        marketPort.recordBuy("HDFCBANK", 1000, 1650.00);
        marketPort.recordBuy("TATAMOTORS", 1000, 980.00);
        settlementService.registerUser(marketDesk, marketPort);

        User alice = new User("USR-ALICE", "Alice", 200000.00);
        Portfolio alicePort = new Portfolio(alice.getUserId());
        settlementService.registerUser(alice, alicePort);

        User bob = new User("USR-BOB", "Bob", 100000.00);
        Portfolio bobPort = new Portfolio(bob.getUserId());
        bobPort.recordBuy("RELIANCE", 50, 2800.00);
        bobPort.recordBuy("TCS", 25, 3700.00);
        settlementService.registerUser(bob, bobPort);

        seedInitialMarketLiquidity(matchingEngine, marketDesk.getUserId());

        User activeUser = alice;
        Portfolio activePortfolio = alicePort;

        System.out.println("================================================================================");
        System.out.println("            WELCOME TO TRADESIM - ALGORITHMIC STOCK PAPER TRADING               ");
        System.out.println("================================================================================");
        System.out.println("  Welcome, Trader! You are connected to the Live Market Simulator.");
        System.out.println("  Practice trading equities in real-time, view Level-2 Market Depth,");
        System.out.println("  manage your portfolio, and explore automated order book matching.");
        System.out.println("================================================================================\n");

        boolean running = true;
        while (running) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  Current Trader : %s [%s] | Cash: Rs. %,.2f%n",
                activeUser.getUsername(), activeUser.getUserId(), activeUser.getCashBalance());
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("  MAIN MENU:");
            System.out.println("   1. View Market Watch (Live Stock Quotes)");
            System.out.println("   2. View Order Book Depth (Level-2 LOB Bids & Asks)");
            System.out.println("   3. Place BUY Limit Order");
            System.out.println("   4. Place SELL Limit Order");
            System.out.println("   5. View My Portfolio & Live Valuation (Dynamic P&L)");
            System.out.println("   6. View Audit Transaction Ledger (Settled Trades)");
            System.out.println("   7. Switch Active Trader (Alice / Bob)");
            System.out.println("   8. Simulate Market Price Movement (Intraday Tick)");
            System.out.println("   0. Exit System");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option [0-8]: ");

            if (!scanner.hasNextLine()) break;
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleMarketWatch(marketQuotes, bookManager);
                    break;
                case "2":
                    handleMarketDepth(scanner, bookManager, marketQuotes);
                    break;
                case "3":
                    handlePlaceOrder(scanner, activeUser, activePortfolio, OrderType.BUY, bookManager, matchingEngine, marketQuotes);
                    break;
                case "4":
                    handlePlaceOrder(scanner, activeUser, activePortfolio, OrderType.SELL, bookManager, matchingEngine, marketQuotes);
                    break;
                case "5":
                    System.out.println("\n" + valuationService.generateValuationReport(activeUser, activePortfolio, marketQuotes));
                    break;
                case "6":
                    handleAuditLedger(settlementService);
                    break;
                case "7":
                    activeUser = handleSwitchUser(scanner, settlementService, activeUser);
                    activePortfolio = settlementService.getPortfolio(activeUser.getUserId());
                    break;
                case "8":
                    handlePriceTick(scanner, marketQuotes);
                    break;
                case "0":
                    running = false;
                    System.out.println("\nThank you for using TradeSim Paper Trading System. Happy Trading!");
                    break;
                default:
                    System.out.println("\n[!] Invalid selection: '" + choice + "'. Please choose an option from 0 to 8.");
            }
            System.out.println();
        }
    }

    private static void registerStock(OrderBookManager bookManager, Map<String, Stock> quotes, Stock stock) {
        quotes.put(stock.getTicker(), stock);
        bookManager.registerStock(stock.getTicker());
    }

    private static void seedInitialMarketLiquidity(OrderMatchingEngine engine, String marketUserId) {
        engine.submitOrder(new SellOrder("ORD-MKT-S1", marketUserId, "RELIANCE", 50, 2900.00));
        engine.submitOrder(new BuyOrder("ORD-MKT-B1", marketUserId, "RELIANCE", 30, 2850.00));

        engine.submitOrder(new SellOrder("ORD-MKT-S2", marketUserId, "TCS", 30, 3800.00));
        engine.submitOrder(new BuyOrder("ORD-MKT-B2", marketUserId, "TCS", 20, 3750.00));

        engine.submitOrder(new SellOrder("ORD-MKT-S3", marketUserId, "INFY", 50, 1500.00));
        engine.submitOrder(new BuyOrder("ORD-MKT-B3", marketUserId, "INFY", 30, 1480.00));

        engine.submitOrder(new SellOrder("ORD-MKT-S4", marketUserId, "HDFCBANK", 50, 1650.00));
        engine.submitOrder(new BuyOrder("ORD-MKT-B4", marketUserId, "HDFCBANK", 30, 1620.00));

        engine.submitOrder(new SellOrder("ORD-MKT-S5", marketUserId, "TATAMOTORS", 50, 980.00));
        engine.submitOrder(new BuyOrder("ORD-MKT-B5", marketUserId, "TATAMOTORS", 30, 960.00));
    }

    private static int promptInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) return -1;
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    private static double promptDouble(Scanner scanner, String prompt, double defaultValue) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) return defaultValue;
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }
 
    private static void handleMarketWatch(Map<String, Stock> quotes, OrderBookManager bookManager) {
        System.out.println("\n================================== MARKET WATCH ==================================");
        System.out.printf("%-10s %-30s %-8s %-14s %-10s %-14s %-14s %-8s%n",
            "Ticker", "Company Name", "Exchange", "Live Price", "Change %", "Day High", "Day Low", "Orders");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        for (Stock s : quotes.values()) {
            OrderBook book = bookManager.getOrderBook(s.getTicker());
            System.out.printf("%-10s %-30s %-8s Rs. %,10.2f %+8.2f%% Rs. %,10.2f Rs. %,10.2f B:%-2d | A:%-2d%n",
                s.getTicker(), s.getCompanyName(), s.getExchange(), s.getCurrentPrice(),
                s.getPercentChange(), s.getDayHigh(), s.getDayLow(),
                book.getBuyOrderCount(), book.getSellOrderCount());
        }
        System.out.println("==========================================================================================================");
    }

    private static void handleMarketDepth(Scanner scanner, OrderBookManager bookManager, Map<String, Stock> quotes) {
        System.out.print("\nEnter Stock Ticker [Default: RELIANCE]: ");
        if (!scanner.hasNextLine()) return;
        String line = scanner.nextLine().trim().toUpperCase();
        String ticker = line.isEmpty() ? "RELIANCE" : line;

        if (!bookManager.hasStock(ticker)) {
            System.out.println("Error: Ticker '" + ticker + "' is not registered.");
            return;
        }

        OrderBook book = bookManager.getOrderBook(ticker);
        Stock stock = quotes.get(ticker);
        System.out.println("\n================ LEVEL-2 ORDER BOOK: " + ticker + " ================");
        System.out.printf("Live Price: Rs. %,.2f | Volume: %,d%n",
            (stock != null ? stock.getCurrentPrice() : 0.0), (stock != null ? stock.getVolume() : 0));
        System.out.println("----------------------------------------------------------------");
        System.out.println("BIDS (Buy Orders - Max-Heap):");
        List<BuyOrder> bids = book.getTopBids(5);
        if (bids.isEmpty()) System.out.println("  (No resting bids)");
        for (BuyOrder b : bids) {
            System.out.printf("  [%s] %d shares @ Rs. %,.2f%n", b.getOrderId(), b.getRemainingQuantity(), b.getLimitPrice());
        }

        System.out.println("\nASKS (Sell Orders - Min-Heap):");
        List<SellOrder> asks = book.getTopAsks(5);
        if (asks.isEmpty()) System.out.println("  (No resting asks)");
        for (SellOrder a : asks) {
            System.out.printf("  [%s] %d shares @ Rs. %,.2f%n", a.getOrderId(), a.getRemainingQuantity(), a.getLimitPrice());
        }
        System.out.println("================================================================");
    }

    private static void handlePlaceOrder(Scanner scanner, User user, Portfolio portfolio, OrderType type,
                                         OrderBookManager bookManager, OrderMatchingEngine engine,
                                         Map<String, Stock> marketQuotes) {
        System.out.printf("%n--- PLACE %s LIMIT ORDER ---%n", type);
        System.out.print("Enter Stock Ticker (e.g. RELIANCE, TCS, INFY): ");
        if (!scanner.hasNextLine()) return;
        String ticker = scanner.nextLine().trim().toUpperCase();

        if (!bookManager.hasStock(ticker)) {
            System.out.println("Error: Ticker '" + ticker + "' is not registered.");
            return;
        }

        Stock stock = marketQuotes.get(ticker);
        double livePrice = (stock != null) ? stock.getCurrentPrice() : 1000.0;
        System.out.printf("Selected: %s | Live Reference Price: Rs. %,.2f%n", ticker, livePrice);

        int quantity = promptInt(scanner, "Enter Quantity (shares): ");
        if (quantity <= 0) {
            System.out.println("Error: Quantity must be a positive integer.");
            return;
        }

        double limitPrice = promptDouble(scanner,
            String.format("Enter Limit Price (Rs.) [Press Enter for live Rs. %,.2f]: ", livePrice), livePrice);
        if (limitPrice <= 0.0) {
            System.out.println("Error: Limit price must be a positive number.");
            return;
        }

        if (type == OrderType.BUY) {
            double requiredMargin = quantity * limitPrice;
            if (user.getCashBalance() < requiredMargin) {
                System.out.printf("Order Rejected: Insufficient Funds. Required: Rs. %,.2f, Available: Rs. %,.2f%n",
                    requiredMargin, user.getCashBalance());
                return;
            }
        } else {
            PortfolioHolding holding = portfolio.getHolding(ticker);
            int availableShares = (holding != null) ? holding.getQuantity() : 0;
            if (availableShares < quantity) {
                System.out.printf("Order Rejected: Insufficient Shares. Requested: %d, Available: %d%n",
                    quantity, availableShares);
                return;
            }
        }

        orderCounter++;
        String orderId = String.format("ORD-%s-%d", (type == OrderType.BUY ? "B" : "S"), orderCounter);

        try {
            Order order = (type == OrderType.BUY)
                ? new BuyOrder(orderId, user.getUserId(), ticker, quantity, limitPrice)
                : new SellOrder(orderId, user.getUserId(), ticker, quantity, limitPrice);

            System.out.printf("Submitting: [%s] %s %d %s @ Rs. %,.2f...%n", orderId, type, quantity, ticker, limitPrice);
            List<TransactionReceipt> receipts = engine.submitOrder(order);

            if (receipts.isEmpty()) {
                System.out.printf("[RESTING ORDER] Limit price (Rs. %,.2f) did not cross counterparty book. Order is sitting in the order book.%n", limitPrice);
                System.out.printf("Current Cash Balance: Rs. %,.2f (Cash is only debited upon trade execution)%n", user.getCashBalance());
            } else {
                int filledQty = 0;
                for (TransactionReceipt r : receipts) {
                    filledQty += r.getExecutedQuantity();
                    System.out.printf("==> TRADE EXECUTED! %d shares of %s matched @ Rs. %,.2f | Total: Rs. %,.2f (Receipt: %s)%n",
                        r.getExecutedQuantity(), r.getTicker(), r.getExecutedPrice(), r.getTotalAmount(), r.getReceiptId());
                    if (stock != null) {
                        stock.updatePrice(r.getExecutedPrice());
                        stock.addVolume(r.getExecutedQuantity());
                    }
                }

                if (filledQty < quantity) {
                    System.out.printf("==> Partial Fill! %d/%d shares filled. Remaining %d shares rest as %s.%n",
                        filledQty, quantity, (quantity - filledQty), order.getStatus());
                } else {
                    System.out.printf("==> Complete Fill! All %d shares filled successfully.%n", quantity);
                }
                System.out.printf("==> Updated Cash Balance : Rs. %,.2f%n", user.getCashBalance());
            }
        } catch (TradeSimException e) {
            System.out.println("Execution Error: " + e.getMessage());
        }
    }

    private static void handleAuditLedger(TradeSettlementService settlementService) {
        List<TransactionReceipt> ledger = settlementService.getTransactionLedger();
        System.out.println("\n=========================== AUDIT TRANSACTION LEDGER ===========================");
        if (ledger.isEmpty()) {
            System.out.println("No trades have settled yet.");
        } else {
            System.out.printf("%-14s %-10s %-8s %-14s %-16s %-28s %-12s <-> %-12s%n",
                "Receipt ID", "Ticker", "Qty", "Price", "Total", "Execution Time", "Buy Order", "Sell Order");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            for (TransactionReceipt r : ledger) {
                System.out.printf("%-14s %-10s %-8d Rs. %,10.2f Rs. %,12.2f %-28s %-12s <-> %-12s%n",
                    r.getReceiptId(), r.getTicker(), r.getExecutedQuantity(),
                    r.getExecutedPrice(), r.getTotalAmount(), new Date(r.getExecutionTime()),
                    r.getBuyOrderId(), r.getSellOrderId());
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            System.out.printf("Total Settled Trades: %d%n", ledger.size());
        }
        System.out.println("=================================================================================");
    }

    private static User handleSwitchUser(Scanner scanner, TradeSettlementService settlementService, User activeUser) {
        System.out.println("\n--- SWITCH TRADER ACCOUNT ---");
        List<User> traders = new ArrayList<>();
        for (User u : settlementService.getAllUsers().values()) {
            if (!u.getUserId().equals("USR-MARKET")) {
                traders.add(u);
            }
        }
        for (int i = 0; i < traders.size(); i++) {
            User u = traders.get(i);
            String mark = u.getUserId().equals(activeUser.getUserId()) ? " (ACTIVE)" : "";
            System.out.printf("  %d. %s [%s] - Cash: Rs. %,.2f%s%n", (i + 1), u.getUsername(), u.getUserId(), u.getCashBalance(), mark);
        }

        int choice = promptInt(scanner, "Select Trader Option: ");
        if (choice >= 1 && choice <= traders.size()) {
            User selected = traders.get(choice - 1);
            System.out.println("Switched active trader to: " + selected.getUsername());
            return selected;
        }
        System.out.println("Invalid selection. Keeping current trader.");
        return activeUser;
    }

    private static void handlePriceTick(Scanner scanner, Map<String, Stock> marketQuotes) {
        System.out.print("\nEnter Stock Ticker to tick price [Default: RELIANCE]: ");
        if (!scanner.hasNextLine()) return;
        String line = scanner.nextLine().trim().toUpperCase();
        String ticker = line.isEmpty() ? "RELIANCE" : line;

        Stock stock = marketQuotes.get(ticker);
        if (stock == null) {
            System.out.println("Error: Ticker '" + ticker + "' not found.");
            return;
        }

        double newPrice = promptDouble(scanner,
            String.format("Current Price of %s is Rs. %,.2f. Enter new price (or Enter for random +/- 1%% tick): ", ticker, stock.getCurrentPrice()),
            -1.0);

        if (newPrice <= 0.0) {
            double pct = (Math.random() * 0.02) - 0.01;
            newPrice = Math.round((stock.getCurrentPrice() * (1.0 + pct)) * 20.0) / 20.0;
        }

        double oldPrice = stock.getCurrentPrice();
        stock.updatePrice(newPrice);
        System.out.printf("Updated %s: Rs. %,.2f -> Rs. %,.2f (%+.2f%% from base Rs. %,.2f)%n",
            ticker, oldPrice, newPrice, stock.getPercentChange(), stock.getBasePrice());
    }
}
