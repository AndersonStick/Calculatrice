package model;

import controller.CalculatorControllerInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.DoubleBinaryOperator;

/** RPN calculator model with an accumulator and an operand stack. */
public class CalculatorModel implements CalculatorModelInterface {
    private double accumulator;
    private boolean accumulatorFilled;
    private final Stack<Double> memory = new Stack<>();
    private CalculatorControllerInterface controller;

    public void setController(CalculatorControllerInterface controller) {
        this.controller = controller;
        notifyChange();
    }

    @Override
    public void setAccumulator(double value) {
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
        return new ArrayList<>(memory);
    }

    @Override
    public void push() {
        memory.push(accumulator);
        clear();
    }

    @Override
    public void pop() {
        requireOperands(1);
        accumulator = memory.pop();
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
        memory.pop();
        notifyChange();
    }

    @Override
    public void dropAll() {
        memory.clear();
        notifyChange();
    }

    @Override
    public void swap() {
        requireOperands(2);
        double top = memory.pop();
        double second = memory.pop();
        memory.push(top);
        memory.push(second);
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
                throw new ArithmeticException("Division by zero");
            }
            return left / right;
        });
    }

    @Override
    public void opposite() {
        accumulator = -accumulator;
        notifyChange();
    }

    /** Uses the accumulator and one stacked value, or pops two stacked values. */
    private void applyBinary(DoubleBinaryOperator operation) {
        boolean useAccumulator = accumulatorFilled;
        int operandCount = useAccumulator ? 1 : 2;
        requireOperands(operandCount);

        int topIndex = memory.size() - 1;
        double right = useAccumulator ? accumulator : memory.get(topIndex);
        double left = useAccumulator ? memory.get(topIndex) : memory.get(topIndex - 1);
        double result = operation.applyAsDouble(left, right);

        for (int i = 0; i < operandCount; i++) {
            memory.pop();
        }
        accumulator = result;
        accumulatorFilled = true;
        notifyChange();
    }

    private void requireOperands(int count) {
        if (memory.size() < count) {
            throw new IllegalStateException("Not enough operands on the stack");
        }
    }

    private void notifyChange() {
        if (controller != null) {
            controller.change(String.valueOf(accumulator), getStackData());
        }
    }
}
