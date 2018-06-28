package es.nkmem.da.movefreq.packets;

import es.nkmem.da.movefreq.wrappers.WrapperPlayClientLook;

public class LookPacket {

	private float yaw;
	private float pitch;
	private boolean onGround;

	public LookPacket(WrapperPlayClientLook wrapper) {
		this.yaw = wrapper.getYaw();
		this.pitch = wrapper.getPitch();
		this.onGround = wrapper.getOnGround();
	}

	public void apply(WrapperPlayClientLook wrapper) {
		wrapper.setYaw(yaw);
		wrapper.setPitch(pitch);
		wrapper.setOnGround(onGround);
	}
}
