package es.nkmem.da.movefreq.packethooks;

import com.comphenix.packetwrapper.WrapperPlayClientPositionLook;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import es.nkmem.da.movefreq.MoveFreqPlugin;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PositionLookHook {
    private final MoveFreqPlugin plugin;
    private Map<UUID, PositionLookPacket> cache = new ConcurrentHashMap<>();

    @Data
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
    }

    long suppressed = 0;
    long total = 0;

    public void hook() {
        PacketAdapter.AdapterParameteters params = new PacketAdapter.AdapterParameteters().clientSide()
                .types(PacketType.Play.Client.POSITION_LOOK).listenerPriority(ListenerPriority.LOWEST).plugin(plugin);
        PacketAdapter adapter = new PacketAdapter(params) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
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
        plugin.getProtocolManager().addPacketListener(adapter);
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                cache.remove(e.getPlayer().getUniqueId());
            }
        }, plugin);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Suppressed " + (suppressed * 100) / total + "% of " + total);
        }, 100, 100);
    }
}