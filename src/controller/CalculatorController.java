package controller;

import model.ExpressionEvaluator;
import view.CalculatorView;

import java.math.BigDecimal;

/** Maintains the expression entered in the view and delegates evaluation to the model. */
public final class CalculatorController implements CalculatorView.Listener {
    private final CalculatorView view;
    private final ExpressionEvaluator evaluator;
    private final StringBuilder expression = new StringBuilder();

    private double lastResult;
    private String resultText = "0";
    private String message;
    private boolean error;
    private boolean justEvaluated;

    public CalculatorController(CalculatorView view) {
        this(view, null);
    }

    public CalculatorController(CalculatorView view, ExpressionEvaluator evaluator) {
        this.view = view;
        this.evaluator = evaluator;
        this.message = evaluator == null
                ? "Interface ready; the model evaluator is not connected yet."
                : "Enter an expression and press =.";
        view.setListener(this);
        refreshView();
    }

    @Override
    public void onDigit(String digit) {
        beginEditingAfterResult(true);
        if (expression.length() >= 120) {
            showError("The expression is too long.");
            return;
        }
        if (expression.length() > 0 && expression.charAt(expression.length() - 1) == ')') {
            expression.append('*');
        }
        expression.append(digit);
        clearMessage();
        refreshView();
    }

    @Override
    public void onDecimalSeparator() {
        beginEditingAfterResult(true);
        if (hasDecimalInCurrentNumber()) {
            return;
        }
        if (expression.length() == 0 || isOperator(lastChar()) || lastChar() == '(' || lastChar() == ')') {
            if (expression.length() > 0 && lastChar() == ')') {
                expression.append('*');
            }
            expression.append("0.");
        } else {
            expression.append('.');
        }
        clearMessage();
        refreshView();
    }

    @Override
    public void onCommand(CalculatorView.Command command) {
        switch (command) {
            case OPEN_PARENTHESIS:
                openParenthesis();
                break;
            case CLOSE_PARENTHESIS:
                closeParenthesis();
                break;
            case ADD:
                appendOperator('+');
                break;
            case SUBTRACT:
                appendOperator('-');
                break;
            case MULTIPLY:
                appendOperator('*');
                break;
            case DIVIDE:
                appendOperator('/');
                break;
            case BACKSPACE:
                backspace();
                break;
            case CLEAR_ENTRY:
                clearEntry();
                break;
            case CLEAR_ALL:
                clearAll();
                break;
            case EQUALS:
                evaluate();
                break;
            default:
                showError("Unknown action.");
        }
    }

    private void openParenthesis() {
        beginEditingAfterResult(true);
        if (expression.length() > 0) {
            char previous = lastChar();
            if (Character.isDigit(previous) || previous == ')' || previous == '.') {
                expression.append('*');
            }
        }
        expression.append('(');
        clearMessage();
        refreshView();
    }

    private void closeParenthesis() {
        if (justEvaluated) {
            return;
        }
        if (unmatchedOpeningParentheses() == 0 || expression.length() == 0) {
            showError("There is no open parenthesis to close.");
            return;
        }
        char previous = lastChar();
        if (Character.isDigit(previous) || previous == ')') {
            expression.append(')');
            clearMessage();
            refreshView();
        } else {
            showError("Complete the expression before closing the parenthesis.");
        }
    }

    private void appendOperator(char operator) {
        beginEditingAfterResult(false);
        if (expression.length() == 0) {
            if (operator == '-') {
                expression.append(operator);
                clearMessage();
                refreshView();
            } else {
                showError("Enter a number or open a parenthesis first.");
            }
            return;
        }

        char previous = lastChar();
        if (previous == '(') {
            if (operator == '-') {
                expression.append(operator);
                clearMessage();
                refreshView();
            } else {
                showError("After '(', enter a number or a minus sign.");
            }
            return;
        }
        if (isOperator(previous)) {
            if (operator == '-' && previous != '-') {
                expression.append(operator);
            } else {
                expression.setCharAt(expression.length() - 1, operator);
            }
        } else {
            expression.append(operator);
        }
        clearMessage();
        refreshView();
    }

    private void backspace() {
        if (justEvaluated) {
            justEvaluated = false;
            resultText = "0";
        }
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
        }
        clearMessage();
        refreshView();
    }

    private void clearEntry() {
        if (justEvaluated) {
            clearAll();
            return;
        }
        if (expression.length() == 0) {
            return;
        }

        int end = expression.length();
        char last = lastChar();
        if (Character.isDigit(last) || last == '.') {
            int start = end - 1;
            while (start > 0 && (Character.isDigit(expression.charAt(start - 1)) || expression.charAt(start - 1) == '.')) {
                start--;
            }
            if (start > 0 && expression.charAt(start - 1) == '-' && isUnaryMinus(start - 1)) {
                start--;
            }
            expression.delete(start, end);
        } else {
            expression.deleteCharAt(end - 1);
        }
        clearMessage();
        refreshView();
    }

    private void clearAll() {
        expression.setLength(0);
        resultText = "0";
        lastResult = 0;
        justEvaluated = false;
        message = evaluator == null
                ? "Interface ready; the model evaluator is not connected yet."
                : "Enter an expression and press =.";
        error = false;
        refreshView();
    }

    private void evaluate() {
        if (expression.length() == 0) {
            showError("Enter an expression to calculate.");
            return;
        }
        if (evaluator == null) {
            showError("The model is not connected to evaluate(String expression) yet.");
            return;
        }
        try {
            double value = evaluator.evaluate(expression.toString());
            if (!Double.isFinite(value)) {
                showError("The result is not a finite number.");
                return;
            }
            lastResult = value;
            resultText = formatResult(value);
            justEvaluated = true;
            message = "Calculation complete.";
            error = false;
            refreshView();
        } catch (RuntimeException exception) {
            showError("The expression could not be evaluated.");
        }
    }

    private void beginEditingAfterResult(boolean startFresh) {
        if (!justEvaluated) {
            return;
        }
        if (startFresh) {
            expression.setLength(0);
            resultText = "0";
        } else {
            expression.setLength(0);
            expression.append(canonicalNumber(lastResult));
        }
        justEvaluated = false;
    }

    private boolean hasDecimalInCurrentNumber() {
        for (int i = expression.length() - 1; i >= 0; i--) {
            char character = expression.charAt(i);
            if (character == '.') {
                return true;
            }
            if (!Character.isDigit(character)) {
                break;
            }
        }
        return false;
    }

    private int unmatchedOpeningParentheses() {
        int balance = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') {
                balance++;
            } else if (expression.charAt(i) == ')') {
                balance--;
            }
        }
        return balance;
    }

    private boolean isUnaryMinus(int index) {
        if (index == 0) {
            return true;
        }
        char previous = expression.charAt(index - 1);
        return isOperator(previous) || previous == '(';
    }

    private boolean isOperator(char character) {
        return character == '+' || character == '-' || character == '*' || character == '/';
    }

    private char lastChar() {
        return expression.charAt(expression.length() - 1);
    }

    private String formatResult(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private String canonicalNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private String displayExpression() {
        return expression.toString()
                .replace('.', ',')
                .replace('*', '×')
                .replace('/', '÷')
                .replace('-', '−');
    }

    private void clearMessage() {
        message = evaluator == null
                ? "Input ready; waiting to connect the evaluator."
                : "Enter an expression and press =.";
        error = false;
    }

    private void showError(String text) {
        message = text;
        error = true;
        refreshView();
    }

    private void refreshView() {
        view.showState(displayExpression(), resultText, message, error);
    }
}
