package view;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;

/** RPN calculator view with an accumulator and four visible stack slots. */
public final class CalculatorView extends JFrame {
    public enum Command {
        PUSH,
        POP,
        DROP,
        DROP_ALL,
        SWAP,
        CLEAR_ACCUMULATOR,
        BACKSPACE,
        OPPOSITE,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }

    public interface Listener {
        void onDigit(String digit);

        void onDecimalSeparator();

        void onCommand(Command command);
    }

    private final JTextField accumulatorDisplay = valueDisplay(true);
    private final JTextField[] stackDisplays = {
            valueDisplay(false), valueDisplay(false), valueDisplay(false), valueDisplay(false)
    };
    private final JLabel statusLabel = new JLabel(" ");
    private Listener listener;

    public CalculatorView() {
        super("RPN Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(390, 650));
        buildInterface();
        installKeyboardShortcuts();
        pack();
        setLocationRelativeTo(null);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void showState(String accumulator, List<Double> stackData, String message, boolean error) {
        accumulatorDisplay.setText(accumulator == null || accumulator.isEmpty() ? "0" : accumulator);
        for (int slot = 0; slot < stackDisplays.length; slot++) {
            int stackIndex = stackData.size() - 1 - slot;
            String value = stackIndex >= 0 ? formatNumber(stackData.get(stackIndex)) : "";
            stackDisplays[slot].setText(value);
        }
        statusLabel.setText(message == null || message.isEmpty() ? " " : message);
        statusLabel.setForeground(error ? new Color(170, 35, 35) : new Color(75, 75, 75));
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(9, 9));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));

        JPanel registers = new JPanel(new GridLayout(5, 1, 5, 5));
        registers.add(registerRow("ACCUMULATOR", accumulatorDisplay));
        registers.add(registerRow("STACK 1 (TOP)", stackDisplays[0]));
        registers.add(registerRow("STACK 2", stackDisplays[1]));
        registers.add(registerRow("STACK 3", stackDisplays[2]));
        registers.add(registerRow("STACK 4", stackDisplays[3]));

        JPanel keypad = new JPanel(new GridLayout(4, 4, 7, 7));
        addDigitButton(keypad, "7");
        addDigitButton(keypad, "8");
        addDigitButton(keypad, "9");
        keypad.add(commandButton("÷", "Divide", Command.DIVIDE));

        addDigitButton(keypad, "4");
        addDigitButton(keypad, "5");
        addDigitButton(keypad, "6");
        keypad.add(commandButton("×", "Multiply", Command.MULTIPLY));

        addDigitButton(keypad, "1");
        addDigitButton(keypad, "2");
        addDigitButton(keypad, "3");
        keypad.add(commandButton("−", "Subtract", Command.SUBTRACT));

        keypad.add(commandButton("±", "Change the sign", Command.OPPOSITE));
        addDigitButton(keypad, "0");
        JButton decimalButton = new JButton(",");
        styleButton(decimalButton);
        decimalButton.setToolTipText("Decimal separator");
        decimalButton.addActionListener(event -> fireDecimalSeparator());
        keypad.add(decimalButton);
        keypad.add(commandButton("+", "Add", Command.ADD));

        JPanel stackControls = new JPanel(new GridLayout(2, 4, 7, 7));
        stackControls.add(commandButton("ENTER", "Push the accumulator onto the stack", Command.PUSH));
        stackControls.add(commandButton("POP", "Move the top stack value to the accumulator", Command.POP));
        stackControls.add(commandButton("DROP", "Remove the top stack value", Command.DROP));
        stackControls.add(commandButton("SWAP", "Swap the top two stack values", Command.SWAP));
        stackControls.add(commandButton("DROP ALL", "Clear the stack", Command.DROP_ALL));
        stackControls.add(commandButton("AC", "Clear the accumulator", Command.CLEAR_ACCUMULATOR));
        stackControls.add(commandButton("⌫", "Delete the last entered digit", Command.BACKSPACE));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 2, 0, 2));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JPanel bottom = new JPanel(new BorderLayout(7, 7));
        bottom.add(stackControls, BorderLayout.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);

        JPanel calculator = new JPanel(new BorderLayout(7, 7));
        calculator.add(keypad, BorderLayout.CENTER);
        calculator.add(bottom, BorderLayout.SOUTH);

        root.add(registers, BorderLayout.NORTH);
        root.add(calculator, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel registerRow(String labelText, JTextField display) {
        JPanel row = new JPanel(new BorderLayout(8, 3));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        label.setPreferredSize(new Dimension(118, 32));
        row.add(label, BorderLayout.WEST);
        row.add(display, BorderLayout.CENTER);
        return row;
    }

    private JTextField valueDisplay(boolean emphasized) {
        JTextField display = new JTextField("0");
        display.setEditable(false);
        display.setFocusable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font(Font.MONOSPACED, emphasized ? Font.BOLD : Font.PLAIN, emphasized ? 26 : 20));
        display.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(emphasized ? new Color(150, 165, 185) : new Color(190, 195, 202)),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)));
        return display;
    }

    private JButton commandButton(String text, String tooltip, Command command) {
        JButton button = new JButton(text);
        styleButton(button);
        button.setToolTipText(tooltip);
        button.addActionListener(event -> fireCommand(command));
        return button;
    }

    private void addDigitButton(JPanel keypad, String digit) {
        JButton button = new JButton(digit);
        styleButton(button);
        button.addActionListener(event -> fireDigit(digit));
        keypad.add(button);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        button.setFocusable(false);
    }

    private void installKeyboardShortcuts() {
        for (char digit = '0'; digit <= '9'; digit++) {
            final String value = String.valueOf(digit);
            bindKey(KeyStroke.getKeyStroke(digit), "digit-" + digit, () -> fireDigit(value));
        }
        bindKey(KeyStroke.getKeyStroke(','), "decimal-comma", this::fireDecimalSeparator);
        bindKey(KeyStroke.getKeyStroke('.'), "decimal-point", this::fireDecimalSeparator);
        bindKey(KeyStroke.getKeyStroke('+'), "add", () -> fireCommand(Command.ADD));
        bindKey(KeyStroke.getKeyStroke('-'), "subtract", () -> fireCommand(Command.SUBTRACT));
        bindKey(KeyStroke.getKeyStroke('*'), "multiply", () -> fireCommand(Command.MULTIPLY));
        bindKey(KeyStroke.getKeyStroke('/'), "divide", () -> fireCommand(Command.DIVIDE));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "push", () -> fireCommand(Command.PUSH));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace", () -> fireCommand(Command.BACKSPACE));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "clear-accumulator", () -> fireCommand(Command.CLEAR_ACCUMULATOR));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "drop-all", () -> fireCommand(Command.DROP_ALL));
    }

    private void bindKey(KeyStroke key, String name, Runnable action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getRootPane().getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void fireDigit(String digit) {
        if (listener != null) {
            listener.onDigit(digit);
        }
    }

    private void fireDecimalSeparator() {
        if (listener != null) {
            listener.onDecimalSeparator();
        }
    }

    private void fireCommand(Command command) {
        if (listener != null) {
            listener.onCommand(command);
        }
    }

    private String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
