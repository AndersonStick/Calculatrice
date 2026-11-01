package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import controller.CalculatorControllerInterface;

/** RPN calculator model with an accumulator and an operand stack. */
public final class CalculatorModel implements CalculatorModelInterface {
    private final Deque<Double> stack = new ArrayDeque<>();

    private double accumulator;
    private boolean accumulatorFilled;
    private CalculatorControllerInterface controller;

    @Override
    public void setController(CalculatorControllerInterface controller) {
        this.controller = controller;
        notifyChange();
    }

    @Override
    public void setAccumulator(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("The accumulator must contain a finite number.");
        }
        accumulator = value;
        accumulatorFilled = true;
        notifyChange();
    }

    @Override
    public double getAccumulator() {
        return accumulator;
    }

    @Override
    public List<Double> getStackData() {
        return new ArrayList<>(stack);
    }

    @Override
    public void push() {
        stack.addLast(accumulator);
        accumulator = 0;
        accumulatorFilled = false;
        notifyChange();
    }

    @Override
    public void pop() {
        requireOperands(1);
        accumulator = stack.removeLast();
        accumulatorFilled = true;
        notifyChange();
    }

    @Override
    public void clear() {
        accumulator = 0;
        accumulatorFilled = false;
        notifyChange();
    }

    @Override
    public void drop() {
        requireOperands(1);
        stack.removeLast();
        notifyChange();
    }

    @Override
    public void dropAll() {
        stack.clear();
        notifyChange();
    }

    @Override
    public void swap() {
        requireOperands(2);
        double top = stack.removeLast();
        double belowTop = stack.removeLast();
        stack.addLast(top);
        stack.addLast(belowTop);
        notifyChange();
    }

    @Override
    public void add() {
        applyBinary((left, right) -> left + right);
    }

    @Override
    public void subtract() {
        applyBinary((left, right) -> left - right);
    }

    @Override
    public void multiply() {
        applyBinary((left, right) -> left * right);
    }

    @Override
    public void divide() {
        applyBinary((left, right) -> {
            if (right == 0) {
                throw new ArithmeticException("Division by zero.");
            }
            return left / right;
        });
    }

    @Override
    public void opposite() {
        accumulator = -accumulator;
        notifyChange();
    }

    /** Applies an operation to the accumulator and stack without losing operands on failure. */
    private void applyBinary(DoubleBinaryOperator operation) {
        boolean useAccumulator = accumulatorFilled;
        int operandCount = useAccumulator ? 1 : 2;
        requireOperands(operandCount);

        Iterator<Double> topDown = stack.descendingIterator();
        double top = topDown.next();
        double right = useAccumulator ? accumulator : top;
        double left = useAccumulator ? top : topDown.next();
        double result = operation.applyAsDouble(left, right);
        if (!Double.isFinite(result)) {
            throw new ArithmeticException("The result is not a finite number.");
        }

        for (int i = 0; i < operandCount; i++) {
            stack.removeLast();
        }
        accumulator = result;
        accumulatorFilled = true;
        notifyChange();
    }

    private void requireOperands(int count) {
        if (stack.size() < count) {
            throw new IllegalStateException("Not enough operands on the stack.");
        }
    }

    private void notifyChange() {
        if (controller != null) {
            controller.change(accumulator, getStackData());
        }
    }
}
