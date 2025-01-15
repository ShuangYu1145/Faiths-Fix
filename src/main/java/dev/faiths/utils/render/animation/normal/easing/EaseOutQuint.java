package dev.faiths.utils.render.animation.normal.easing;

import dev.faiths.utils.render.animation.normal.Animation;

public class EaseOutQuint extends Animation {

	public EaseOutQuint(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
	    double x1 = x / duration;
	    x1--;
	    return x1 * x1 * x1 * x1 * x1 + 1;
	}
}
