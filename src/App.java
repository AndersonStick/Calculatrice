import model.CalculatorModel;

public class App {
    public static void main(String[] args) throws Exception {
        CalculatorModel calc = new CalculatorModel();
        calc.push();

        calc.setAccumulator(5);
        calc.push();

        calc.setAccumulator(2);
        calc.push();



        System.out.println(calc.getAccumulator());
    }
}
