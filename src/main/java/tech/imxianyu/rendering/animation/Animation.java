package tech.imxianyu.rendering.animation;

/**
 * the abstract class of an animation
 */
public abstract class Animation {


    public abstract double interpolate(boolean backwards, float speed);

    /**
     * cycles the animation.
     * [animation start] -> [animation end] -> [animation start] (and so on...)
     * @param speed the speed of the animation
     * @return animation value
     */
    public abstract double cycleAnimation(float speed);

    public abstract void reset();
}
