package tradesim.services;

import tradesim.exceptions.InsufficientFundsException;
import tradesim.exceptions.InsufficientSharesException;
import tradesim.exceptions.UserNotFoundException;
import tradesim.models.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeSettlementService {
    private static int receiptCounter = 1000;

    private final Map<String, User> userRegistry = new HashMap<>();
    private final Map<String, Portfolio> portfolioRegistry = new HashMap<>();
    private final List<TransactionReceipt> transactionLedger = new ArrayList<>();

    public void registerUser(User user, Portfolio portfolio) {
        if (user == null || portfolio == null) {
            throw new IllegalArgumentException("User and Portfolio cannot be null.");
        }
        userRegistry.put(user.getUserId(), user);
        portfolioRegistry.put(user.getUserId(), portfolio);
    }

    public User getUser(String userId) {
        User user = userRegistry.get(userId);
        if (user == null)
            throw new UserNotFoundException("User not found: " + userId);
        return user;
    }

    public Portfolio getPortfolio(String userId) {
        Portfolio portfolio = portfolioRegistry.get(userId);
        if (portfolio == null)
            throw new UserNotFoundException("Portfolio not found: " + userId);
        return portfolio;
    }

    public Map<String, User> getAllUsers() {
        return Collections.unmodifiableMap(userRegistry);
    }

    public Map<String, Portfolio> getAllPortfolios() {
        return Collections.unmodifiableMap(portfolioRegistry);
    }

    public TransactionReceipt settleTrade(BuyOrder buyOrder, SellOrder sellOrder,
            int matchQty, double execPrice, double brokerageFee) {
        User buyer = getUser(buyOrder.getUserId());
        User seller = getUser(sellOrder.getUserId());
        Portfolio buyerPortfolio = getPortfolio(buyOrder.getUserId());
        Portfolio sellerPortfolio = getPortfolio(sellOrder.getUserId());

        double totalCost = (matchQty * execPrice) + brokerageFee;
        double netProceeds = (matchQty * execPrice) - brokerageFee;

        if (buyer.getCashBalance() < totalCost) {
            throw new InsufficientFundsException(
                    String.format("Buyer [%s] has insufficient funds. Required: Rs. %,.2f, Available: Rs. %,.2f",
                            buyer.getUsername(), totalCost, buyer.getCashBalance()));
        }

        PortfolioHolding sellerHolding = sellerPortfolio.getHolding(sellOrder.getTicker());
        int sellerShares = (sellerHolding != null) ? sellerHolding.getQuantity() : 0;
        if (sellerShares < matchQty) {
            throw new InsufficientSharesException(
                    String.format("Seller [%s] has insufficient shares of %s. Required: %d, Available: %d",
                            seller.getUsername(), sellOrder.getTicker(), matchQty, sellerShares));
        }

        buyer.debit(totalCost);
        seller.credit(netProceeds);
        sellerPortfolio.recordSell(sellOrder.getTicker(), matchQty, execPrice);
        buyerPortfolio.recordBuy(buyOrder.getTicker(), matchQty, execPrice);

        buyOrder.reduceQuantity(matchQty);
        sellOrder.reduceQuantity(matchQty);

        String receiptId = "RCP-" + (++receiptCounter);
        TransactionReceipt receipt = new TransactionReceipt(
                receiptId, buyOrder.getOrderId(), sellOrder.getOrderId(),
                buyOrder.getTicker(), matchQty, execPrice, brokerageFee);
        transactionLedger.add(receipt);
        return receipt;
    }

    public List<TransactionReceipt> getTransactionLedger() {
        return Collections.unmodifiableList(transactionLedger);
    }

    public List<TransactionReceipt> getTransactionsByTicker(String ticker) {
        List<TransactionReceipt> filtered = new ArrayList<>();
        if (ticker != null) {
            for (TransactionReceipt r : transactionLedger) {
                if (r.getTicker().equalsIgnoreCase(ticker))
                    filtered.add(r);
            }
        }
        return filtered;
    }
}