package tradesim.models;

public class BuyOrder extends Order {

    public BuyOrder(String orderId, String userId, String ticker, int quantity, double limitPrice) {
        super(orderId, userId, ticker, OrderType.BUY, quantity, limitPrice);
    }

    @Override
    public int compareTo(Order other) {
        if (other == null) return -1;
        // Highest limit price gets priority
        int priceComparison = Double.compare(other.getLimitPrice(), this.getLimitPrice());
        if (priceComparison != 0) {
            return priceComparison;
        }
        // Earliest timestamp gets priority
        int timeComparison = Long.compare(this.getTimestamp(), other.getTimestamp());
        if (timeComparison != 0) {
            return timeComparison;
        }
        // Consistent ordering by unique Order ID
        return this.getOrderId().compareTo(other.getOrderId());
    }
}
