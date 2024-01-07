package tech.imxianyu.rendering.animation.animations;

import tech.imxianyu.rendering.animation.Animation;
import tech.imxianyu.rendering.animation.AnimationSystem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ImXianyu
 * @since 1/8/2023 10:06 AM
 */
public class MultiEndpointAnimation extends Animation {

    public Map<Integer, Double> endpoints = new HashMap<>();

    public int curEndpoint = -1;
    public int totalEndPoints;
    public double startValue = -115213.313;
    public double value;
    boolean backwardsCycle = false;

    @Override
    public void reset() {
        curEndpoint = 0;

        if (startValue != -115213.313) {
            value = startValue;
        } else {
            value = endpoints.get(0);
        }

    }

    @Override
    public double interpolate(boolean backwards, float speed) {

        if (!backwards) {
            double nextPoint;
            if (curEndpoint < totalEndPoints - 1) {
                try {
                    nextPoint = endpoints.get(curEndpoint + 1);
                } catch (NullPointerException exception) {
                    nextPoint = endpoints.get(0);
                    System.err.println("Current endpoint: " + (curEndpoint + 1));
                    exception.printStackTrace();
                }
            } else if (curEndpoint == -1) {
                nextPoint = 0;
            } else {
                nextPoint = endpoints.get(totalEndPoints - 1);
            }

            value = AnimationSystem.interpolate(value, nextPoint, speed);

            if (isInApproximate(value, nextPoint) && curEndpoint < totalEndPoints - 1) {
                curEndpoint++;
            }
        } else {
            if (curEndpoint > -1) {
                double nextPoint;

                if (curEndpoint == 0) {
                    nextPoint = endpoints.get(0);
                } else {
                    try {
                        nextPoint = endpoints.get(curEndpoint - 1);
                    } catch (NullPointerException exception) {
                        nextPoint = endpoints.get(0);
                        System.err.println("Current endpoint: " + (curEndpoint - 1));
                        exception.printStackTrace();
                    }
                }

                value = AnimationSystem.interpolate(value, nextPoint, speed);

                if (isInApproximate(value, nextPoint)) {
                    curEndpoint--;
                }
            }
        }

        return value;
    }

    @Override
    public double cycleAnimation(float speed) {
        if (!backwardsCycle) {
            if (curEndpoint < totalEndPoints - 1) {
                double nextPoint = endpoints.get(curEndpoint + 1);

                value = AnimationSystem.interpolate(value, nextPoint, speed);

                if (isInApproximate(value, nextPoint)) {
                    curEndpoint++;
                }
            } else {
                backwardsCycle = true;
            }
        } else {
            if (curEndpoint > 0) {
                double nextPoint = endpoints.get(curEndpoint - 1);

                value = AnimationSystem.interpolate(value, nextPoint, speed);

                if (isInApproximate(value, nextPoint)) {
                    curEndpoint--;
                }
            } else {
                backwardsCycle = false;
            }
        }

        return value;
    }

    private boolean isInApproximate(double value, double value2) {

        value = new BigDecimal(value).setScale(3, RoundingMode.DOWN).doubleValue();
        value2 = new BigDecimal(value2).setScale(3, RoundingMode.DOWN).doubleValue();

        if (value == 0)
            value = 0.0001;

        if (value2 == 0)
            value2 = 0.0001;

        if (value < value2) {
            return Math.abs(value / value2) > 0.98;
        } else {
            return Math.abs(value2 / value) > 0.98;
        }
    }

    public MultiEndpointAnimation withEndPoints(double... endPoints) {

        this.totalEndPoints = endPoints.length;

        for (int i = 0; i < endPoints.length; i++) {
            endpoints.put(i, endPoints[i]);
        }

        return this;
    }

    public MultiEndpointAnimation withStartValue(double startValue) {
        this.value = startValue;
        this.startValue = startValue;

        return this;
    }
}
