package dev.faiths.utils.render.animation.normal.easing;

import dev.faiths.utils.render.animation.normal.Animation;

public class EaseInSine extends Animation {

	public EaseInSine(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    return -1 * Math.cos(x / duration * (Math.PI / 2)) + 1;
	}
}
