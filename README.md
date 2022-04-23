# RunExp
A lightweight math expressions solver for Java that compiles expressions directly into JVM bytecode to minimize runtime usage time overhead.

Also planned to implement runexp-bytecode compilation to use when JVM-bytecode build is not available.

# Usage

### Regular way:

with x:
```java
Expression expression = RunExp.compile("sin(x) * 0.5 + (x * 0.1)")
expression.eval(1.25f) // same as sin(1.25f) * 0.5f + (1.25f * 0.1f)
```

constant (if Expression object needed):
```java
Expression expression = RunExp.compile("pi ^ 2", true)
```

### Solve constant expression with no Expression creation:

```java
float value = RunExp.eval("pi * 0.5");
```

# Features:
- unary operations: '-'
- binary operations: '+', '-', '*', '/' and '^' (exponentation)
- functions:
  - sin
  - cos
  - tan
  - exp
  - sqrt
  - pow (same as '^' operator)
  - min(a, b)
  - max(a, b)
  - sign / signum
  - rand
  - smoother (smoother step)

# Examples:
```java
try:
  int width = (int)RunExp.eval(widthField.getText());
  // ...
catch (ExpCompileException e){
  invalidInputMessage(e.message);
}
```
