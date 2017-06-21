package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysicsManagement.CoordTransformObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;

public class PhysicsCalculationsOrbital extends PhysicsCalculations {

    public boolean isOrbitalPhased = true;
    private Vector setLinearVel = new Vector();
    private Vector setAngularVel = new Vector();

    public PhysicsCalculationsOrbital(PhysicsObject toProcess) {
        super(toProcess);
    }

    @Override
    public void processWorldCollision() {
        if (!isOrbitalPhased) {
            super.processWorldCollision();
        }
    }

    @Override
    public void calculateForces() {
        isOrbitalPhased = true;
        if (!isOrbitalPhased) {
            super.calculateForces();
        } else {
            double modifiedDrag = Math.pow(drag, physTickSpeed / .05D);
            setLinearVel.multiply(modifiedDrag);
            setAngularVel.multiply(modifiedDrag);
        }
    }

    @Override
    public void addQueuedForces() {
        if (!isOrbitalPhased) {
            super.addQueuedForces();
        }
    }

    @Override
    public void applyLinearVelocity() {
        if (!isOrbitalPhased) {
            super.applyLinearVelocity();
        } else {
            parent.wrapper.posX += physTickSpeed * setLinearVel.X;
            parent.wrapper.posY += physTickSpeed * setLinearVel.Y;
            parent.wrapper.posZ += physTickSpeed * setLinearVel.Z;
        }
    }

    @Override
    public void applyAngularVelocity() {
        if (!isOrbitalPhased) {
            super.applyAngularVelocity();
        } else {
            CoordTransformObject coordTrans = parent.coordTransform;

            double[] rotationChange = RotationMatrices.getRotationMatrix(setAngularVel.X, setAngularVel.Y, setAngularVel.Z, angularVelocity.length() * physTickSpeed);
            Quaternion faggot = Quaternion.QuaternionFromMatrix(RotationMatrices.getMatrixProduct(rotationChange, coordTrans.lToWRotation));
            double[] radians = faggot.toRadians();

            wrapperEnt.pitch = Double.isNaN(radians[0]) ? 0.0f : (float) Math.toDegrees(radians[0]);
            wrapperEnt.yaw = Double.isNaN(radians[1]) ? 0.0f : (float) Math.toDegrees(radians[1]);
            wrapperEnt.roll = Double.isNaN(radians[2]) ? 0.0f : (float) Math.toDegrees(radians[2]);
            coordTrans.updateAllTransforms();
        }
    }

    public void setLinearVel(Vector newLinearVel) {
        setLinearVel = newLinearVel;
    }

    public void setAngularVel(Vector newAngularVel) {
        setAngularVel = newAngularVel;
    }

}
