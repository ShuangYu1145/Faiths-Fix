package dev.faiths.utils.render.animation.normal.easing;

import dev.faiths.utils.render.animation.normal.Animation;

public class EaseInOutCubic extends Animation {

	public EaseInOutCubic(int ms, double endPoint) {
		super(ms, endPoint);
		this.reset();
	}

	@Override
	protected double getEquation(double x) {
		
	    double x1 = x / (duration / 2);
	    
	    if (x1 < 1) {
	    	return 0.5 * x1 * x1 * x1;
	    }
	    
	    x1 -= 2;
	    
	    return 0.5 * (x1 * x1 * x1 + 2);
	}
}
