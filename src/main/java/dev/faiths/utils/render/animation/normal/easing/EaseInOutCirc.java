package dev.faiths.utils.render.animation.normal.easing;

import dev.faiths.utils.render.animation.normal.Animation;

public class EaseInOutCirc extends Animation {

	public EaseInOutCirc(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    double x1 = x / duration - 1;
	    return Math.sqrt(1 - x1 * x1);
	}
}
