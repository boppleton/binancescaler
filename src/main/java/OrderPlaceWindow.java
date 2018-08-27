import org.knowm.xchange.dto.Order;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class OrderPlaceWindow extends JFrame {

    private ArrayList<SingleTrade> trades;
    private ArrayList<Double> bidask;
    int amtscale;
    int roundscale;

    private GridBagConstraints gbc = new GridBagConstraints();

    private JPanel tradesPanel;

    private Thread t1;

    private boolean keepGoing = true;

    public OrderPlaceWindow(String title, ArrayList<SingleTrade> trades, ArrayList<Double> bidask, int amtscale, int roundscale) throws HeadlessException {
        super(title);
        this.trades = trades;
        this.bidask = bidask;
        this.amtscale = amtscale;
        this.roundscale = roundscale;

        setLayout(new GridBagLayout());

        JButton stopButton = new JButton("Cancel");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(stopButton, gbc);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keepGoing = false;
                setTitle("stopped");
                dispose();
            }
        });

        tradesPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(tradesPanel);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;

        add(scrollPane, gbc);

        for (int i = 0; i < trades.size(); i++) {

            gbc.gridy = i;
            gbc.weightx = 1;
            gbc.weighty = 0;
            tradesPanel.add(new JLabel("#" + (i + 1) + ". " + (trades.get(i).side.toString().contains("BID") ? "Buy" : "Sell") + " " + trades.get(i).amt + " " + trades.get(i).pair.substring(0, trades.get(i).pair.indexOf("/")) + " at " + trades.get(i).price), gbc);
        }

        //weightfiller
        gbc.gridx = 0;
        gbc.gridy = trades.size() + 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        tradesPanel.add(new JLabel(""), gbc);

        placeOrders();

    }

    private void placeOrders() {

        t1 = new Thread(new Runnable() {
            public void run() {

                for (int i = 0; i < trades.size(); i++) {

                    if (keepGoing) {

                        SingleTrade t = trades.get(i);

                        System.out.println(t.pair + " " + t.side + " " + t.amt + " at " + t.price);

                        if ((t.side == Order.OrderType.BID && t.price > bidask.get(0)) || (t.side == Order.OrderType.ASK && t.price < bidask.get(1))) {

                            setTitle("ERROR: order would execute immediately at market, skipping");

                        } else {

                            String id = null;
                            try {
                                setTitle("placing order..");
                                id = BinanceAPI.placeOrder(t, roundscale, amtscale);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (id == null) {
                                System.out.println("error placing order!");
                                setTitle("error placing order!");
                                JLabel currentLabel = (JLabel) tradesPanel.getComponent(i);


                                currentLabel.setText("Success (" + id + ") " + currentLabel.getText());
                                SwingUtilities.invokeLater(() -> {
                                    currentLabel.setBackground(new Color(255, 34, 57));
                                    currentLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                                    revalidate();

                                });


                                break;
                            } else {
                                setTitle("order success - " + id);
                                JLabel currentLabel = (JLabel) tradesPanel.getComponent(i);

                                currentLabel.setText("Success (" + id + ") " + currentLabel.getText());
                                SwingUtilities.invokeLater(() -> {
                                    currentLabel.setBackground(new Color(48, 183, 48));
                                    currentLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, currentLabel.getFont().getSize()));
                                    revalidate();
                                });
                            }

                            try {
                                Thread.sleep(trades.size() > 50 ? 1000 : 300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        t1.start();


    }


}
