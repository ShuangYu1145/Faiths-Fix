package wtf.pub;

import dev.faiths.utils.DebugUtil;
import dev.faiths.utils.TimerUtil;
import dev.faiths.utils.math.MathUtils;

import javax.vecmath.Vector2f;

public class CalculateThread extends Thread {
    private static final double T = 10;
    private static final double T_MIN = 0.0001;
    private static final double ALPHA = 0.997;
    private int iteration;
    private double temperature, energy, solutionE;
    public Vector2f solution;
    public boolean stop;
    private final ProjectileUtil.EnderPearlPredictor predictor;

    public CalculateThread(double predictX, double predictY, double predictZ, double minMotionY, double maxMotionY) {
        predictor = new ProjectileUtil.EnderPearlPredictor(predictX, predictY, predictZ, minMotionY, maxMotionY);
        this.iteration = 0;
        this.temperature = T;
        this.energy = 0;
        stop = false;
    }

    @Override
    public void run() {
        TimerUtil timer = new TimerUtil();
        timer.reset();
        solution = new Vector2f(
                MathUtils.getRandomInRange(-180, 180),
                MathUtils.getRandomInRange(-90, 90)
        );
        Vector2f current = solution;
        energy = predictor.assessRotation(solution);
        solutionE = energy;
        while (temperature >= T_MIN && !stop) {
            Vector2f rotation = new Vector2f(
                    (float) (current.x + MathUtils.getRandomInRange(-temperature * 18, temperature * 18)),
                    (float) (current.y + MathUtils.getRandomInRange(-temperature * 9, temperature * 9))
            );
            if (rotation.y > 90F) rotation.y = 90F;
            if (rotation.y < -90F) rotation.y = -90F;
            double assessment = predictor.assessRotation(rotation);
            double deltaE = assessment - energy;
            if (deltaE >= 0 || MathUtils.getRandomInRange(0, 1) < Math.exp(-deltaE / temperature * 100)) {
                energy = assessment;
                current = rotation;
                if (assessment > solutionE) {
                    if (assessment > 0.9) {
                        DebugUtil.log("new solution (" + solution.x + ", " + solution.y + "), value: " + assessment + ". it is too high, i think it can't trust. let's us find other solution");
                    } else {
                        solutionE = assessment;
                        solution = new Vector2f(rotation.x, rotation.y);
                        DebugUtil.log("new solution (" + solution.x + ", " + solution.y + "), value: " + solutionE);
                    }
                }
            }
            if (solutionE >= 0.8 && solutionE <= 0.9) {
                DebugUtil.log("its good enough, just do it");
                break;
            }
            temperature *= ALPHA;
        }
        DebugUtil.log("Path finding completed");
    }
}
