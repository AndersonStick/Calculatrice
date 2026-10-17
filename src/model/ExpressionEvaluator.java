package model;

/**
 * Contract for the calculator logic to evaluate a conventional infix expression.
 * Expressions use '.', '+', '-', '*', '/', and parentheses.
 */
@FunctionalInterface
public interface ExpressionEvaluator {
    double evaluate(String expression);
}
