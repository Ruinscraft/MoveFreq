package es.nkmem.da.movefreq;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import es.nkmem.da.movefreq.hooks.AFKDetectorHook;
import es.nkmem.da.movefreq.hooks.PositionLookHook;

import org.bukkit.plugin.java.JavaPlugin;

public class MoveFreqPlugin extends JavaPlugin {

	private static MoveFreqPlugin instance;
	private ProtocolManager protocolManager;

	private boolean messages;
	private int afkThreshold;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		messages = getConfig().getBoolean("messages", false);
		afkThreshold = getConfig().getInt("afk-threshold", 120);

		protocolManager = ProtocolLibrary.getProtocolManager();
		new PositionLookHook().hook();
		new AFKDetectorHook().hook();

		getCommand("movefreq").setExecutor((sender, command, label, args) -> {
			if (args.length < 1) {
				sender.sendMessage("/movefreq reload");
				return false;
			}
			if (args[0].equals("reload")) {
				reload();
				sender.sendMessage("MoveFreq reloaded");
				return true;
			}
			sender.sendMessage("/movefreq reload");
			return false;
		});
	}

	@Override
	public void onDisable() {
		protocolManager.removePacketListeners(this);
	}

	public void reload() {
		reloadConfig();
		messages = getConfig().getBoolean("messages", false);
		afkThreshold = getConfig().getInt("seconds-afk", 120);
	}

	public static MoveFreqPlugin getInstance() {
		return instance;
	}

	public ProtocolManager getProtocolManager() {
		return protocolManager;
	}

	public boolean hasMessages() {
		return messages;
	}

	public int getAFKThreshold() {
		return afkThreshold;
	}

}
