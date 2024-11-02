package dev.faiths.utils.tasks;

public abstract class FutureTask {

    /* fields */
    private final int delay;
    private long lastTime;

    /* constructors */
    protected FutureTask(final int delay) {
        this.delay = delay;
        this.lastTime = System.nanoTime() / 1000000L;
    }

    /* methods */
    public final boolean delay() {
        return System.nanoTime() / 1000000L - this.lastTime >= this.delay;
    }

    public abstract void execute();

    public abstract void run();

}
