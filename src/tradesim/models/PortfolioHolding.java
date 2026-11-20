package tradesim.models;

import tradesim.exceptions.InsufficientSharesException;

public class PortfolioHolding {
    private final String userId;
    private final String ticker;
    private int quantity;
    private double averageBuyPrice;
    private double realizedPnL;

    public PortfolioHolding(String userId, String ticker, int quantity, double buyPrice) {
        if (quantity < 0 || buyPrice < 0.0) {
            throw new IllegalArgumentException("Quantity and buy price cannot be negative.");
        }
        this.userId = userId;
        this.ticker = ticker.toUpperCase();
        this.quantity = quantity;
        this.averageBuyPrice = buyPrice;
        this.realizedPnL = 0.0;
    }

    public void addShares(int additionalQty, double buyPrice) {
        if (additionalQty <= 0 || buyPrice <= 0.0) {
            throw new IllegalArgumentException("Quantity and price must be positive.");
        }
        double totalCost = (this.quantity * this.averageBuyPrice) + (additionalQty * buyPrice);
        this.quantity += additionalQty;
        this.averageBuyPrice = totalCost / this.quantity;
    }

    public void removeShares(int qtySold, double sellPrice) {
        if (qtySold <= 0) {
            throw new IllegalArgumentException("Quantity sold must be positive.");
        }
        if (qtySold > this.quantity) {
            throw new InsufficientSharesException(
                String.format("Insufficient shares of %s. Requested: %d, Available: %d", ticker, qtySold, this.quantity)
            );
        }
        this.realizedPnL += (sellPrice - this.averageBuyPrice) * qtySold;
        this.quantity -= qtySold;
    }

    public double getUnrealizedPnL(double currentMarketPrice) {
        return (currentMarketPrice - averageBuyPrice) * quantity;
    }

    public double getCurrentValue(double currentMarketPrice) {
        return quantity * currentMarketPrice;
    }

    public String getUserId() { return userId; }
    public String getTicker() { return ticker; }
    public int getQuantity() { return quantity; }
    public double getAverageBuyPrice() { return averageBuyPrice; }
    public double getRealizedPnL() { return realizedPnL; }

    @Override
    public String toString() {
        return String.format("%s | Qty: %d | Avg Buy: Rs. %.2f | Realized P&L: Rs. %.2f",
            ticker, quantity, averageBuyPrice, realizedPnL);
    }
}