package dev.faiths.utils.render;

import dev.faiths.Faiths;

public class AnimationUtil {

    private static boolean canAnimate;
    public static float rotateDirection = 0;

    public static int getDelta() {
        return Faiths.delta;
    }

    public static float animate(float current, float target, float speed) {

        if (getDelta() <= 50 && !canAnimate) canAnimate = true;
        if (!canAnimate) return current;

        return purse(target, current, getDelta(), Math.abs(target - current) * speed);
    }

    public static float getAnimationState(float animation, float finalState, float speed) {
        final float add = (float) (Faiths.delta * (speed / 1000f));
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }
        return animation;
    }

    public static float purse(float target, float current, long delta, float speed) {

        if (delta < 1L) delta = 1L;

        final float difference = current - target;

        final float smoothing = Math.max(speed * (delta / 16F), .15F);

        if (difference > speed)
            current = Math.max(current - smoothing, target);
        else if (difference < -speed)
            current = Math.min(current + smoothing, target);
        else current = target;

        return current;
    }

    public static float getRotateDirection() {// AllitemRotate->Rotate
        rotateDirection = rotateDirection + (float) Faiths.delta;
        if (rotateDirection > 360)
            rotateDirection = 0;
        return rotateDirection;
    }
}