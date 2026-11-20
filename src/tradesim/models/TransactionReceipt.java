package tradesim.models;

import java.util.Date;

public class TransactionReceipt {
    private final String receiptId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String ticker;
    private final int executedQuantity;
    private final double executedPrice;
    private final double brokerageFee;
    private final double totalAmount;
    private final long executionTime;

    public TransactionReceipt(String receiptId, String buyOrderId, String sellOrderId, 
                              String ticker, int executedQuantity, double executedPrice, 
                              double brokerageFee) {
        this.receiptId = receiptId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.ticker = ticker.toUpperCase();
        this.executedQuantity = executedQuantity;
        this.executedPrice = executedPrice;
        this.brokerageFee = brokerageFee;
        this.totalAmount = (executedQuantity * executedPrice) + brokerageFee;
        this.executionTime = System.currentTimeMillis();
    }

    public String getReceiptId() { return receiptId; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public String getTicker() { return ticker; }
    public int getExecutedQuantity() { return executedQuantity; }
    public double getExecutedPrice() { return executedPrice; }
    public double getBrokerageFee() { return brokerageFee; }
    public double getTotalAmount() { return totalAmount; }
    public long getExecutionTime() { return executionTime; }
    public Date getExecutionDate() { return new Date(executionTime); }

    @Override
    public String toString() {
        return String.format("Receipt[%s | %s: %d @ Rs. %.2f | Fee: Rs. %.2f | Total: Rs. %.2f | Time: %s]",
            receiptId, ticker, executedQuantity, executedPrice, brokerageFee, totalAmount, new Date(executionTime));
    }
}
