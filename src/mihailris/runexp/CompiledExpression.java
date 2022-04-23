package mihailris.runexp;

public class CompiledExpression implements Expression {
    final byte[] bytecode;
    public CompiledExpression(byte[] bytecode){
        this.bytecode = bytecode;
    }

    @Override
    public float eval(float x) {
        return 0; // TODO: implement
    }
}
