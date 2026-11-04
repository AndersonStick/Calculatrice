# RPN Calculator

A Java Swing calculator built with a model-view-controller structure. It uses reverse Polish notation (RPN): enter values first, then choose an operation.

## Run the application

Open the project in Visual Studio Code and run `src/App.java`, or compile and run it from PowerShell:

```powershell
javac -d bin -cp src (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp bin App
```

## Use the calculator

For example, to calculate `3 - 1`:

1. Enter `3` and press **ENTER**.
2. Enter `1`.
3. Press **−**. The accumulator displays `2`.

The display contains the accumulator and the four newest values on the stack. **STACK 1 (TOP)** is the next stack value used by an operation. Older values remain in the model even when they are outside the four visible slots.

The stack controls are **ENTER** (push), **POP**, **DROP**, **SWAP**, and **DROP ALL**. **AC** clears the accumulator; **C** clears both the accumulator and the stack. Keyboard shortcuts are available for digits, decimal separator, arithmetic, **ENTER**, backspace, **AC**, and **C**.

## Project structure

- `src/model`: calculator state and RPN operations
- `src/controller`: input handling and model-to-view updates
- `src/view`: Swing interface
- `src/App.java`: application entry point

Compiled `.class` files are generated in `bin` and are not source files.
