package model;

import java.util.Arrays;
import java.util.List;

import controller.CalculatorControllerInterface;

/** Executable checks for CalculatorModel behavior. */
public final class CalculatorModelTest {
    private static final double EPSILON = 0.0000000001;

    private CalculatorModelTest() {
    }

    public static void main(String[] args) {
        testPushPopAndClear();
        testArithmeticWithTwoStackOperands();
        testArithmeticWithAccumulatorOperand();
        testStackOperations();
        testErrorsPreserveState();
        testControllerNotification();
        System.out.println("CalculatorModelTest: all tests passed.");
    }

    private static void testPushPopAndClear() {
        CalculatorModel model = new CalculatorModel();
        model.setAccumulator(42.5);
        model.push();

        assertClose(0, model.getAccumulator(), "push resets the accumulator");
        assertStack(model, 42.5);

        model.pop();
        assertClose(42.5, model.getAccumulator(), "pop restores the top value");
        assertStack(model);

        model.clear();
        assertClose(0, model.getAccumulator(), "clear resets the accumulator");
        assertStack(model);
    }

    private static void testArithmeticWithTwoStackOperands() {
        CalculatorModel addition = twoStackOperands(3, 2);
        addition.add();
        assertClose(5, addition.getAccumulator(), "addition");
        assertStack(addition);

        CalculatorModel subtraction = twoStackOperands(3, 2);
        subtraction.subtract();
        assertClose(1, subtraction.getAccumulator(), "subtraction order");

        CalculatorModel multiplication = twoStackOperands(3, 2);
        multiplication.multiply();
        assertClose(6, multiplication.getAccumulator(), "multiplication");

        CalculatorModel division = twoStackOperands(6, 2);
        division.divide();
        assertClose(3, division.getAccumulator(), "division order");

        CalculatorModel decimal = twoStackOperands(1.5, 2.25);
        decimal.add();
        assertClose(3.75, decimal.getAccumulator(), "decimal addition");
    }

    private static void testArithmeticWithAccumulatorOperand() {
        CalculatorModel model = new CalculatorModel();
        model.setAccumulator(3);
        model.push();
        model.setAccumulator(1);

        model.subtract();

        assertClose(2, model.getAccumulator(), "stack value minus accumulator value");
        assertStack(model);
    }

    private static void testStackOperations() {
        CalculatorModel model = twoStackOperands(10, 20);
        model.swap();
        assertStack(model, 20, 10);

        model.drop();
        assertStack(model, 20);

        model.dropAll();
        assertStack(model);

        assertThrows(IllegalStateException.class, model::pop, "pop on an empty stack");
        assertThrows(IllegalStateException.class, model::drop, "drop on an empty stack");
        assertThrows(IllegalStateException.class, model::swap, "swap with fewer than two values");
    }

    private static void testErrorsPreserveState() {
        CalculatorModel missingOperand = new CalculatorModel();
        missingOperand.setAccumulator(7);
        missingOperand.push();
        assertThrows(IllegalStateException.class, missingOperand::add, "missing operand");
        assertClose(0, missingOperand.getAccumulator(), "missing operand keeps accumulator");
        assertStack(missingOperand, 7);

        CalculatorModel divisionByZero = twoStackOperands(10, 0);
        assertThrows(ArithmeticException.class, divisionByZero::divide, "division by zero");
        assertClose(0, divisionByZero.getAccumulator(), "division by zero keeps accumulator");
        assertStack(divisionByZero, 10, 0);
    }

    private static void testControllerNotification() {
        CalculatorModel model = new CalculatorModel();
        RecordingController controller = new RecordingController();
        model.setController(controller);
        assertEquals(1, controller.notifications, "setting the controller notifies it");

        model.setAccumulator(8);
        assertEquals(2, controller.notifications, "model changes notify the controller");
        assertClose(8, controller.accumulator, "callback accumulator value");
        assertStack(controller.stackData);
    }

    private static CalculatorModel twoStackOperands(double first, double second) {
        CalculatorModel model = new CalculatorModel();
        model.setAccumulator(first);
        model.push();
        model.setAccumulator(second);
        model.push();
        return model;
    }

    private static void assertStack(CalculatorModel model, double... expected) {
        assertStack(model.getStackData(), expected);
    }

    private static void assertStack(List<Double> actual, double... expected) {
        if (actual.size() != expected.length) {
            throw new AssertionError("Expected stack " + Arrays.toString(expected) + " but was " + actual);
        }
        for (int index = 0; index < expected.length; index++) {
            if (Math.abs(actual.get(index) - expected[index]) > EPSILON) {
                throw new AssertionError("Expected stack " + Arrays.toString(expected) + " but was " + actual);
            }
        }
    }

    private static void assertClose(double expected, double actual, String description) {
        if (Math.abs(expected - actual) > EPSILON) {
            throw new AssertionError(description + ": expected " + expected + " but was " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String description) {
        if (expected != actual) {
            throw new AssertionError(description + ": expected " + expected + " but was " + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType,
            ThrowingRunnable action, String description) {
        try {
            action.run();
        } catch (Throwable exception) {
            if (expectedType.isInstance(exception)) {
                return;
            }
            throw new AssertionError(description + ": expected " + expectedType.getSimpleName()
                    + " but was " + exception.getClass().getSimpleName(), exception);
        }
        throw new AssertionError(description + ": expected " + expectedType.getSimpleName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }

    private static final class RecordingController implements CalculatorControllerInterface {
        private int notifications;
        private double accumulator;
        private List<Double> stackData;

        @Override
        public void change(double accumulator, List<Double> stackData) {
            notifications++;
            this.accumulator = accumulator;
            this.stackData = stackData;
        }
    }
}
