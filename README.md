# RunExp
A lightweight math expressions solver for Java that compiles expressions directly into JVM bytecode to minimize runtime overhead aimed to precompiling expressions.

Also planned to implement runexp-bytecode compilation to use when JVM-bytecode build is not available.

# Usage:

with x:
```java
Expression expression = RunExp.compile("sin(x) * 0.5 + (x * 0.1)");
expression.eval(1.25f); // same as sin(1.25f) * 0.5f + (1.25f * 0.1f)
```


### Solve constant expression:

```java
float value = RunExp.eval("pi * 0.5");
```

if Expression wrapper needed (ConstantExpression used):
```java
Expression expression = RunExp.compile("pi ^ 2", true);
```

### Setting up:
- Add custom constant: `RunExp.addConstant(name, value)`
- RunExp.allowJVM setting - allow compiling expressions directly into JVM bytecode (true by default)

# Features:
- unary operations: '-'
- binary operations: '+', '-', '*', '/' and '^' (exponentation)
- functions:
  - abs
  - sin
  - cos
  - tan
  - exp
  - sqrt
  - pow (same as '^' operator)
  - min(a, b)
  - max(a, b)
  - sign / signum
  - rand - random number in range `[0.0, 1.0]`
  - smoother (smoother step)
- constants:
  - pi (Math.PI)
  - pi2 (Math.PI * 2)
  - e (Math.E)
  - raddeg (180.0 / Math.PI) usage: degrees = radians * raddeg
  - degrad (Math.PI / 180.0) usage: radians = degreen * degrad

# Examples:
```java
try {
  int width = (int)RunExp.eval(widthField.getText());
  // ...
} catch (ExpCompileException e){
  invalidInputMessage(e.message);
}
```
