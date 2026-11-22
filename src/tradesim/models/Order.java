package tradesim.models;

import tradesim.exceptions.InvalidOrderException;

public abstract class Order implements Comparable<Order> {
    private final String orderId;
    private final String userId;
    private final String ticker;
    private final OrderType orderType;
    private final int quantity;
    private int remainingQuantity;
    private final double limitPrice;
    private OrderStatus status;
    private final long timestamp;

    public Order(String orderId, String userId, String ticker, OrderType orderType, int quantity, double limitPrice) {
        if (orderId == null || orderId.trim().isEmpty() || userId == null || userId.trim().isEmpty() ||
            ticker == null || ticker.trim().isEmpty() || quantity <= 0 || limitPrice <= 0.0 || orderType == null) {
            throw new InvalidOrderException("Invalid order: ID, User, Ticker, Qty > 0, Price > 0 required.");
        }
        this.orderId = orderId;
        this.userId = userId;
        this.ticker = ticker.toUpperCase();
        this.orderType = orderType;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.limitPrice = limitPrice;
        this.status = OrderStatus.PENDING;
        this.timestamp = System.nanoTime();
    }

    public void reduceQuantity(int filledQuantity) {
        if (filledQuantity <= 0 || filledQuantity > this.remainingQuantity) {
            throw new IllegalArgumentException("Invalid filled quantity: " + filledQuantity);
        }
        this.remainingQuantity -= filledQuantity;
        this.status = (this.remainingQuantity == 0) ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    public void cancel() {
        if (this.status == OrderStatus.FILLED) {
            throw new IllegalStateException("Cannot cancel an order that is already FILLED.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isClosed() {
        return this.status == OrderStatus.FILLED || 
               this.status == OrderStatus.CANCELLED || 
               this.status == OrderStatus.REJECTED;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getTicker() { return ticker; }
    public OrderType getOrderType() { return orderType; }
    public int getQuantity() { return quantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public int getFilledQuantity() { return quantity - remainingQuantity; }
    public double getLimitPrice() { return limitPrice; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public long getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return orderId.equals(order.orderId);
    }

    @Override
    public int hashCode() {
        return orderId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %d/%d %s @ Rs. %.2f (%s)", 
            orderId, orderType, remainingQuantity, quantity, ticker, limitPrice, status);
    }
}
