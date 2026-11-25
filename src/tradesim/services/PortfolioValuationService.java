package tradesim.services;

import tradesim.models.Portfolio;
import tradesim.models.PortfolioHolding;
import tradesim.models.Stock;
import tradesim.models.User;

import java.util.Map;

public class PortfolioValuationService {

    public double calculateStockHoldingValue(Portfolio portfolio, Map<String, Stock> marketQuotes) {
        if (portfolio == null || marketQuotes == null) return 0.0;
        double totalValue = 0.0;
        for (PortfolioHolding holding : portfolio.getHoldings().values()) {
            if (holding.getQuantity() > 0) {
                Stock stock = marketQuotes.get(holding.getTicker());
                double livePrice = (stock != null) ? stock.getCurrentPrice() : holding.getAverageBuyPrice();
                totalValue += (holding.getQuantity() * livePrice);
            }
        }
        return totalValue;
    }

    public double calculateTotalNetWorth(User user, Portfolio portfolio, Map<String, Stock> marketQuotes) {
        if (user == null) return 0.0;
        return user.getCashBalance() + calculateStockHoldingValue(portfolio, marketQuotes);
    }

    public double calculateUnrealizedPnL(Portfolio portfolio, Map<String, Stock> marketQuotes) {
        if (portfolio == null || marketQuotes == null) return 0.0;
        double totalPnL = 0.0;
        for (PortfolioHolding holding : portfolio.getHoldings().values()) {
            if (holding.getQuantity() > 0) {
                Stock stock = marketQuotes.get(holding.getTicker());
                double livePrice = (stock != null) ? stock.getCurrentPrice() : holding.getAverageBuyPrice();
                totalPnL += ((livePrice - holding.getAverageBuyPrice()) * holding.getQuantity());
            }
        }
        return totalPnL;
    }

    public double calculateTotalInvested(Portfolio portfolio) {
        if (portfolio == null) return 0.0;
        double totalInvested = 0.0;
        for (PortfolioHolding holding : portfolio.getHoldings().values()) {
            if (holding.getQuantity() > 0) {
                totalInvested += (holding.getQuantity() * holding.getAverageBuyPrice());
            }
        }
        return totalInvested;
    }

    public String generateValuationReport(User user, Portfolio portfolio, Map<String, Stock> marketQuotes) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== PORTFOLIO REPORT: %s ===%n", user.getUsername()));
        sb.append(String.format("Available Cash Balance: Rs. %,.2f%n", user.getCashBalance()));

        double totalInvested = calculateTotalInvested(portfolio);
        double currentValuation = calculateStockHoldingValue(portfolio, marketQuotes);
        double netWorth = calculateTotalNetWorth(user, portfolio, marketQuotes);
        double unrealizedPnL = calculateUnrealizedPnL(portfolio, marketQuotes);
        double pnlPercent = totalInvested > 0 ? (unrealizedPnL / totalInvested) * 100.0 : 0.0;

        sb.append(String.format("Invested Capital:      Rs. %,.2f%n", totalInvested));
        sb.append(String.format("Current Stock Value:   Rs. %,.2f%n", currentValuation));
        sb.append(String.format("Total Net Worth:       Rs. %,.2f%n", netWorth));
        sb.append(String.format("Unrealized P&L:        Rs. %,+.2f (%+.2f%%)%n", unrealizedPnL, pnlPercent));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("%-10s %-8s %-12s %-12s %-14s %-12s%n", 
            "Ticker", "Qty", "Avg Buy", "Live Price", "Current Val", "P&L"));
        sb.append("----------------------------------------------------------------------\n");

        for (PortfolioHolding h : portfolio.getHoldings().values()) {
            if (h.getQuantity() > 0) {
                Stock s = marketQuotes.get(h.getTicker());
                double live = (s != null) ? s.getCurrentPrice() : h.getAverageBuyPrice();
                double val = h.getQuantity() * live;
                double pnl = (live - h.getAverageBuyPrice()) * h.getQuantity();
                sb.append(String.format("%-10s %-8d Rs. %-8.2f Rs. %-8.2f Rs. %-10.2f Rs. %+-8.2f%n",
                    h.getTicker(), h.getQuantity(), h.getAverageBuyPrice(), live, val, pnl));
            }
        }
        sb.append("======================================================================\n");
        return sb.toString();
    }
}
