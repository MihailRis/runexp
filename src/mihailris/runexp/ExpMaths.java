package mihailris.runexp;

import java.util.Random;

public class ExpMaths {
    public static final Random random = new Random();

    public static float smoother(float a){
        return a * a * a * (a * (a * 6 - 15) + 10);
    }
    public static float rand(float a){
        return random.nextFloat();
    }
}
