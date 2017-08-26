package ValkyrienWarfareBase.Fixes;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class SoundFixWrapper implements ISound {

	final Vector soundLocation;
	private final ISound wrappedSound;
	private final PhysicsWrapperEntity wrapper;

	public SoundFixWrapper(ISound wrappedSound, PhysicsWrapperEntity wrapper, Vector soundLocation) {
		this.wrappedSound = wrappedSound;
		this.wrapper = wrapper;
		this.soundLocation = soundLocation;
	}

	@Override
	public ResourceLocation getSoundLocation() {
		return wrappedSound.getSoundLocation();
	}

	@Override
	public SoundEventAccessor createAccessor(SoundHandler handler) {
		return wrappedSound.createAccessor(handler);
	}

	@Override
	public Sound getSound() {
		return wrappedSound.getSound();
	}

	@Override
	public SoundCategory getCategory() {
		return wrappedSound.getCategory();
	}

	@Override
	public boolean canRepeat() {
		return wrappedSound.canRepeat();
	}

	@Override
	public int getRepeatDelay() {
		return wrappedSound.getRepeatDelay();
	}

	@Override
	public float getVolume() {
		return wrappedSound.getVolume();
	}

	@Override
	public float getPitch() {
		return wrappedSound.getPitch();
	}

	@Override
	public float getXPosF() {
		return (float) soundLocation.X;
	}

	@Override
	public float getYPosF() {
		return (float) soundLocation.Y;
	}

	@Override
	public float getZPosF() {
		return (float) soundLocation.Z;
	}

	@Override
	public AttenuationType getAttenuationType() {
		return wrappedSound.getAttenuationType();
	}

}