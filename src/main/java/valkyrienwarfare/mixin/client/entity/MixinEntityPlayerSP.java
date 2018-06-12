package valkyrienwarfare.mixin.client.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.entity.EntityPlayerSP;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.mod.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.network.SubspacedEntityRecordMessage;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

	private final ISubspacedEntity thisAsSubspaced = ISubspacedEntity.class.cast(this);

	@Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
	private void postOnUpdateWalkingPlayer(CallbackInfo info) {
		IDraggable draggable = IDraggable.class.cast(this);
		if (draggable.getWorldBelowFeet() != null) {
			draggable.getWorldBelowFeet().getPhysicsObject().getSubspace().snapshotSubspacedEntity(thisAsSubspaced);
			ISubspacedEntityRecord entityRecord = draggable.getWorldBelowFeet().getPhysicsObject().getSubspace()
					.getRecordForSubspacedEntity(thisAsSubspaced);
			SubspacedEntityRecordMessage recordMessage = new SubspacedEntityRecordMessage(entityRecord);
			ValkyrienWarfareMod.physWrapperNetwork.sendToServer(recordMessage);
			// Do the magic
			// System.out.println("REEEEEEEEEEEEE3#3333EEEEEEE!!!!!!!");
		}
	}
}
