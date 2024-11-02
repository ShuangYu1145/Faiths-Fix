package dev.faiths.utils.render;

public class SmoothAnimationTimer {

    public float target;

    public float speed = 0.3f;

    public SmoothAnimationTimer(float target) {
        this.target = target;
    }

    public SmoothAnimationTimer(float target, float speed) {
        this.target = target;
        this.speed = speed;
    }

    private float value = 0;

    public boolean update(boolean increment) {
        this.value = AnimationUtil.getAnimationState(value, increment ? target : 0,
                (float) (Math.max(10, (Math.abs(this.value - (increment ? target : 0))) * 40) * speed));
        return value == target;
    }

    public void setValue(float f) {
        value = f;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setTarget(float scrollY) {
        this.target = scrollY;
    }

    public float getSpeed() {
        return speed;
    }

    public float getTarget() {
        return target;
    }

    public float getValue() {
        return value;
    }
}