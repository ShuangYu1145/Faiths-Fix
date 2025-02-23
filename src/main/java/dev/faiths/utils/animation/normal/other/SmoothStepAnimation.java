package dev.faiths.utils.animation.normal.other;

import dev.faiths.utils.animation.normal.Animation;
import dev.faiths.utils.animation.normal.Direction;

public class SmoothStepAnimation extends Animation {

    public SmoothStepAnimation(int ms, double endPoint) {
        super(ms, endPoint);
        this.reset();
    }

    public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
        this.reset();
    }

    protected double getEquation(double x) {
        double x1 = x / (double) duration;
        return -2 * Math.pow(x1, 3) + (3 * Math.pow(x1, 2));
    }
}
