package org.valkyrienskies.addon.world.capability;

public class ImplCapabilityAntiGravity implements ICapabilityAntiGravity {
    private double antiGravity;
    private double multiplier = 1;

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public double getAntiGravity() {
        return antiGravity;
    }

    @Override
    public void setAntiGravity(double antiGravity) {
        this.antiGravity = antiGravity;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}
