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

/** Standard infix calculator view. It forwards input events to the controller. */
public final class CalculatorView extends JFrame {
    public enum Command {
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        BACKSPACE,
        CLEAR_ENTRY,
        CLEAR_ALL,
        EQUALS
    }

    public interface Listener {
        void onDigit(String digit);

        void onDecimalSeparator();

        void onCommand(Command command);
    }

    private final JTextField expressionDisplay = new JTextField("0");
    private final JTextField resultDisplay = new JTextField("0");
    private final JLabel statusLabel = new JLabel(" ");
    private Listener listener;

    public CalculatorView() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(360, 550));
        buildInterface();
        installKeyboardShortcuts();
        pack();
        setLocationRelativeTo(null);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void showState(String expression, String result, String message, boolean error) {
        expressionDisplay.setText(expression == null || expression.isEmpty() ? "0" : expression);
        resultDisplay.setText(result == null || result.isEmpty() ? "0" : result);
        statusLabel.setText(message == null || message.isEmpty() ? " " : message);
        statusLabel.setForeground(error ? new Color(170, 35, 35) : new Color(75, 75, 75));
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(9, 9));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));

        JPanel displayPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        JLabel expressionLabel = new JLabel("EXPRESSION");
        expressionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        expressionDisplay.setEditable(false);
        expressionDisplay.setFocusable(false);
        expressionDisplay.setHorizontalAlignment(JTextField.RIGHT);
        expressionDisplay.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        expressionDisplay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 195, 202)),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));

        JLabel resultLabel = new JLabel("RESULT");
        resultLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        resultDisplay.setEditable(false);
        resultDisplay.setFocusable(false);
        resultDisplay.setHorizontalAlignment(JTextField.RIGHT);
        resultDisplay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
        resultDisplay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 165, 185)),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));

        JPanel expressions = new JPanel(new BorderLayout(3, 3));
        expressions.add(expressionLabel, BorderLayout.NORTH);
        expressions.add(expressionDisplay, BorderLayout.CENTER);
        JPanel result = new JPanel(new BorderLayout(3, 3));
        result.add(resultLabel, BorderLayout.NORTH);
        result.add(resultDisplay, BorderLayout.CENTER);
        displayPanel.add(expressions);
        displayPanel.add(result);

        JPanel keypad = new JPanel(new GridLayout(5, 4, 7, 7));
        keypad.add(commandButton("(", "Open parenthesis", Command.OPEN_PARENTHESIS));
        keypad.add(commandButton(")", "Close parenthesis", Command.CLOSE_PARENTHESIS));
        keypad.add(commandButton("⌫", "Delete the last character", Command.BACKSPACE));
        keypad.add(commandButton("C", "Clear the expression and result", Command.CLEAR_ALL));

        addDigitButton(keypad, "7");
        addDigitButton(keypad, "8");
        addDigitButton(keypad, "9");
        keypad.add(commandButton("÷", "Division", Command.DIVIDE));

        addDigitButton(keypad, "4");
        addDigitButton(keypad, "5");
        addDigitButton(keypad, "6");
        keypad.add(commandButton("×", "Multiplication", Command.MULTIPLY));

        addDigitButton(keypad, "1");
        addDigitButton(keypad, "2");
        addDigitButton(keypad, "3");
        keypad.add(commandButton("−", "Subtraction", Command.SUBTRACT));

        keypad.add(commandButton("CE", "Clear the current number", Command.CLEAR_ENTRY));
        addDigitButton(keypad, "0");
        JButton decimalButton = new JButton(",");
        styleButton(decimalButton);
        decimalButton.setToolTipText("Decimal separator");
        decimalButton.addActionListener(event -> fireDecimalSeparator());
        keypad.add(decimalButton);
        keypad.add(commandButton("+", "Addition", Command.ADD));

        JButton equalsButton = commandButton("=", "Evaluate the expression", Command.EQUALS);
        equalsButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
        equalsButton.setBackground(new Color(47, 105, 170));
        equalsButton.setForeground(Color.WHITE);
        equalsButton.setOpaque(true);
        equalsButton.setBorderPainted(false);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JPanel bottom = new JPanel(new BorderLayout(7, 7));
        bottom.add(equalsButton, BorderLayout.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(7, 7));
        center.add(keypad, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        root.add(displayPanel, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
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
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
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
        bindKey(KeyStroke.getKeyStroke('('), "open-parenthesis", () -> fireCommand(Command.OPEN_PARENTHESIS));
        bindKey(KeyStroke.getKeyStroke(')'), "close-parenthesis", () -> fireCommand(Command.CLOSE_PARENTHESIS));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "equals", () -> fireCommand(Command.EQUALS));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace", () -> fireCommand(Command.BACKSPACE));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "clear-entry", () -> fireCommand(Command.CLEAR_ENTRY));
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear-all", () -> fireCommand(Command.CLEAR_ALL));
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
}
