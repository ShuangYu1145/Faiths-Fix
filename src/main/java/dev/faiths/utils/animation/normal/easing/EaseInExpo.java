package dev.faiths.utils.animation.normal.easing;

import dev.faiths.utils.animation.normal.Animation;

public class EaseInExpo extends Animation {

	public EaseInExpo(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    return (x == 0) ? 0 : Math.pow(2, 10 * (x / duration - 1));
	}
}
