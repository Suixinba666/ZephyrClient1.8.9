package tech.imxianyu.events.player;

import tech.imxianyu.eventapi.Event;
import tech.imxianyu.eventapi.EventState;

public class StepEvent extends EventState {
    private final boolean pre;
    private double stepHeight;
    private double realHeight;

    public StepEvent(boolean state, double stepHeight, double realHeight) {
        this.pre = state;
        this.stepHeight = stepHeight;
        this.realHeight = realHeight;
    }

    public StepEvent(boolean state, double stepHeight) {
        this.pre = state;
        this.stepHeight = stepHeight;
        this.realHeight = this.realHeight;
    }

    public boolean isPre() {
        return this.pre;
    }

    public double getStepHeight() {
        return this.stepHeight;
    }

    public void setStepHeight(double stepHeight) {
        this.stepHeight = stepHeight;
    }

    public double getRealHeight() {
        return this.realHeight;
    }

    public void setRealHeight(double realHeight) {
        this.realHeight = realHeight;
    }
}
