package controller;

import java.util.List;

/** Callback used by the model to notify the controller of a state change. */
public interface CalculatorControllerInterface {
    void change(String accumulator, List<Double> stackData);
}
