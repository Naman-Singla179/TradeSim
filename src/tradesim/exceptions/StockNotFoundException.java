package tradesim.exceptions;

public class StockNotFoundException extends TradeSimException {
    public StockNotFoundException(String message) {
        super(message);
    }
}