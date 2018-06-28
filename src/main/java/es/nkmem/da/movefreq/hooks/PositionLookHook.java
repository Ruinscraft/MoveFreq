package es.nkmem.da.movefreq.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import es.nkmem.da.movefreq.MoveFreqPlugin;
import es.nkmem.da.movefreq.packets.PositionLookPacket;
import es.nkmem.da.movefreq.wrappers.WrapperPlayClientPositionLook;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PositionLookHook {

	private Map<UUID, PositionLookPacket> cache = new ConcurrentHashMap<>();

	long suppressed = 0;
	long total = 0;

	public void hook() {
		PacketAdapter.AdapterParameteters params = new PacketAdapter.AdapterParameteters().clientSide()
				.types(PacketType.Play.Client.POSITION_LOOK)
				.listenerPriority(ListenerPriority.LOW)
				.plugin(MoveFreqPlugin.getInstance());
		PacketAdapter adapter = new PacketAdapter(params) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.isCancelled()) return;
				Player p = event.getPlayer();
				UUID uuid = p.getUniqueId();
				WrapperPlayClientPositionLook wrapper = new WrapperPlayClientPositionLook(event.getPacket());

				PositionLookPacket last = cache.get(uuid);
				if (last == null || last.isExpired()) {
					cache.put(uuid, new PositionLookPacket(wrapper));
				} else if (p.getVelocity().length() < 0.0785) {
					suppressed++;
					last.apply(wrapper);
				}
				total++;
			}
		};
		MoveFreqPlugin.getInstance().getProtocolManager().addPacketListener(adapter);
		MoveFreqPlugin.getInstance().getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent e) {
				cache.remove(e.getPlayer().getUniqueId());
			}
		}, MoveFreqPlugin.getInstance());

		if (MoveFreqPlugin.getInstance().hasMessages()) {
			MoveFreqPlugin.getInstance().getServer().getScheduler()
			.runTaskTimerAsynchronously(MoveFreqPlugin.getInstance(), () -> {
				if (total == 0) {
					return;
				}
				MoveFreqPlugin.getInstance()
				.getLogger().info("Suppressed " + (suppressed * 100) / total + "% of " + total);
				suppressed = 0;
				total = 0;
	        }, 600, 600);
		}
	}

}
