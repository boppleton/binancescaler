import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinancePriceQuantity;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.OrderType;
import org.knowm.xchange.binance.dto.trade.TimeInForce;
import org.knowm.xchange.binance.service.BinanceAccountServiceRaw;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.binance.service.BinanceTradeServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BinanceAPI {

    private static Exchange exchange;

    private static MarketDataService marketService;
    private static BinanceMarketDataServiceRaw marketServiceRaw;

    private static TradeService tradeService;
    private static BinanceTradeServiceRaw tradeServiceRaw;

    private static AccountService accountService;
    private static BinanceAccountServiceRaw accountServiceRaw;

    public static void connect(String k, String s) throws IOException {

        ExchangeSpecification exSpec = new ExchangeSpecification(BinanceExchange.class);
        exSpec.setApiKey(k);
        exSpec.setSecretKey(s);
        exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);

        marketService = exchange.getMarketDataService();
        marketServiceRaw = (BinanceMarketDataServiceRaw) marketService;

        tradeService = exchange.getTradeService();
        tradeServiceRaw = (BinanceTradeServiceRaw) tradeService;

        accountService = exchange.getAccountService();
        accountServiceRaw = (BinanceAccountServiceRaw) accountService;

    }

    public static String testConnection() {

        AccountInfo acctInfo = null;

        String acctStatus = "connectionSuccess";


        try {
            acctInfo = accountService.getAccountInfo();


        } catch (Exception e) {
            acctStatus = e.getMessage();
        }


        return acctStatus;


    }

    public static String placeOrder(Trade t, int roundscale) throws IOException {

        System.out.println("placing limit " + t.pair + " " + t.side.toString() + " " + t.amt + " at " + new BigDecimal(t.price).setScale(roundscale, RoundingMode.HALF_EVEN));

        LimitOrder order = new LimitOrder( t.side, new BigDecimal(t.amt).setScale(2, RoundingMode.HALF_DOWN), getPair(t.pair), "", null, new BigDecimal(t.price).setScale(roundscale, RoundingMode.HALF_EVEN) );

        String id = tradeService.placeLimitOrder(order);

        System.out.println("order id: " + id);

        return id;
    }

    private static CurrencyPair getPair(String pair) {

        if (pair.contains("BTC")) {
            String c1 = pair.substring(0, pair.indexOf("BTC"));
            return new CurrencyPair(new Currency(c1), Currency.BTC);
        } else if (pair.contains("ETH")) {
            String c1 = pair.substring(0, pair.indexOf("ETH"));
            return new CurrencyPair(new Currency(c1), Currency.ETH);
        } else if (pair.contains("BNB")) {
            String c1 = pair.substring(0, pair.indexOf("BNB"));
            return new CurrencyPair(new Currency(c1), Currency.BNB);
        }else {
            String c1 = pair.substring(0, pair.indexOf("USDT"));
            return new CurrencyPair(new Currency(c1), Currency.USDT);
        }




    }

    public static ArrayList<String> getPairs() throws IOException {

        List<BinancePriceQuantity> tickers = marketServiceRaw.tickerAllBookTickers();

        ArrayList<String> pairs = new ArrayList<>();

        for (BinancePriceQuantity p : tickers) {
            pairs.add(p.symbol);
        }

        Collections.sort(pairs);


        return pairs;

    }

    public static int getRoundscale(Trade trade) throws IOException {

        CurrencyPair p = getPair(trade.pair);

        OrderBook o = marketService.getOrderBook(p);
        List<LimitOrder> orders = o.getBids();

        int depth;

        if (orders.size() < 50) {
            depth = orders.size();
        } else {
            depth = 50;
        }

        int maxScale = 0;

        String etest = "" + orders.get(0).getLimitPrice().doubleValue();
        System.out.println(etest);
        if (etest.contains("E")) {
            return 8;
        } else {

            for (int i = 0; i < depth; i++) {

                System.out.println(orders.get(i).getLimitPrice().doubleValue());

                Double price = orders.get(i).getLimitPrice().doubleValue();


                String[] splitter = price.toString().split("\\.");

                if (splitter[1].length() > maxScale) {
                    maxScale = splitter[1].length();
                    System.out.println("maxscale up to " + splitter[1].length());
                }

            }
        }

        return maxScale;

    }
}
