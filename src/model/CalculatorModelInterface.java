package model;

import java.util.List;

import controller.CalculatorControllerInterface;

public interface CalculatorModelInterface {
    /** Registers the controller that receives state-change callbacks. */
    void setController(CalculatorControllerInterface controller);

    void setAccumulator(double value);

    double getAccumulator();

    /** Returns a bottom-to-top snapshot of the stack. */
    List<Double> getStackData();

    void push();

    void pop();

    void clear();

    void drop();

    void dropAll();

    void swap();

    void add();

    void subtract();

    void multiply();

    void divide();

    void opposite();
}
