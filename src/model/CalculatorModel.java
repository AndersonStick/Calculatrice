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

    public void push() {
        stack.push(accumulator);
    }

    public void pop(){
        accumulator = stack.pop();
    }

    public Stack<Double> getStack() {
        return stack;
    }
}
