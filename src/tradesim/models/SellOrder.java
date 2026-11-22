package tradesim.models;

public class SellOrder extends Order {

    public SellOrder(String orderId, String userId, String ticker, int quantity, double limitPrice) {
        super(orderId, userId, ticker, OrderType.SELL, quantity, limitPrice);
    }

    @Override
    public int compareTo(Order other) {
        if (other == null)
            return -1;
        // Lowest limit price gets priority
        int priceComparison = Double.compare(this.getLimitPrice(), other.getLimitPrice());
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