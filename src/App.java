import controller.CalculatorController;
import view.CalculatorView;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorView view = new CalculatorView();
            new CalculatorController(view);
            view.setVisible(true);
        });
    }
}
