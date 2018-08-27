import org.apache.commons.io.FileUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GUI extends JFrame {

    private static ArrayList<String> acctLinesForDelete = new ArrayList<>();

    private static GridBagConstraints gbc = new GridBagConstraints();

    private static JPanel chooseAccountPanel = new JPanel();
    private static JLabel noAccountsLabel = new JLabel("No accounts set up");
    private static JButton addAccountButton;

    private static JPanel scaleMainPanel;

    private JPanel pairPickerPanel;

    private ArrayList<SingleTrade> trades = new ArrayList<>();

    public GUI(String title) throws IOException {
        super(title);

        setLayout(new GridBagLayout());

        chooseAccountFrame();

        addAccountButton();

        basePairChecksPanel();

    }


    private JComboBox<String> pairPicker;
    public static HashMap<CurrencyPair, CurrencyPairMetaData> pairs = new HashMap<>();

    private static JRadioButton buyRadio;
    private static JRadioButton sellRadio;

    private static JTextField totalAmtField;
    private static JTextField orderQtyField;

    private static JTextField startpriceField;
    private static JTextField endpriceField;

    private static JRadioButton flatRadio;
    private static JRadioButton upRadio;
    private static JRadioButton downRadio;
    private static JRadioButton randomizerRadio;
    private static double randomStrength = .3;

    private static JTextArea ordersArea;

    private static JButton startButton;
    private static JButton cancelButton;


    private static JLabel baseorcounterLabel = new JLabel();
    private static String baseorcounterString = "Counter";


    private static boolean stop = false;;

    private void startMainScalePanel(String accountName) throws IOException {

        //connected, set title
        setTitle("Binance Scaler - " + accountName);

        //setup main panel
        scaleMainPanel = new JPanel();
        scaleMainPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        add(scaleMainPanel, gbc);


        // PAIR PANEL
        JPanel pairPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        scaleMainPanel.add(pairPanel, gbc);
        pairPanel.setBorder(BorderFactory.createTitledBorder("pair"));
        pairPicker = new JComboBox<>();
        pairPicker.setEditable(true);
        pairPicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e);


                setBaseorcounter(e);

                if (e.getModifiers() == 4) {
                    try {
                        pickerAddToFavorites(e);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else if (e.getModifiers() == 8) {
                    try {
                        pickerDeleteFavorite(e);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        ComboPopup popup = (ComboPopup) pairPicker.getUI().getAccessibleChild(pairPicker, 0);
        ((JComponent) popup).setPreferredSize(new Dimension(150, 500));
        ((JComponent) popup).setLayout(new GridLayout(1, 1));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pairPanel.add(pairPicker, gbc);


        // FILL PAIR PICKER~
        pairs = BinanceAPI.getPairs();

        System.out.println(pairs.get(CurrencyPair.NEO_BTC));

        //first add favorite pairs

        //get lines
        String fileName = "lines.txt";
        Object[] s = null;
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            s = stream.toArray();

            for (int i = 0; i < s.length; i++) {
//                acctStringPull(s[i].toString());

                if (s[i].toString().contains("favorite")) {
                    pairPicker.addItem(s[i].toString().substring(s[i].toString().indexOf("favorite pair:") + 14, s[i].toString().length()));
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //add all
        Set pairset = pairs.keySet();
        ArrayList<CurrencyPair> ps = new ArrayList<>(pairset);
        Collections.sort(ps);

        String lastCounter = "";
        for (int i = 0; i < pairs.size(); i++) {

            String p = ps.get(i).toString();


            if (p.endsWith("BTC") && btcpairsCheckbox.isSelected()) {
                if (!lastCounter.contains(p.substring(0, p.indexOf("BTC")))) {
                    pairPicker.addItem("---");
                }
                pairPicker.addItem(p);
                lastCounter = p.substring(0, p.indexOf("BTC"));
            } else if (p.endsWith("USDT") && usdtpairsCheckbox.isSelected()) {
                if (!lastCounter.contains(p.substring(0, p.indexOf("USDT")))) {
                    pairPicker.addItem("---");
                }
                pairPicker.addItem(p);
                lastCounter = p.substring(0, p.indexOf("USDT"));
            } else if (p.endsWith("ETH") && ethpairsCheckbox.isSelected()) {
                if (!lastCounter.contains(p.substring(0, p.indexOf("ETH")))) {
                    pairPicker.addItem("---");
                }
                pairPicker.addItem(p);
                lastCounter = p.substring(0, p.indexOf("ETH"));
            } else if (p.endsWith("BNB") && bnbpairsCheckbox.isSelected()) {
                if (!lastCounter.contains(p.substring(0, p.indexOf("BNB")))) {
                    pairPicker.addItem("---");
                }
                pairPicker.addItem(p);
                lastCounter = p.substring(0, p.indexOf("BNB"));
            }

        }


        // buy or sell radio panel
        JPanel buysellradioPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = .1;
        gbc.weighty = .1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        scaleMainPanel.add(buysellradioPanel, gbc);
        buysellradioPanel.setBorder(BorderFactory.createTitledBorder("order type"));
        buyRadio = new JRadioButton("Buy");
        buyRadio.setSelected(true);
        sellRadio = new JRadioButton("Sell");
        ButtonGroup buysellGroup = new ButtonGroup();
        buysellGroup.add(buyRadio);
        buysellGroup.add(sellRadio);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        buysellradioPanel.add(buyRadio, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        buysellradioPanel.add(sellRadio, gbc);

        //total amt panel
        JPanel totalAmtPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = .1;
        gbc.weighty = .1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(totalAmtPanel, gbc);
        totalAmtPanel.setBorder(BorderFactory.createTitledBorder("total size"));
        totalAmtField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        totalAmtPanel.add(totalAmtField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        baseorcounterLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {


                String currentPair = pairPicker.getSelectedItem().toString();

                System.out.println(currentPair);

                try {
                    toggleCounterorbase(currentPair);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        totalAmtPanel.add(baseorcounterLabel, gbc);


        //order qty panel
        JPanel orderQtyPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = .1;
        gbc.weighty = .1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(orderQtyPanel, gbc);
        orderQtyPanel.setBorder(BorderFactory.createTitledBorder("# of orders"));
        orderQtyField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        orderQtyPanel.add(orderQtyField, gbc);

        //startprice panel
        JPanel startpricePanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = .1;
        gbc.weighty = .1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(startpricePanel, gbc);
        startpricePanel.setBorder(BorderFactory.createTitledBorder("upper price"));
        startpriceField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        startpricePanel.add(startpriceField, gbc);

        //endprice panel
        JPanel endpricePanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = .1;
        gbc.weighty = .1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(endpricePanel, gbc);
        endpricePanel.setBorder(BorderFactory.createTitledBorder("lower price"));
        endpriceField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        endpricePanel.add(endpriceField, gbc);

        //distribution panel
        JPanel distributionPanl = new JPanel(new GridBagLayout());
        distributionPanl.setBorder(BorderFactory.createTitledBorder("distribution"));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = .1;
        gbc.weighty = .1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(distributionPanl, gbc);
        flatRadio = new JRadioButton("flat");
        flatRadio.setSelected(true);
        upRadio = new JRadioButton("up");
        downRadio = new JRadioButton("down");
        randomizerRadio = new JRadioButton("randomizer");


        ButtonGroup distroButtonGroup = new ButtonGroup();
        distroButtonGroup.add(flatRadio);
        distroButtonGroup.add(upRadio);
        distroButtonGroup.add(downRadio);
        distroButtonGroup.add(randomizerRadio);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(flatRadio, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(upRadio, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(downRadio, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(randomizerRadio, gbc);


        //preview/start button panel
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(buttonsPanel, gbc);
        //preview button
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    previewButtonPressed();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        buttonsPanel.add(previewButton, gbc);
        //startButton
        startButton = new JButton("Start");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        buttonsPanel.add(startButton, gbc);
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startOrders();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    if (e1.getMessage().contains("MIN_NOTIONAL")) {
                        setTitle("Error: single order size too small");
                    } else if (e1.getMessage().contains("invalid price")) {
                        setTitle("Error: invalid price");
                    } else {
                        setTitle(e1.getMessage());
                    }
                }
            }
        });
        //cancelButton
        cancelButton = new JButton("Cancel");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        buttonsPanel.add(cancelButton, gbc);
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop = true;

            }
        });

        //preview Scrollpanel
        ordersArea = new JTextArea();
        ordersArea.setEditable(false);
        JScrollPane previewScrollpane = new JScrollPane(ordersArea);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 6;
        scaleMainPanel.add(previewScrollpane, gbc);


        //EVENTLISTENERS FOR STARTBUTTON
        pairPicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
            }
        });

        buyRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
            }
        });

        sellRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
            }
        });

        totalAmtField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                startButton.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        orderQtyField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                startButton.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        startpriceField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                startButton.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        endpriceField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                startButton.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });

        flatRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
            }
        });

        upRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
            }
        });

        downRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
            }
        });


    }

    private void toggleCounterorbase(String pair) throws IOException {

        int roundscale = BinanceAPI.getRoundscale(pair);
        int amtscale = BinanceAPI.getAmountscale(pair);

        String counter = pair.substring(0, pair.indexOf("/"));
        String base = pair.substring(pair.indexOf("/") + 1, pair.length());

        if (counter.contains("BTC") || counter.contains("ETH") || counter.contains("USDT") || counter.contains("BNB")) {

            if (baseorcounterLabel.getText().contains(counter)) {
                baseorcounterLabel.setText(base);
                totalAmtField.setText( new BigDecimal(Double.parseDouble(totalAmtField.getText()) * ((Double.parseDouble(startpriceField.getText())+Double.parseDouble(endpriceField.getText()))/2)).setScale(roundscale, RoundingMode.HALF_DOWN) + "");
            } else {
                baseorcounterLabel.setText(counter);
                totalAmtField.setText( new BigDecimal(Double.parseDouble(totalAmtField.getText()) / ((Double.parseDouble(startpriceField.getText())+Double.parseDouble(endpriceField.getText()))/2)).setScale(amtscale, RoundingMode.HALF_DOWN) + "");
            }


        } else if (baseorcounterLabel.getText().contains("BTC") || baseorcounterLabel.getText().contains("ETH") || baseorcounterLabel.getText().contains("USDT") || baseorcounterLabel.getText().contains("BNB")) {

            baseorcounterLabel.setText(counter);
            totalAmtField.setText( new BigDecimal(Double.parseDouble(totalAmtField.getText()) / ((Double.parseDouble(startpriceField.getText())+Double.parseDouble(endpriceField.getText()))/2)).setScale(amtscale, RoundingMode.HALF_DOWN) + "");
        } else {
            baseorcounterLabel.setText(base);
            totalAmtField.setText( new BigDecimal(Double.parseDouble(totalAmtField.getText()) * ((Double.parseDouble(startpriceField.getText())+Double.parseDouble(endpriceField.getText()))/2)).setScale(roundscale, RoundingMode.HALF_DOWN) + "");
        }


    }

    private void setBaseorcounter(ActionEvent e) {

        String pair = e.toString().substring(e.toString().indexOf("selectedItemReminder=") + 21, e.toString().length() - 1);

        String counter = pair.substring(0, pair.indexOf("/"));
        String base = pair.substring(pair.indexOf("/") + 1, pair.length());

        baseorcounterLabel.setText(counter);
    }

    //still need to fillPicker with favs at top

    private void pickerDeleteFavorite(ActionEvent e) throws IOException {

        String pair = e.toString().substring(e.toString().indexOf("selectedItemReminder=") + 21, e.toString().length() - 1);

        System.out.println("remove from favs " + pair);

        if (!pair.contains("---")) {
            pairPicker.removeItemAt(pairPicker.getSelectedIndex());
        }

        java.util.List<String> lines = FileUtils.readLines(new File("lines.txt"));
        List<String> updatedLines = lines.stream().filter(s -> !s.contains(pair)).collect(Collectors.toList());
        FileUtils.writeLines(new File("lines.txt"), updatedLines, false);


    }

    private void pickerAddToFavorites(ActionEvent e) throws IOException {

        System.out.println("adding to favorites " + e.toString().substring(e.toString().indexOf("selectedItemReminder=") + 21, e.toString().length() - 1));

        pairPicker.insertItemAt(e.toString().substring(e.toString().indexOf("selectedItemReminder=") + 21, e.toString().length() - 1), 0);

        String str = "favorite pair:" + e.toString().substring(e.toString().indexOf("selectedItemReminder=") + 21, e.toString().length() - 1);
        BufferedWriter writer = new BufferedWriter(new FileWriter("lines.txt", true));
        writer.append(str);
        writer.append('\n');

        writer.close();

    }


    private void startOrders() throws InterruptedException, IOException {

        System.out.println("getting price roundingscale");

        SingleTrade t = trades.get(0);

        int roundscale = BinanceAPI.getRoundscale(t.pair);

        int amtscale = BinanceAPI.getAmountscale(t.pair);

        ArrayList<Double> bidask = BinanceAPI.getBidask(t);

        System.out.println(roundscale);

        System.out.println("starting orders..");



        if ((t.side == Order.OrderType.BID && t.price > bidask.get(0)) || (t.side == Order.OrderType.ASK && t.price < bidask.get(1))) {

            setTitle("ERROR: order would execute immediately at market (fundus are safu)");

        } else {
            Thread t1 = new Thread(new Runnable() {
                public void run()
                {



                    SwingUtilities.invokeLater(() -> {
                        OrderPlaceWindow orderWindow = null;
                        try {
                            UIManager.setLookAndFeel(new SubstanceGraphiteAquaLookAndFeel());
                        } catch (Exception e) { System.out.println("Substance look&feel load error!"); e.printStackTrace(); }

                        try {
                            orderWindow = new OrderPlaceWindow("order placer", new ArrayList<>(trades), bidask, amtscale, roundscale);
                        } catch (Exception e) { System.out.println("window object create error"); e.printStackTrace(); }

                        if (orderWindow != null) {
                            orderWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                            orderWindow.setSize(300, 500);
                            orderWindow.setLocationRelativeTo(null);
                            orderWindow.setVisible(true);
                        }
                    });




                }});
            t1.start();

        }







    }

    private void previewButtonPressed() throws InterruptedException, IOException {


        String pair = pairPicker.getSelectedItem().toString();


        String orderType = buyRadio.isSelected() ? "Buy" : "Sell";




        int numberOfOrders = Integer.parseInt(orderQtyField.getText());

        double upperPrice = Double.parseDouble(startpriceField.getText());
        double lowerPrice = Double.parseDouble(endpriceField.getText());

        double totalSize = Double.parseDouble(totalAmtField.getText());

        String distribution = "flat";
        if (flatRadio.isSelected()) {
            distribution = "flat";
        } else if (upRadio.isSelected()) {
            distribution = "up";
        } else if (downRadio.isSelected()) {
            distribution = "down";
        } else if (randomizerRadio.isSelected()) {
            distribution = "randomizer";
        }


        makeOrderBatch(pair, orderType, totalSize, numberOfOrders, upperPrice, lowerPrice, distribution);


    }

    private void makeOrderBatch(String pair, String orderType, double totalSize, int numberOfOrders, double upperPrice, double lowerPrice, String distribution) throws IOException {

        ArrayList<Double> amounts = new ArrayList<>();
        ArrayList<Double> prices = new ArrayList<>();
        int roundscale = BinanceAPI.getRoundscale(pair);
        int amtscale = BinanceAPI.getAmountscale(pair);

        double rangeAmt = upperPrice - lowerPrice;
        double steps = rangeAmt / (numberOfOrders - 1);

        for (int i = 0; i < numberOfOrders; i++) {
            if (i == 0) {
                prices.add(upperPrice);
            } else if (i == numberOfOrders - 1) {
                prices.add(lowerPrice);
            } else {

                BigDecimal bd = new BigDecimal(Double.toString(lowerPrice + (steps * i)));


                bd = bd.setScale(roundscale, RoundingMode.HALF_EVEN);

                prices.add(bd.doubleValue());
            }
        }

        Collections.sort(prices);







        ArrayList<Double> distributedTotal = new ArrayList<>();
        double allSum = 0;

        double singleOrderAmt = 0;
        if (distribution.contains("flat")) {

            singleOrderAmt = totalSize / numberOfOrders;

            BigDecimal bd = new BigDecimal(Double.toString(singleOrderAmt));
            bd = bd.setScale(4, RoundingMode.HALF_EVEN);

            singleOrderAmt = bd.doubleValue();


            if (pair.endsWith(baseorcounterLabel.getText())){
                for (int i = 0; i < numberOfOrders; i++) {
                    System.out.println("totalsize: " + totalSize + "num orders: " + numberOfOrders + " price: " + prices.get(i));
                    singleOrderAmt = (totalSize / numberOfOrders)/prices.get(i);
                    distributedTotal.add((singleOrderAmt)); //current price / total amt
                }

            } else {
                for (int i = 0; i < numberOfOrders; i++) {
                distributedTotal.add(singleOrderAmt);
                }
            }

        } else if (distribution.contains("randomizer")) {
            Random random = new Random();




            boolean add = true;
            double diff = 0;

            if (numberOfOrders > 1) {

                for (int i = 0; (numberOfOrders % 2 == 0 ? i < numberOfOrders : i < numberOfOrders - 1); i++) {

                    if (pair.endsWith(baseorcounterLabel.getText())){
                        singleOrderAmt = (totalSize / numberOfOrders)/prices.get(i);
                    } else{
                        singleOrderAmt = totalSize / numberOfOrders;
                    }

                    if (add) {
                        double ran = random.nextDouble() * randomStrength;
                        double thisOne = singleOrderAmt * (ran + 1);
                        distributedTotal.add(i, thisOne);
                        diff = singleOrderAmt - thisOne;
                        add = false;
                    } else {
                        distributedTotal.add(singleOrderAmt + diff);
                        add = true;
                    }
                }
                if (distributedTotal.size() < numberOfOrders) {
                    distributedTotal.add(singleOrderAmt);
                }
            } else {
                distributedTotal.add(singleOrderAmt);
            }


            for (int i = 0; i < distributedTotal.size(); i++) {
                //add all
                allSum += distributedTotal.get(i);
            }


        } else if (distribution.contains("up") || distribution.contains("down")) {

            ArrayList<Double> pricePointPercentages = new ArrayList<>();

            // Min and max percentage of the amount allocated per price point
            double minPercentage = 0.05;
            double maxPercentage = 0.4;


            for (int i = 0; i < numberOfOrders; i++) {
                pricePointPercentages.add(i, (minPercentage + (i * (maxPercentage - minPercentage)) / (numberOfOrders + 1)));
            }


            double leftover = 0;
            double distributionSum = 0;
            for (Double d : pricePointPercentages) {
                distributionSum += d;
            }

            for (int i = 0; i < pricePointPercentages.size(); i++) {

                double val = (pricePointPercentages.get(i) * totalSize) / distributionSum + leftover;

                if (pair.endsWith(baseorcounterLabel.getText())){
                    val /= prices.get(i);
                }

                distributedTotal.add(val);

            }

            for (int i = 0; i < distributedTotal.size(); i++) {
                //add all
                allSum += distributedTotal.get(i);
            }

            if (distribution.contains("up")) {
                Collections.reverse(distributedTotal);
            }


            singleOrderAmt = totalSize / numberOfOrders;
            BigDecimal bd = new BigDecimal(Double.toString(singleOrderAmt));
            bd = bd.setScale(4, RoundingMode.HALF_EVEN);
            singleOrderAmt = bd.doubleValue();
            for (int i = 0; i < numberOfOrders; i++) {
                amounts.add(singleOrderAmt);
            }

        }


//delet?
//        // get amounts
//        boolean baseOrCounterBool = false;
//        String counter = pair.substring(0, pair.indexOf("/"));
//        String base = pair.substring(pair.indexOf("/") + 1, pair.length());
//
//        System.out.println("counter: " + counter);
//
//        //if current field is same as pair base
//        if (base.contains(baseorcounterLabel.getText()))
//
//        {
//            System.out.println(pair + ", base (" + base + ")");
//            baseOrCounterBool = true;
//        }





        trades.clear();

        ordersArea.setText("");
        for (int i = 0; i < prices.size(); i++)

        {
            trades.add(new SingleTrade(pair, orderType, new BigDecimal(distributedTotal.get(i)).setScale(amtscale, RoundingMode.HALF_DOWN).doubleValue(), new BigDecimal(prices.get(i)).setScale(roundscale, RoundingMode.HALF_DOWN).doubleValue()));
            ordersArea.append(pair + " " + orderType + " " + new BigDecimal(distributedTotal.get(i)).setScale(amtscale, RoundingMode.HALF_DOWN) + " at " + new BigDecimal(prices.get(i)).setScale(roundscale, RoundingMode.HALF_DOWN) + "\n");
        }

        startButton.setEnabled(true);


        for (int i = 0; i < prices.size(); i++) {

            System.out.println("price: " + prices.get(i) + " amt: " + distributedTotal.get(i) + " total: " + allSum);

        }


    }


    private void connect(String accountName, String key, String sec) throws IOException {

        setTitle("Connecting to API...");

        BinanceAPI.connect(key, sec);

        String acctStatus = BinanceAPI.testConnection();

        if (!acctStatus.contains("connectionSuccess")) {
            setTitle(acctStatus);
        } else {

            chooseAccountPanel.setEnabled(false);
            chooseAccountPanel.setVisible(false);
            addAccountButton.setEnabled(false);
            addAccountButton.setVisible(false);
            pairsCheckboxPanel.setEnabled(false);
            pairsCheckboxPanel.setVisible(false);

            startMainScalePanel(accountName);
        }


    }

    private void addAccountButton() {

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        addAccountButton = new JButton("New Account");
        add(addAccountButton, gbc);

        addAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newAccountPopup();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }


    private static JPanel pairsCheckboxPanel;
    private static JCheckBox btcpairsCheckbox;
    private static JCheckBox usdtpairsCheckbox;
    private static JCheckBox ethpairsCheckbox;
    private static JCheckBox bnbpairsCheckbox;


    private void basePairChecksPanel() {

        btcpairsCheckbox = new JCheckBox("BTC", true);
        usdtpairsCheckbox = new JCheckBox("USDT", true);
        ethpairsCheckbox = new JCheckBox("ETH", true);
        bnbpairsCheckbox = new JCheckBox("BNB", true);

        pairsCheckboxPanel = new JPanel(new GridBagLayout());
        pairsCheckboxPanel.setBorder(BorderFactory.createTitledBorder("Base Pairs"));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(pairsCheckboxPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pairsCheckboxPanel.add(btcpairsCheckbox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pairsCheckboxPanel.add(usdtpairsCheckbox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pairsCheckboxPanel.add(ethpairsCheckbox, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pairsCheckboxPanel.add(bnbpairsCheckbox, gbc);

        JLabel baseInstructionsLabel = new JLabel("right click to add a pair to favorites");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridwidth = 4;
        pairsCheckboxPanel.add(baseInstructionsLabel, gbc);

        JLabel baseInstructionsLabel2 = new JLabel("middle click to remove from favorites");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.weighty = 0;

        pairsCheckboxPanel.add(baseInstructionsLabel2, gbc);
        gbc.gridwidth = 1;

    }


    private void newAccountPopup() throws IOException {


        JPanel addAccountPanel = new JPanel();

        addAccountPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel nameLabel = new JLabel("Name: ");
        JTextField nameField = new JTextField("binance acct #1");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        addAccountPanel.add(nameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addAccountPanel.add(nameField, gbc);


        JLabel apiKeyLabel = new JLabel("API key: ");
        JTextField apiKeyField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        addAccountPanel.add(apiKeyLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addAccountPanel.add(apiKeyField, gbc);


        JLabel apiSecLabel = new JLabel("API secret: ");
        JTextField apiSecField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        addAccountPanel.add(apiSecLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addAccountPanel.add(apiSecField, gbc);


        //send it
        int result = JOptionPane.showConfirmDialog(null, addAccountPanel, "Add Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {

            String str = "<<1>>" + nameField.getText() + "<<2>>" + apiKeyField.getText() + "<<3>>" + apiSecField.getText() + "<<4>>";
            BufferedWriter writer = new BufferedWriter(new FileWriter("lines.txt", true));
            writer.append(str);
            writer.append('\n');

            writer.close();


            fillAccounts();
        }


    }

    private void chooseAccountFrame() throws IOException {

        chooseAccountPanel.setLayout(new GridBagLayout());
        chooseAccountPanel.setBorder(BorderFactory.createTitledBorder("Account"));


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(chooseAccountPanel, gbc);


        add(noAccountsLabel, gbc);

        fillAccounts();


    }

    private void fillAccounts() throws IOException {

        chooseAccountPanel.removeAll();

        String fileName = "lines.txt";
        Object[] s = null;
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {


            s = stream.toArray();

            for (int i = 0; i < s.length; i++) {
                acctStringPull(s[i].toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        chooseAccountPanel.setVisible(s.length != 0);
        noAccountsLabel.setVisible(s.length == 0);


        revalidate();

    }

    private void acctStringPull(String line) {

        if (line.length() != 0 && !line.contains("favorite")) {


            String accountName = line.substring(line.indexOf("<<1>>") + 5, line.indexOf("<<2>>"));
            String key = line.substring(line.indexOf("<<2>>") + 5, line.indexOf("<<3>>"));
            String sec = line.substring(line.indexOf("<<3>>") + 5, line.indexOf("<<4>>"));

            JPanel singleAccountPan = new JPanel(new GridBagLayout());

            JLabel singleAccountLabel = new JLabel(accountName);

            JButton singleAccountConnectButton = new JButton("connect");
            singleAccountConnectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    try {
                        connect(accountName, key, sec);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            });


            JButton singleAccountDeleteButton = new JButton("delete");
            singleAccountDeleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        deleteAccountPopup(accountName);

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }


                }
            });

            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.weightx = 1;
            singleAccountPan.add(singleAccountLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0;
            singleAccountPan.add(singleAccountConnectButton, gbc);
            gbc.gridx = 2;
            gbc.weightx = 0;
            singleAccountPan.add(singleAccountDeleteButton, gbc);

            gbc.gridy = chooseAccountPanel.getComponentCount();
            chooseAccountPanel.add(singleAccountPan, gbc);

        }
    }

    private void deleteAccountPopup(String accountName) throws IOException {

        int result = JOptionPane.showConfirmDialog((Component) null, "Are you sure you want to delete [" + accountName + "]?",
                "alert", JOptionPane.OK_CANCEL_OPTION);

        if (result == 0) {
            deleteAccountLine1(accountName);
        }

    }

    private void deleteAccountLine1(String accountName) throws IOException {


        String fileName = "lines.txt";

        //read file into stream, try-with-resources

        acctLinesForDelete.clear();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

//            stream.forEach(GUI::deleteAccountLine2);

            Object[] s = stream.toArray();

            for (int i = 0; i < s.length; i++) {
                deleteAccountLine2(s[i].toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter pw = new PrintWriter("lines.txt");
        pw.close();

        for (int i = 0; i < acctLinesForDelete.size(); i++) {
            String str = acctLinesForDelete.get(i);

            if (!acctLinesForDelete.get(i).contains(accountName)) {
                BufferedWriter writer = new BufferedWriter(new FileWriter("lines.txt", true));
                writer.append(str);
                writer.append('\n');


                writer.close();
            }
        }

        fillAccounts();


    }

    private void deleteAccountLine2(String s) {

        acctLinesForDelete.add(s);

    }


}
