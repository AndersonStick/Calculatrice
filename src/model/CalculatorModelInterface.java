package model;

import java.util.Stack;

public interface CalculatorModelInterface {
    void setAccumulator(double value);

    double getAccumulator();

    Stack<Double> getStack();

    void push();

    void pop();

    void clear();

    void drop();

    void dropAll();

    void swap();

    double add(double valA,double valB);

    double subtraction(double valA,double valB);
    
    double multiplication(double valA,double valB);
    
    double division(double valA,double valB);
}