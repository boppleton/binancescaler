import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinancePriceQuantity;
import org.knowm.xchange.binance.service.BinanceAccountServiceRaw;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.binance.service.BinanceTradeServiceRaw;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    private static GUI gui = null;





    public static void main(String[] args) throws IOException {

//        BinanceAPI.connect();

        startGUI();




    }

    private static void startGUI() {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new SubstanceGraphiteAquaLookAndFeel());
            } catch (Exception e) { System.out.println("Substance look&feel load error!"); e.printStackTrace(); }

            try {
                gui = new GUI("Binance Scaler");
            } catch (Exception e) { System.out.println("window object create error"); e.printStackTrace(); }

            if (gui != null) {
                gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gui.setSize(400, 600);
                gui.setLocationRelativeTo(null);
                gui.setVisible(true);
            }
        });

    }





}
