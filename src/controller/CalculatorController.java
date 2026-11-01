package controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import model.CalculatorModelInterface;
import view.CalculatorView;

/** Connects calculator input events to the model and forwards model updates to the view. */
public final class CalculatorController implements CalculatorView.Listener, CalculatorControllerInterface {
    private static final int MAX_INPUT_LENGTH = 18;

    private final CalculatorView view;
    private final CalculatorModelInterface model;

    private String inputBuffer = "0";
    private String message = "RPN calculator ready.";
    private boolean entryActive = true;
    private boolean error;

    public CalculatorController(CalculatorView view, CalculatorModelInterface model) {
        this.view = Objects.requireNonNull(view, "view");
        this.model = Objects.requireNonNull(model, "model");
        view.setListener(this);
        model.setController(this);
    }

    @Override
    public void onDigit(String digit) {
        if (!entryActive) {
            inputBuffer = "0";
            entryActive = true;
        }
        if (inputBuffer.length() >= MAX_INPUT_LENGTH) {
            showError("The entered number is too long.");
            return;
        }
        if (inputBuffer.equals("0")) {
            inputBuffer = digit;
        } else {
            inputBuffer += digit;
        }
        clearMessage();
        model.setAccumulator(parseInput());
    }

    @Override
    public void onDecimalSeparator() {
        if (!entryActive) {
            inputBuffer = "0";
            entryActive = true;
        }
        boolean separatorAdded = !inputBuffer.contains(",");
        if (separatorAdded) {
            inputBuffer += ",";
        }
        clearMessage();
        if (separatorAdded) {
            model.setAccumulator(parseInput());
        } else {
            refreshView();
        }
    }

    @Override
    public void onCommand(CalculatorView.Command command) {
        switch (command) {
            case PUSH:
                push();
                break;
            case POP:
                execute(model::pop, "Stack value moved to the accumulator.");
                break;
            case DROP:
                execute(model::drop, "Top stack value removed.");
                break;
            case DROP_ALL:
                execute(model::dropAll, "Stack cleared.");
                break;
            case SWAP:
                execute(model::swap, "Top stack values swapped.");
                break;
            case CLEAR_ACCUMULATOR:
                clearAccumulator();
                break;
            case CLEAR_ALL:
                clearAll();
                break;
            case BACKSPACE:
                backspace();
                break;
            case OPPOSITE:
                execute(model::opposite, "Sign changed.");
                break;
            case ADD:
                execute(model::add, "Addition complete.");
                break;
            case SUBTRACT:
                execute(model::subtract, "Subtraction complete.");
                break;
            case MULTIPLY:
                execute(model::multiply, "Multiplication complete.");
                break;
            case DIVIDE:
                execute(model::divide, "Division complete.");
                break;
            default:
                showError("Unknown action.");
        }
    }

    @Override
    public void change(double accumulator, List<Double> stackData) {
        String visibleAccumulator = entryActive
                ? inputBuffer
                : formatNumber(accumulator);
        view.showState(visibleAccumulator, stackData, message, error);
    }

    private void push() {
        inputBuffer = "0";
        entryActive = true;
        execute(model::push, "Value pushed. Enter the next number.");
    }

    private void clearAccumulator() {
        inputBuffer = "0";
        entryActive = true;
        message = "Accumulator cleared.";
        error = false;
        model.clear();
    }

    private void clearAll() {
        inputBuffer = "0";
        entryActive = true;
        message = "Calculator cleared.";
        error = false;
        model.clear();
        model.dropAll();
    }

    private void backspace() {
        if (!entryActive) {
            clearAccumulator();
            return;
        }
        if (inputBuffer.length() > 1) {
            inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
        } else {
            inputBuffer = "0";
        }
        clearMessage();
        model.setAccumulator(parseInput());
    }

    private void execute(Runnable operation, String successMessage) {
        String previousInput = inputBuffer;
        boolean previousEntryActive = entryActive;
        entryActive = false;
        try {
            operation.run();
            inputBuffer = formatNumber(model.getAccumulator());
            message = successMessage;
            error = false;
        } catch (IllegalStateException | ArithmeticException exception) {
            inputBuffer = previousInput;
            entryActive = previousEntryActive;
            message = exception.getMessage() == null ? "The operation could not be completed." : exception.getMessage();
            error = true;
        }
        refreshView();
    }

    private double parseInput() {
        String normalized = inputBuffer.replace(',', '.');
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return Double.parseDouble(normalized);
    }

    private void clearMessage() {
        message = "RPN calculator ready.";
        error = false;
    }

    private void showError(String text) {
        message = text;
        error = true;
        refreshView();
    }

    private String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private void refreshView() {
        String visibleAccumulator = entryActive ? inputBuffer : formatNumber(model.getAccumulator());
        view.showState(visibleAccumulator, model.getStackData(), message, error);
    }
}
