package model;

import java.util.Stack;

public class CalculatorModel {
    private double accumulator;
    private Stack<Double> stack;

    public CalculatorModel() {
        accumulator = 0;
        stack = new Stack<>();
    }

    public void setAccumulator(double value) {
        accumulator = value;
    }

    public double getAccumulator() {
        return accumulator;
    }

    public Stack<Double> getStack() {
        return stack;
    }

    public void push() {
        stack.push(accumulator);
    }

    public void pop() {
        accumulator = stack.pop();
    }

    public void clear() {
        accumulator = 0;
    }

    public void drop() {
        stack.pop();
    }

    public void dropAll() {
        stack.clear();
    }

    public void swap() {
        double first = stack.pop();
        double second = stack.pop();

        stack.push(first);
        stack.push(second);
    }
}