package es.nkmem.da.movefreq.packets;

import es.nkmem.da.movefreq.wrappers.WrapperPlayClientPositionLook;

public class PositionLookPacket {

	private long initTime;

	private double x;
	private double y;
	private double z;

	private float yaw;
	private float pitch;

	private boolean onGround;

	public PositionLookPacket(WrapperPlayClientPositionLook wrapper) {
		this.initTime = System.currentTimeMillis();
		this.x = wrapper.getX();
		this.y = wrapper.getY();
		this.z = wrapper.getZ();

		this.yaw = wrapper.getYaw();
		this.pitch = wrapper.getPitch();
		this.onGround = wrapper.getOnGround();
	}

	public boolean isExpired() {
		return (System.currentTimeMillis() - initTime) > 50;
	}

	public void apply(WrapperPlayClientPositionLook wrapper) {
		wrapper.setX(x);
		wrapper.setY(y);
		wrapper.setZ(z);

		wrapper.setYaw(yaw);
		wrapper.setPitch(pitch);
		wrapper.setOnGround(onGround);
	}

	public boolean equals(Object object) {
		if (!(object instanceof PositionPacket)) {
			return false;
		}
		PositionLookPacket packet = (PositionLookPacket) object;
		if (packet.x == this.x && packet.y == this.y 
				&& packet.z == this.z && packet.onGround == this.onGround
				&& packet.yaw == this.yaw && packet.pitch == this.pitch) {
			return true;
		}
		return false;
	}

}
