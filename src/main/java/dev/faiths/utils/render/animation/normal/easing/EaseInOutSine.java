package dev.faiths.utils.render.animation.normal.easing;

import dev.faiths.utils.render.animation.normal.Animation;

public class EaseInOutSine extends Animation {

	public EaseInOutSine(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    return -0.5 * (Math.cos(Math.PI * x / duration) - 1);
	}
}
