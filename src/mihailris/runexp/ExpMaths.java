package mihailris.runexp;

import java.util.Random;

/**
 * Class for implementation of special RunExp built-in functions
 */
public class ExpMaths {
    public static final Random random = new Random();

    public static float smoother(float a){
        return a * a * a * (a * (a * 6 - 15) + 10);
    }

    /**
     * @param a used to make function non-constant
     * @return pseudorandom value in range [0.0, 1.0] not depending on parameter
     */
    public static float rand(@SuppressWarnings("unused") float a){
        return random.nextFloat();
    }
}
