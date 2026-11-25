package tradesim.models;

import tradesim.exceptions.InsufficientSharesException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Portfolio {
    private final String userId;
    private final Map<String, PortfolioHolding> holdings = new HashMap<>();

    public Portfolio(String userId) {
        this.userId = userId;
    }

    public void recordBuy(String ticker, int quantity, double price) {
        String symbol = ticker.toUpperCase();
        PortfolioHolding holding = holdings.get(symbol);
        if (holding == null) {
            holdings.put(symbol, new PortfolioHolding(userId, symbol, quantity, price));
        } else {
            holding.addShares(quantity, price);
        }
    }

    public void recordSell(String ticker, int quantity, double price) {
        String symbol = ticker.toUpperCase();
        PortfolioHolding holding = holdings.get(symbol);
        int available = (holding == null) ? 0 : holding.getQuantity();
        if (available < quantity) {
            throw new InsufficientSharesException(
                String.format("Cannot sell %d shares of %s. You hold: %d", quantity, symbol, available)
            );
        }
        holding.removeShares(quantity, price);
    }

    public PortfolioHolding getHolding(String ticker) {
        return holdings.get(ticker.toUpperCase());
    }

    public Map<String, PortfolioHolding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public String getUserId() { return userId; }

    @Override
    public String toString() {
        return String.format("Portfolio[%s | Active Positions: %d]", userId, holdings.size());
    }
}
