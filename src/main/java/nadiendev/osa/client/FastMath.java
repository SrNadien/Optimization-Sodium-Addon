package nadiendev.osa.client;

import net.minecraft.util.Mth;

public final class FastMath {
    private static final int SIZE = 4096;
    private static final int MASK = SIZE - 1;
    private static final float SCALE = (float) (SIZE / (2.0 * Math.PI));
    private static final float QUARTER_TURN = (float) (Math.PI / 2.0);
    private static final float[] TABLE = new float[SIZE + 1];

    private static volatile boolean enabled;

    static {
        for (int i = 0; i <= SIZE; i++) {
            TABLE[i] = (float) Math.sin(i * 2.0 * Math.PI / SIZE);
        }
    }

    private FastMath() {}

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static float sin(float value) {
        float scaled = value * SCALE;
        int index = Mth.floor(scaled);
        float fraction = scaled - index;
        int low = index & MASK;
        float start = TABLE[low];
        return start + (TABLE[low + 1] - start) * fraction;
    }

    public static float cos(float value) {
        return sin(value + QUARTER_TURN);
    }
}
