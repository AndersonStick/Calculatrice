package model;

import java.util.List;

public interface CalculatorModelInterface {
    void setAccumulator(double value);

    double getAccumulator();

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
