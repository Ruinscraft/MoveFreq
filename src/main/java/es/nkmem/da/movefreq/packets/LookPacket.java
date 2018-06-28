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

	public boolean equals(Object object) {
		if (!(object instanceof PositionPacket)) {
			return false;
		}
		LookPacket packet = (LookPacket) object;
		if (packet.yaw == this.yaw && packet.pitch == this.pitch 
				&& packet.onGround == this.onGround) {
			return true;
		}
		return false;
	}

}
