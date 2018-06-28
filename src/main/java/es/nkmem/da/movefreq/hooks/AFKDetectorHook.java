package es.nkmem.da.movefreq.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import es.nkmem.da.movefreq.MoveFreqPlugin;
import es.nkmem.da.movefreq.packets.LookPacket;
import es.nkmem.da.movefreq.packets.PositionLookPacket;
import es.nkmem.da.movefreq.packets.PositionPacket;
import es.nkmem.da.movefreq.wrappers.WrapperPlayClientLook;
import es.nkmem.da.movefreq.wrappers.WrapperPlayClientPosition;
import es.nkmem.da.movefreq.wrappers.WrapperPlayClientPositionLook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AFKDetectorHook {

	private Map<UUID, Long> lastUpdate = new ConcurrentHashMap<>();
	private LoadingCache<UUID, MovementData> movements = CacheBuilder.newBuilder()
			.build(new CacheLoader<UUID, MovementData>() {
				@Override
				public MovementData load(UUID uuid) throws Exception {
					return new MovementData();
				}
			});

	private void updateLast(Player player) {
		lastUpdate.put(player.getUniqueId(), System.currentTimeMillis());
	}

	private boolean isAFK(Player p) {
		return System.currentTimeMillis() - lastUpdate.get(p.getUniqueId()) 
				> MoveFreqPlugin.getInstance().getAFKThreshold() * 1000;
	}

	public void hook() {
		Bukkit.getOnlinePlayers().forEach(this::updateLast);

		MoveFreqPlugin.getInstance().getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerChat(AsyncPlayerChatEvent event) {
				updateLast(event.getPlayer());
			}

			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
				updateLast(event.getPlayer());
			}

			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerInteract(PlayerInteractEvent event) {
				updateLast(event.getPlayer());
			}

			@EventHandler
			public void onInventoryClick(InventoryClickEvent event) {
				updateLast((Player) event.getWhoClicked());
			}

			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerJoin(PlayerJoinEvent event) {
				updateLast(event.getPlayer());
			}

			@EventHandler(priority = EventPriority.LOWEST)
			public void onPlayerQuit(PlayerQuitEvent event) {
				lastUpdate.remove(event.getPlayer().getUniqueId());
				movements.invalidate(event.getPlayer().getUniqueId());
			}
		}, MoveFreqPlugin.getInstance());

		ProtocolManager protocolManager = MoveFreqPlugin.getInstance().getProtocolManager();

		// TODO: DRY
		PacketAdapter.AdapterParameteters posLook = 
				new PacketAdapter.AdapterParameteters().clientSide()
				.types(PacketType.Play.Client.POSITION_LOOK)
				.listenerPriority(ListenerPriority.LOWEST)
				.plugin(MoveFreqPlugin.getInstance());
		protocolManager.addPacketListener(new PacketAdapter(posLook) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				WrapperPlayClientPositionLook wrapper = 
						new WrapperPlayClientPositionLook(event.getPacket());
				PositionLookPacket packet = new PositionLookPacket(wrapper);
				Player p = event.getPlayer();

				MovementData data = movements.getUnchecked(p.getUniqueId());
				if (!packet.equals(data.getPositionLook())) {
					data.setPositionLook(packet);
					updateLast(p);
				} else if (isAFK(p)) {
					event.setCancelled(true);
				}
			}
		});

		PacketAdapter.AdapterParameteters look = 
				new PacketAdapter.AdapterParameteters().clientSide()
				.types(PacketType.Play.Client.LOOK)
				.listenerPriority(ListenerPriority.LOWEST)
				.plugin(MoveFreqPlugin.getInstance());
		protocolManager.addPacketListener(new PacketAdapter(look) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				WrapperPlayClientLook wrapper = new WrapperPlayClientLook(event.getPacket());
				LookPacket packet = new LookPacket(wrapper);
				Player p = event.getPlayer();

				MovementData data = movements.getUnchecked(p.getUniqueId());
				if (!packet.equals(data.getLook())) {
					data.setLook(packet);
					updateLast(p);
				} else if (isAFK(p)) {
					event.setCancelled(true);
				}
			}
		});

		PacketAdapter.AdapterParameteters pos = new PacketAdapter.AdapterParameteters().clientSide()
				.types(PacketType.Play.Client.POSITION)
				.listenerPriority(ListenerPriority.LOWEST)
				.plugin(MoveFreqPlugin.getInstance());
		protocolManager.addPacketListener(new PacketAdapter(pos) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				WrapperPlayClientPosition wrapper = new WrapperPlayClientPosition(event.getPacket());
				PositionPacket packet = new PositionPacket(wrapper);
				Player p = event.getPlayer();

				MovementData data = movements.getUnchecked(p.getUniqueId());
				if (!packet.equals(data.getPosition())) {
					data.setPosition(packet);
					updateLast(p);
				} else if (isAFK(p)) {
					event.setCancelled(true);
				}
			}
		});

		MoveFreqPlugin.getInstance().getServer().getScheduler()
		.runTaskTimerAsynchronously(MoveFreqPlugin.getInstance(), () -> {
			if (MoveFreqPlugin.getInstance().hasMessages()) {
				int afk = 0;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (isAFK(p)) afk++;
				}
				if (afk == 0) {
					return;
				}
				MoveFreqPlugin.getInstance().getLogger().info("Number of AFK players: " + afk);
			}
		}, 600, 600);
	}

	public class MovementData {
		private LookPacket look;
		private PositionPacket position;
		private PositionLookPacket positionLook;

		public PositionLookPacket getPositionLook() {
			return positionLook;
		}

		public void setPositionLook(PositionLookPacket positionLook) {
			this.positionLook = positionLook;
		}

		public PositionPacket getPosition() {
			return position;
		}

		public void setPosition(PositionPacket position) {
			this.position = position;
		}

		public LookPacket getLook() {
			return look;
		}

		public void setLook(LookPacket look) {
			this.look = look;
		}
	}

}
