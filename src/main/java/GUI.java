import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GUI extends JFrame {

    private static ArrayList<String> acctLinesForDelete = new ArrayList<>();

    private static GridBagConstraints gbc = new GridBagConstraints();

    private static JPanel chooseAccountPanel = new JPanel();
    private static JLabel noAccountsLabel = new JLabel("No accounts set up");
    private static JButton addAccountButton;

    private static JPanel scaleMainPanel;

    private JPanel pairPickerPanel;

    ArrayList<Trade> trades = new ArrayList<>();

    public GUI(String title) throws IOException {
        super(title);

        setLayout(new GridBagLayout());

        chooseAccountFrame();

        addAccountButton();


    }

    private JComboBox<String> pairPicker;

    private static JRadioButton buyRadio;
    private static JRadioButton sellRadio;

    private static JTextField totalAmtField;
    private static JTextField orderQtyField;

    private static JTextField startpriceField;
    private static JTextField endpriceField;

    private static JRadioButton flatRadio;
    private static JRadioButton upRadio;
    private static JRadioButton downRadio;

    private static JTextArea ordersArea;

    private static JButton startButton;



    private void startMainScalePanel(String accountName) throws IOException {

        //connected, set title
        setTitle("Binance Scaler - " + accountName);

        //setup main panel
        scaleMainPanel = new JPanel();
        scaleMainPanel.setLayout(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        add(scaleMainPanel, gbc);
        // PAIR PANEL
        JPanel pairPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.NORTHWEST;
        scaleMainPanel.add(pairPanel, gbc);
        pairPanel.setBorder(BorderFactory.createTitledBorder("pair"));
        pairPicker = new JComboBox<>();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        pairPanel.add(pairPicker, gbc);
        // FILL PAIR PICKER~
        ArrayList<String> pairs = BinanceAPI.getPairs();
        for (String p : pairs) {
            pairPicker.addItem(p);
        }

        // buy or sell radio panel
        JPanel buysellradioPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.NORTHWEST;
        scaleMainPanel.add(buysellradioPanel, gbc);
        buysellradioPanel.setBorder(BorderFactory.createTitledBorder("order type"));
        buyRadio = new JRadioButton("Buy");
        buyRadio.setSelected(true);
        sellRadio = new JRadioButton("Sell");
        ButtonGroup buysellGroup = new ButtonGroup();
        buysellGroup.add(buyRadio);
        buysellGroup.add(sellRadio);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        buysellradioPanel.add(buyRadio, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        buysellradioPanel.add(sellRadio, gbc);

        //total amt panel
        JPanel totalAmtPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(totalAmtPanel, gbc);
        totalAmtPanel.setBorder(BorderFactory.createTitledBorder("total size"));
        totalAmtField = new JTextField();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        totalAmtPanel.add(totalAmtField, gbc);

        //order qty panel
        JPanel orderQtyPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(orderQtyPanel, gbc);
        orderQtyPanel.setBorder(BorderFactory.createTitledBorder("# of orders"));
        orderQtyField = new JTextField();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        orderQtyPanel.add(orderQtyField, gbc);

        //startprice panel
        JPanel startpricePanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(startpricePanel, gbc);
        startpricePanel.setBorder(BorderFactory.createTitledBorder("upper price"));
        startpriceField = new JTextField();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        startpricePanel.add(startpriceField, gbc);

        //endprice panel
        JPanel endpricePanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(endpricePanel, gbc);
        endpricePanel.setBorder(BorderFactory.createTitledBorder("lower price"));
        endpriceField = new JTextField();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        endpricePanel.add(endpriceField, gbc);

        //distribution panel
        JPanel distributionPanl = new JPanel(new GridBagLayout());
        distributionPanl.setBorder(BorderFactory.createTitledBorder("distribution"));
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(distributionPanl, gbc);
        flatRadio = new JRadioButton("flat");
        flatRadio.setSelected(true);
        upRadio = new JRadioButton("up");
        downRadio = new JRadioButton("down");
        ButtonGroup distroButtonGroup = new ButtonGroup();
        distroButtonGroup.add(flatRadio);
        distroButtonGroup.add(upRadio);
        distroButtonGroup.add(downRadio);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(flatRadio, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(upRadio, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER;
        distributionPanl.add(downRadio, gbc);


        //preview/start button panel
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = .1; gbc.weighty = .1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH;
        scaleMainPanel.add(buttonsPanel, gbc);
        //preview button
        JButton previewButton = new JButton("Preview");
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previewButtonPressed();
            }
        });
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        buttonsPanel.add(previewButton, gbc);
        //startButton
        startButton = new JButton("Start");
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
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

        //preview Scrollpanel
        ordersArea = new JTextArea();
        ordersArea.setEditable(false);
        ordersArea.setText("order 1 \norder 2 \norder 3 \n order 4");
        JScrollPane previewScrollpane = new JScrollPane(ordersArea);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; gbc.weighty = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.BOTH; gbc.gridheight = 6;
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



    private void startOrders() throws InterruptedException, IOException {

        System.out.println("getting price roundingscale");
        int roundscale = BinanceAPI.getRoundscale(trades.get(0));

        System.out.println(roundscale);

        System.out.println("starting orders..");



        for (Trade t : trades) {
            System.out.println(t.pair + " " + t.side + " " + t.amt + " at " + t.price);

            String id = BinanceAPI.placeOrder(t, roundscale);

            if (id == null) {
                System.out.println("error placing order!");
                setTitle("ERROR placing order!");
                break;
            } else {
                setTitle("order success");
                Thread.sleep(1000);
            }
        }

    }

    private void placeTrade(Trade t) {


    }

    private void previewButtonPressed() {

        String pair = pairPicker.getSelectedItem().toString();


        String orderType = buyRadio.isSelected() ? "Buy":"Sell";

        double totalSize = Double.parseDouble(totalAmtField.getText());
        int numberOfOrders = Integer.parseInt(orderQtyField.getText());

        double upperPrice = Double.parseDouble(startpriceField.getText());
        double lowerPrice = Double.parseDouble(endpriceField.getText());

        String distribution = "flat";
        if (flatRadio.isSelected()) {
            distribution = "flat";
        } else if (upRadio.isSelected()) {
            distribution = "up";
        } else if (downRadio.isSelected()) {
            distribution = "down";
        }


        makeOrderBatch(pair, orderType, totalSize, numberOfOrders, upperPrice, lowerPrice, distribution);


    }

    private void makeOrderBatch(String pair, String orderType, double totalSize, int numberOfOrders, double upperPrice, double lowerPrice, String distribution) {

        double singleOrderAmt = 0;

        if (distribution.contains("flat")) {
            singleOrderAmt = totalSize / numberOfOrders;

            BigDecimal bd = new BigDecimal(Double.toString(singleOrderAmt));
            bd = bd.setScale(2, RoundingMode.HALF_EVEN);

            singleOrderAmt = bd.doubleValue();

        }

        ArrayList<Double> prices = new ArrayList<>();

        double rangeAmt = upperPrice-lowerPrice;
        double steps = rangeAmt / (numberOfOrders-1);

        for (int i = 0; i < numberOfOrders; i++) {
            if (i == 0) {
                prices.add(upperPrice);
            } else if (i == numberOfOrders-1) {
                prices.add(lowerPrice);
            } else {

                BigDecimal bd = new BigDecimal(Double.toString(lowerPrice + (steps * i)));
                bd = bd.setScale(8, RoundingMode.HALF_EVEN);

                prices.add(bd.doubleValue());
            }
        }

        Collections.sort(prices);

        trades.clear();

        ordersArea.setText("");
        for (Double p : prices) {
            trades.add(new Trade(pair, orderType, singleOrderAmt, p));
            ordersArea.append(pair + " " + orderType + " " + singleOrderAmt + " at " + new BigDecimal(p).setScale(8, RoundingMode.HALF_EVEN) + "\n");
        }

        startButton.setEnabled(true);


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

            startMainScalePanel(accountName);
        }





    }

    private void addAccountButton() {

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(5,5,5,5);
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
        gbc.insets = new Insets(5,5,5,5);
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

        if (line.length() != 0) {


            String accountName = line.substring(line.indexOf("<<1>>") + 5, line.indexOf("<<2>>"));
            String key = line.substring(line.indexOf("<<2>>") + 5, line.indexOf("<<3>>"));
            String sec = line.substring(line.indexOf("<<3>>") + 5, line.indexOf("<<4>>"));

            JPanel singleAccountPan = new JPanel();

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

            singleAccountPan.add(singleAccountLabel);
            singleAccountPan.add(singleAccountConnectButton);
            singleAccountPan.add(singleAccountDeleteButton);

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

    private void setupPairPicker() throws IOException {

        pairPickerPanel = new JPanel(new GridBagLayout());
        pairPickerPanel.setBorder(BorderFactory.createTitledBorder("Pair"));
        pairPicker = new JComboBox<>();

        ArrayList<String> pairs = BinanceAPI.getPairs();

        for (String p : pairs) {
            pairPicker.addItem(p);
        }
        add(pairPicker);
    }


}
