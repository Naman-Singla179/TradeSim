package tradesim.services;

import tradesim.models.*;

import java.util.ArrayList;
import java.util.List;

public class OrderMatchingEngine {
    private final OrderBookManager orderBookManager;
    private final TradeSettlementService settlementService;

    public OrderMatchingEngine(OrderBookManager orderBookManager, TradeSettlementService settlementService) {
        if (orderBookManager == null || settlementService == null) {
            throw new IllegalArgumentException("Dependencies cannot be null.");
        }
        this.orderBookManager = orderBookManager;
        this.settlementService = settlementService;
    }

    public List<TransactionReceipt> submitOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Cannot submit null order.");
        }
        orderBookManager.addOrder(order);
        return matchOrders(order.getTicker());
    }

    public List<TransactionReceipt> matchOrders(String ticker) {
        OrderBook book = orderBookManager.getOrderBook(ticker);
        List<TransactionReceipt> receipts = new ArrayList<>();

        while (book.peekBestBid() != null && book.peekBestAsk() != null) {
            BuyOrder bestBid = book.peekBestBid();
            SellOrder bestAsk = book.peekBestAsk();

            if (bestBid.isClosed() || bestBid.getRemainingQuantity() <= 0) {
                book.pollBestBid();
                continue;
            }
            if (bestAsk.isClosed() || bestAsk.getRemainingQuantity() <= 0) {
                book.pollBestAsk();
                continue;
            }

            if (bestBid.getLimitPrice() >= bestAsk.getLimitPrice()) {

                if (bestBid.getUserId().equals(bestAsk.getUserId())) {
                    break;
                }

                double executionPrice = (bestBid.getTimestamp() < bestAsk.getTimestamp())
                    ? bestBid.getLimitPrice()
                    : bestAsk.getLimitPrice();

                int matchQty = Math.min(bestBid.getRemainingQuantity(), bestAsk.getRemainingQuantity());

                try {
                    TransactionReceipt receipt = settlementService.settleTrade(
                        bestBid, bestAsk, matchQty, executionPrice, 0.0
                    );
                    receipts.add(receipt);

                    if (bestBid.getRemainingQuantity() == 0) {
                        book.pollBestBid(); 
                    }
                    if (bestAsk.getRemainingQuantity() == 0) {
                        book.pollBestAsk();
                    }
                } catch (tradesim.exceptions.InsufficientFundsException e) {
                    bestBid.setStatus(OrderStatus.REJECTED);
                    book.pollBestBid();
                } catch (tradesim.exceptions.InsufficientSharesException e) {
                    bestAsk.setStatus(OrderStatus.REJECTED);
                    book.pollBestAsk();
                }
            } else {
                break;
            }
        }

        return receipts;
    }

    public OrderBookManager getOrderBookManager() {
        return orderBookManager;
    }

    public TradeSettlementService getSettlementService() {
        return settlementService;
    }
}