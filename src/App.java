import controller.CalculatorController;
import model.CalculatorModel;
import view.CalculatorView;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorView view = new CalculatorView();
            CalculatorModel model = new CalculatorModel();
            new CalculatorController(view, model);
            view.setVisible(true);
        });
    }
}
