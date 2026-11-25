package tradesim.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class OrderBook {
    private final String ticker;
    private final PriorityQueue<BuyOrder> buyOrders;
    private final PriorityQueue<SellOrder> sellOrders;

    public OrderBook(String ticker) {
        this.ticker = ticker.toUpperCase();
        this.buyOrders = new PriorityQueue<>();
        this.sellOrders = new PriorityQueue<>();
    }

    public void addOrder(Order order) {
        if (order instanceof BuyOrder) {
            buyOrders.add((BuyOrder) order);
        } else if (order instanceof SellOrder) {
            sellOrders.add((SellOrder) order);
        } else {
            throw new IllegalArgumentException("Unknown order type: " + (order == null ? "null" : order.getClass().getName()));
        }
    }

    public BuyOrder peekBestBid() {
        return buyOrders.peek();
    }

    public SellOrder peekBestAsk() {
        return sellOrders.peek();
    }

    public BuyOrder pollBestBid() {
        return buyOrders.poll();
    }

    public SellOrder pollBestAsk() {
        return sellOrders.poll();
    }

    public Order findOrder(String orderId) {
        if (orderId == null) return null;
        for (BuyOrder b : buyOrders) if (b.getOrderId().equals(orderId)) return b;
        for (SellOrder s : sellOrders) if (s.getOrderId().equals(orderId)) return s;
        return null;
    }

    public boolean cancelOrder(String orderId) {
        Order order = findOrder(orderId);
        if (order != null) {
            order.cancel();
            return buyOrders.remove(order) || sellOrders.remove(order);
        }
        return false;
    }

    public List<BuyOrder> getAllBids() {
        List<BuyOrder> copy = new ArrayList<>(buyOrders);
        Collections.sort(copy);
        return copy;
    }

    public List<SellOrder> getAllAsks() {
        List<SellOrder> copy = new ArrayList<>(sellOrders);
        Collections.sort(copy);
        return copy;
    }

    public List<BuyOrder> getTopBids(int depth) {
        List<BuyOrder> copy = getAllBids();
        return new ArrayList<>(copy.subList(0, Math.min(depth, copy.size())));
    }

    public List<SellOrder> getTopAsks(int depth) {
        List<SellOrder> copy = getAllAsks();
        return new ArrayList<>(copy.subList(0, Math.min(depth, copy.size())));
    }

    public int getBuyOrderCount() {
        return buyOrders.size();
    }

    public int getSellOrderCount() {
        return sellOrders.size();
    }

    public String getTicker() { return ticker; }

    @Override
    public String toString() {
        return String.format("OrderBook[%s | Bids: %d | Asks: %d]", ticker, buyOrders.size(), sellOrders.size());
    }
}
