package es.nkmem.da.movefreq.packets;

import es.nkmem.da.movefreq.wrappers.WrapperPlayClientPosition;

public class PositionPacket {

	private double x;
	private double y;
	private double z;
	private boolean onGround;

	public PositionPacket(WrapperPlayClientPosition wrapper) {
		this.x = wrapper.getX();
		this.y = wrapper.getY();
		this.z = wrapper.getZ();
		this.onGround = wrapper.getOnGround();
	}

	public void apply(WrapperPlayClientPosition wrapper) {
		wrapper.setX(x);
		wrapper.setY(y);
		wrapper.setZ(z);
		wrapper.setOnGround(onGround);
	}
}

