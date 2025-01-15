package dev.faiths.utils.render.animation.normal;

public enum Direction {
    FORWARDS,
    BACKWARDS;

    public Direction opposite() {
        if (this == Direction.FORWARDS) {
            return Direction.BACKWARDS;
        } else {
        	return Direction.FORWARDS;
        }
    }
}