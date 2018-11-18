import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinancePriceQuantity;
import org.knowm.xchange.binance.service.BinanceAccountServiceRaw;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.binance.service.BinanceTradeServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.RateLimit;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BinanceAPI {

    private static Exchange exchange;

    private static MarketDataService marketService;
    private static BinanceMarketDataServiceRaw marketServiceRaw;

    private static TradeService tradeService;
    private static BinanceTradeServiceRaw tradeServiceRaw;

    private static AccountService accountService;
    private static BinanceAccountServiceRaw accountServiceRaw;

    public static void connect(String k, String s) {

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

    public static String placeOrder(SingleTrade t, int roundscale, int amtscale) throws IOException {

        System.out.println("placing limit " + t.pair + " " + t.side.toString() + " " + t.amt + " at " + new BigDecimal(t.price).setScale(roundscale, RoundingMode.HALF_EVEN));

        LimitOrder order = new LimitOrder( t.side, new BigDecimal(t.amt).setScale(amtscale, RoundingMode.HALF_DOWN), getPair(t.pair), "", null, new BigDecimal(t.price).setScale(roundscale, RoundingMode.HALF_EVEN) );

        String id = tradeService.placeLimitOrder(order);

        System.out.println("order id: " + id);

        return id;
    }

    private static CurrencyPair getPair(String pair) {

        if (pair.contains("BTC/USDT")) {
            return CurrencyPair.BTC_USDT;
        } else if (pair.contains("BCHSV/BTC")) {
            return new CurrencyPair(new Currency("BCHSV"), Currency.BTC);
        } else if (pair.contains("BCHSV/USDT")) {
            return new CurrencyPair(new Currency("BCHSV"), Currency.USDT);
        } else if (pair.contains("BCHABC/BTC")) {
            return new CurrencyPair(new Currency("BCHABC"), Currency.BTC);
        } else if (pair.contains("BCHABC/USDT")) {
            return new CurrencyPair(new Currency("BCHABC"), Currency.USDT);
        } else if (pair.contains("IOTX/BTC")) {
            return new CurrencyPair(new Currency("IOTX"), Currency.BTC);
        } else if (pair.contains("IOTX/ETH")) {
            return new CurrencyPair(new Currency("IOTX"), Currency.ETH);
        }
        else {

            if (pair.contains("/BTC")) {
                String c1 = pair.substring(0, pair.indexOf("/BTC"));
                return new CurrencyPair(new Currency(c1), Currency.BTC);
            } else if (pair.contains("/ETH")) {
                String c1 = pair.substring(0, pair.indexOf("/ETH"));
                return new CurrencyPair(new Currency(c1), Currency.ETH);
            } else if (pair.contains("/BNB")) {
                String c1 = pair.substring(0, pair.indexOf("/BNB"));
                return new CurrencyPair(new Currency(c1), Currency.BNB);
            } else {
                String c1 = pair.substring(0, pair.indexOf("/USDT"));
                return new CurrencyPair(new Currency(c1), Currency.USDT);
            }

        }

    }

    public static HashMap<CurrencyPair, CurrencyPairMetaData> getPairs() throws IOException {

//        List<BinancePriceQuantity> tickers = marketServiceRaw.tickerAllBookTickers();

        ExchangeMetaData meta = exchange.getExchangeMetaData();

        HashMap metamap = (HashMap) meta.getCurrencyPairs();

//
//        ArrayList<String> pairs = new ArrayList<>();
//
//        for (BinancePriceQuantity p : tickers) {
//                pairs.add(p.symbol);
//        }
//
//        Collections.sort(pairs);

        return metamap;

    }


    public static int getRoundscale(String pair) throws IOException {

        System.out.println("geting roundscale, pair: " + pair);

        CurrencyPair p = getPair(pair);

        System.out.println(GUI.pairs.get(p));

        System.out.println(GUI.pairs.get(p).getPriceScale());

        return GUI.pairs.get(p).getPriceScale();

    }

    public static int getAmountscale(String pair) throws IOException {

        CurrencyPair p = getPair(pair);

        System.out.println("getscle: "+ GUI.pairs.get(p).getMinimumAmount().scale());


        return GUI.pairs.get(p).getMinimumAmount().scale();

//        CurrencyPair p = getPair(trade.pair);
//
//        Trades trades = marketService.getTrades(p);
//
//        List<Trade> tradess = trades.getTrades();
//
//        int maxDecimals = 0;
//
//
//        for (Trade t : tradess) {
//            System.out.println(t.toString());
//
//            String ss = "";
//
//            ss = t.getOriginalAmount().toString().replaceFirst("\\.0*$|(\\.\\d*?)0+$", "$1");
//
//            System.out.println(ss);
//
//            String[] s = ss.split("\\.");
//
//            int m = s[s.length - 1].length();
//
//            if ( (m > maxDecimals) && (Double.parseDouble(ss) % 1 != 0) ) {
//
//                System.out.println("maxdec up, " + m);
//                maxDecimals = m;
//            }
//
//        }
//
//        System.out.println("max decimals - " + maxDecimals);
//
//        return maxDecimals;
    }

    public static ArrayList<Double> getBidask(SingleTrade singleTrade) throws IOException {

        Ticker t = marketService.getTicker(getPair(singleTrade.pair));

        ArrayList<Double> bidask = new ArrayList<>();

        bidask.add(0, t.getBid().doubleValue());
        bidask.add(1, t.getAsk().doubleValue());

        return bidask;


    }

    public static ArrayList<Double> getBidaskFromString(String pair) throws IOException {

        Ticker t = marketService.getTicker(getPair(pair));

        ArrayList<Double> bidask = new ArrayList<>();

        bidask.add(0, t.getBid().doubleValue());
        bidask.add(1, t.getAsk().doubleValue());

        return bidask;


    }

    public static void getRateLimits() throws InterruptedException, IOException {


        System.out.println(exchange.getExchangeMetaData().toString());

    }
}
