package valkyrienwarfare.addon.control.fuel;

public interface IEtherGasEngine {

    int getCurrentEtherGas();

    int getEtherGasCapacity();
    
    // pre : Throws an IllegalArgumentExcepion if more gas is added than there is
    //       capacity for this engine.
    void addEtherGas(int gas);
}
