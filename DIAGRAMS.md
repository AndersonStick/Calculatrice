# RPN Calculator Diagrams

These Mermaid diagrams document the architecture and behavior of the RPN calculator.

They can be previewed in Visual Studio Code with a Mermaid-compatible Markdown extension or exported as SVG, PNG, or PDF using the [Mermaid Live Editor](https://mermaid.live/).

## 1. MVC Architecture

```mermaid
flowchart LR
    User[User] --> View[CalculatorView]
    View --> Controller[CalculatorController]
    Controller --> Model[CalculatorModel]
    Model --> Controller
    Controller --> View

    Model -. implements .-> ModelInterface[CalculatorModelInterface]
    Controller -. implements .-> ControllerInterface[CalculatorControllerInterface]
    Controller -. implements .-> Listener[CalculatorView.Listener]
```

## 2. Sequence Diagram: `3 ENTER 1 -`

```mermaid
sequenceDiagram
    actor User
    participant View as CalculatorView
    participant Controller as CalculatorController
    participant Model as CalculatorModel

    User->>View: Presses 3
    View->>Controller: onDigit("3")
    Controller->>Controller: Updates inputBuffer
    Controller->>Model: setAccumulator(3)
    Model->>Controller: change(3, empty stack)
    Controller->>View: showState(3, empty stack)

    User->>View: Presses ENTER
    View->>Controller: onCommand(PUSH)
    Controller->>Model: push()
    Model->>Model: Stores 3 in the stack
    Model->>Model: Sets accumulator to 0
    Model->>Model: Sets accumulatorFilled to false
    Model->>Controller: change(0, stack=[3])
    Controller->>View: showState(0, stack=[3])

    User->>View: Presses 1
    View->>Controller: onDigit("1")
    Controller->>Model: setAccumulator(1)
    Model->>Model: Sets accumulatorFilled to true
    Model->>Controller: change(1, stack=[3])
    Controller->>View: showState(1, stack=[3])

    User->>View: Presses subtraction
    View->>Controller: onCommand(SUBTRACT)
    Controller->>Model: subtract()
    Model->>Model: Calls applyBinary()
    Model->>Model: Reads left=3 from the stack
    Model->>Model: Reads right=1 from the accumulator
    Model->>Model: Calculates 3 - 1
    Model->>Model: Removes 3 from the stack
    Model->>Model: Stores 2 in the accumulator
    Model->>Controller: change(2, empty stack)
    Controller->>View: showState(2, empty stack)
```

## 3. Class Diagram

```mermaid
classDiagram
    class App {
        +main(String[] args)
    }

    class CalculatorView {
        -JTextField accumulatorDisplay
        -JTextField[] stackDisplays
        -JLabel statusLabel
        +setListener(Listener listener)
        +showState(String accumulator, List~Double~ stackData, String message, boolean error)
    }

    class CalculatorController {
        -CalculatorView view
        -CalculatorModelInterface model
        -String inputBuffer
        -boolean entryActive
        -boolean error
        +onDigit(String digit)
        +onDecimalSeparator()
        +onCommand(Command command)
        +change(double accumulator, List~Double~ stackData)
    }

    class CalculatorModel {
        -Deque~Double~ stack
        -double accumulator
        -boolean accumulatorFilled
        +setAccumulator(double value)
        +getAccumulator() double
        +push()
        +pop()
        +drop()
        +dropAll()
        +swap()
        +add()
        +subtract()
        +multiply()
        +divide()
        +opposite()
    }

    class CalculatorModelInterface {
        <<interface>>
    }

    class CalculatorControllerInterface {
        <<interface>>
        +change(double accumulator, List~Double~ stackData)
    }

    class Listener {
        <<interface>>
        +onDigit(String digit)
        +onDecimalSeparator()
        +onCommand(Command command)
    }

    App --> CalculatorView : creates
    App --> CalculatorModel : creates
    App --> CalculatorController : connects
    CalculatorController --> CalculatorView : updates
    CalculatorController --> CalculatorModelInterface : commands
    CalculatorModel ..|> CalculatorModelInterface
    CalculatorController ..|> CalculatorControllerInterface
    CalculatorController ..|> Listener
    CalculatorModel --> CalculatorControllerInterface : notifies
```

## 4. Calculator State Diagram

```mermaid
stateDiagram-v2
    [*] --> Ready

    Ready --> EnteringNumber: Press digit
    EnteringNumber --> EnteringNumber: Press digit or decimal separator

    EnteringNumber --> StackUpdated: Press ENTER
    StackUpdated --> Ready: Accumulator reset

    EnteringNumber --> OperationCompleted: Press arithmetic operation
    StackUpdated --> OperationCompleted: Operation has enough operands

    OperationCompleted --> EnteringNumber: Press digit
    OperationCompleted --> Ready: Press ENTER
    OperationCompleted --> OperationCompleted: Press another operation

    Ready --> AccumulatorCleared: Press AC
    EnteringNumber --> AccumulatorCleared: Press AC
    OperationCompleted --> AccumulatorCleared: Press AC
    AccumulatorCleared --> Ready

    Ready --> CalculatorCleared: Press C
    EnteringNumber --> CalculatorCleared: Press C
    OperationCompleted --> CalculatorCleared: Press C
    CalculatorCleared --> Ready

    StackUpdated --> Error: Not enough operands
    EnteringNumber --> Error: Invalid or too-long input
    OperationCompleted --> Error: Division by zero
    Error --> Ready: User continues
```

## 5. Stack and Accumulator Flow

```mermaid
flowchart TD
    A["Initial state<br/>Accumulator: 0<br/>Stack: empty"]
    B["Enter 3<br/>Accumulator: 3<br/>Stack: empty"]
    C["Press ENTER<br/>Accumulator: 0<br/>Stack TOP: 3"]
    D["Enter 1<br/>Accumulator: 1<br/>Stack TOP: 3"]
    E["Press subtraction<br/>left = 3<br/>right = 1"]
    F["Result<br/>Accumulator: 2<br/>Stack: empty"]

    A --> B --> C --> D --> E --> F
```

## 6. Binary Operation Logic

```mermaid
flowchart TD
    Start[Binary operation requested] --> Filled{Is accumulatorFilled true?}

    Filled -- Yes --> OneOperand[Require one stack operand]
    OneOperand --> ReadStack[Read top stack value]
    ReadStack --> UseAccumulator[Use accumulator as right operand]

    Filled -- No --> TwoOperands[Require two stack operands]
    TwoOperands --> ReadTwo[Read top and second stack values]
    ReadTwo --> BothStack[Use both stack values as operands]

    UseAccumulator --> Calculate[Calculate operation]
    BothStack --> Calculate

    Calculate --> Valid{Is result finite?}
    Valid -- No --> Error[Report error without changing operands]
    Valid -- Yes --> Remove[Remove consumed operands]
    Remove --> Store[Store result in accumulator]
    Store --> Notify[Notify controller]
```
