import org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel;
import javax.swing.*;
import java.io.IOException;

public class Main {

    private static GUI gui = null;

    public static void main(String[] args) throws InterruptedException, IOException {

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
                gui.setSize(450, 620);
                gui.setLocationRelativeTo(null);
                gui.setVisible(true);
            }
        });
    }
}
