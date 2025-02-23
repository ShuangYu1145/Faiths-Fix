package dev.faiths.utils.animation.normal.easing;

import dev.faiths.utils.animation.normal.Animation;

public class EaseOutCirc extends Animation {

	public EaseOutCirc(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    double x1 = x / duration - 1;
	    return Math.sqrt(1 - x1 * x1);
	}
}
