package tradesim.models;

public class Stock {
    private final String ticker;
    private final String companyName;
    private final String exchange;
    private final double basePrice;
    private double currentPrice;
    private double dayHigh;
    private double dayLow;
    private long volume;

    public Stock(String ticker, String companyName, String exchange, double basePrice) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty.");
        }
        if (basePrice <= 0.0) {
            throw new IllegalArgumentException("Base price must be greater than 0.0.");
        }
        this.ticker = ticker.toUpperCase();
        this.companyName = companyName;
        this.exchange = exchange.toUpperCase();
        this.basePrice = basePrice;
        this.currentPrice = basePrice;
        this.dayHigh = basePrice;
        this.dayLow = basePrice;
        this.volume = 0L;
    }

    public void updatePrice(double newPrice) {
        if (newPrice <= 0.0) return;
        this.currentPrice = newPrice;
        if (newPrice > dayHigh) dayHigh = newPrice;
        if (newPrice < dayLow) dayLow = newPrice;
    }

    public void addVolume(long tradedQty) {
        if (tradedQty > 0) {
            this.volume += tradedQty;
        }
    }

    public double getPercentChange() {
        return ((currentPrice - basePrice) / basePrice) * 100.0;
    }

    public String getTicker() { return ticker; }
    public String getCompanyName() { return companyName; }
    public String getExchange() { return exchange; }
    public double getBasePrice() { return basePrice; }
    public double getCurrentPrice() { return currentPrice; }
    public double getDayHigh() { return dayHigh; }
    public double getDayLow() { return dayLow; }
    public long getVolume() { return volume; }

    @Override
    public String toString() {
        return String.format("%s (%s - %s) | Live: Rs. %.2f | Chg: %+.2f%% | H: Rs. %.2f L: Rs. %.2f | Vol: %d",
            ticker, companyName, exchange, currentPrice, getPercentChange(), dayHigh, dayLow, volume);
    }
}
