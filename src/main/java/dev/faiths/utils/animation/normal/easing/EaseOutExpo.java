package dev.faiths.utils.animation.normal.easing;

import dev.faiths.utils.animation.normal.Animation;

public class EaseOutExpo extends Animation {

	public EaseOutExpo(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    return (x == duration) ? 1 : (-Math.pow(2, -10 * x / duration) + 1);
	}
}
