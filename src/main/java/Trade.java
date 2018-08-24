import org.knowm.xchange.dto.Order;

public class Trade {

    String pair;
    Order.OrderType side;
    double amt;
    double price;

    public Trade(String pair, String side, double amt, double price) {
        this.pair = pair;
        this.side = side.contains("Buy")? Order.OrderType.BID: Order.OrderType.ASK;
        this.amt = amt;
        this.price = price;
    }

    public String getPair() {
        return pair;
    }

    public Order.OrderType getSide() {
        return side;
    }

    public double getAmt() {
        return amt;
    }

    public double getPrice() {
        return price;
    }
}
