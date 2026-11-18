package tradesim.exceptions;

public class TradeSimException extends RuntimeException {
    public TradeSimException(String message) {
        super(message);
    }

    public TradeSimException(String message, Throwable cause) {
        super(message, cause);
    }
}
