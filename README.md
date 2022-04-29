# RunExp
A lightweight math expressions solver for Java that compiles expressions directly into JVM bytecode minimizing runtime overhead aimed to precompiling expressions.

# Usage:

with x:
```java
Expression expression = RunExp.compile("sin(x) * 0.5 + (x * 0.1)");
expression.eval(1.25f); // same as sin(1.25f) * 0.5f + (1.25f * 0.1f)

// or

RunExpSolver solver = new RunExpSolver();
Expression expression = solver.compile("sin(x) * 0.5 + (x * 0.1)");
expression.eval(1.25f);
```


### Solve constant expression:

```java
float value = RunExp.eval("pi * 0.5");
```

if Expression wrapper needed (ConstantExpression used):
```java
ConstantExpression expression = RunExp.compileConstant("pi ^ 2");
```

### Setting up:
RunExp class uses RunExpSolver instance available as `RunExp.solver`, but it's preferred
to create new one.
```java
RunExpSolver solver = new RunExpSolver();
solver.addConstant("g", 9.8f);
...
float value = solver.eval(expressionString);
```
- Add custom constant: `solver.addConstant(name, value)`
- Add custom function (example): 
```java 
try {
  // adding Noise.noise2d(float, float) static method as function 'noise'
  solver.addFunction("noise", Noise.class, "noise2d");
  // if Noise.noise is overloaded
  solver.addFunction("noise", Noise.class, "noise2d", class.float, class.float);
} catch (NoSuchMethodException e){
  ...
}
// see RunExpSolver.addFunction docs for more info
``` 
- `solver.allowJVM` setting - allow compiling expressions directly into JVM bytecode (true by default)

# Features:
- unary operations: '-'
- binary operations: '+', '-', '*', '/' and '^' (exponentation)
- functions:
  - abs
  - sin, cos, tan
  - sqrt, exp, pow (same as '^' operator)
  - min(a, b), max(a, b)
  - round, floor, ceil
  - sign / signum
  - rand - random number in range `[0.0, 1.0]`
  - smoother (smoother step)
- constants:
  - pi (Math.PI)
  - pi2 (Math.PI * 2)
  - e (Math.E)
  - raddeg (180.0 / Math.PI) usage: degrees = radians * raddeg
  - degrad (Math.PI / 180.0) usage: radians = degreen * degrad
- custom constants
- custom functions (directly calling static methods as functions)
# Examples:
```java
try {
  int width = (int)RunExp.eval(widthField.getText());
  // ...
} catch (ExpCompileException e){
  invalidInputMessage(e.message);
}
```
