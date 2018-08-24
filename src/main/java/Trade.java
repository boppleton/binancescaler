import org.knowm.xchange.dto.Order;

public class Trade {

    String pair;
    Order.OrderType side;
    double amt;
    double price;

    Trade(String pair, String side, double amt, double price) {
        this.pair = pair;
        this.side = side.contains("Buy")? Order.OrderType.BID: Order.OrderType.ASK;
        this.amt = amt;
        this.price = price;
    }
}
