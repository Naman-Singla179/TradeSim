package tradesim.services;

import tradesim.exceptions.StockNotFoundException;
import tradesim.models.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderBookManager {
    private final Map<String, OrderBook> books = new HashMap<>();

    public OrderBook registerStock(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty.");
        }
        String symbol = ticker.toUpperCase();
        OrderBook book = books.get(symbol);
        if (book == null) {
            book = new OrderBook(symbol);
            books.put(symbol, book);
        }
        return book;
    }

    public OrderBook getOrderBook(String ticker) {
        if (ticker == null) throw new StockNotFoundException("Ticker cannot be null.");
        OrderBook book = books.get(ticker.toUpperCase());
        if (book == null) throw new StockNotFoundException("No order book found for: " + ticker);
        return book;
    }

    public boolean hasStock(String ticker) {
        return ticker != null && books.containsKey(ticker.toUpperCase());
    }

    public void addOrder(Order order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null.");
        getOrderBook(order.getTicker()).addOrder(order);
    }

    public boolean cancelOrder(String ticker, String orderId) {
        return getOrderBook(ticker).cancelOrder(orderId);
    }

    public Order findOrder(String orderId) {
        if (orderId == null) return null;
        for (OrderBook book : books.values()) {
            Order order = book.findOrder(orderId);
            if (order != null) return order;
        }
        return null;
    }

    public List<Order> getActiveOrdersForUser(String userId) {
        List<Order> userOrders = new ArrayList<>();
        if (userId != null) {
            for (OrderBook book : books.values()) {
                for (BuyOrder b : book.getAllBids()) if (b.getUserId().equals(userId)) userOrders.add(b);
                for (SellOrder s : book.getAllAsks()) if (s.getUserId().equals(userId)) userOrders.add(s);
            }
        }
        return userOrders;
    }

    public Set<String> getRegisteredTickers() {
        return Collections.unmodifiableSet(books.keySet());
    }

    public Map<String, OrderBook> getAllOrderBooks() {
        return Collections.unmodifiableMap(books);
    }

    @Override
    public String toString() {
        return "OrderBookManager[Registered Stocks: " + books.keySet() + "]";
    }
}
