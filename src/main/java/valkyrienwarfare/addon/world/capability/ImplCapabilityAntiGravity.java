package valkyrienwarfare.addon.world.capability;

public class ImplCapabilityAntiGravity implements ICapabilityAntiGravity {

    private double antiGravity;
    
    @Override
    public double getAntiGravity() {
        return antiGravity;
    }

    @Override
    public void setAntiGravity(double antiGravity) {
        this.antiGravity = antiGravity;
    }

}
