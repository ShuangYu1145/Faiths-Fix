package dev.faiths.utils.animation.normal.easing;

import dev.faiths.utils.animation.normal.Animation;

public class EaseOutQuart extends Animation {

	public EaseOutQuart(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    double x1 = x / duration;
	    x1--;
	    return -1 * (x1 * x1 * x1 * x1 - 1);
	}
}
