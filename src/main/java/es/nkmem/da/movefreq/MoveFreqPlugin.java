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
	private double velocityThreshold;

	@Override
	public void onEnable() {
		instance = this;

		saveDefaultConfig();
		defineVariables();

		protocolManager = ProtocolLibrary.getProtocolManager();
		new PositionLookHook().hook();
		new AFKDetectorHook().hook();

		registerCommand();
	}

	@Override
	public void onDisable() {
		protocolManager.removePacketListeners(this);
	}

	public void reload() {
		reloadConfig();
		defineVariables();
	}

	public void defineVariables() {
		messages = getConfig().getBoolean("messages");
		afkThreshold = getConfig().getInt("afk-threshold");
		velocityThreshold = getConfig().getDouble("velocity-threshold");
	}

	public void registerCommand() {
		getCommand("movefreq").setExecutor((sender, command, label, args) -> {
			if (args.length < 1) {
				sender.sendMessage("/movefreq reload");
				return false;
			}
			if (args[0].equals("reload")) {
				reload();
				getLogger().info("Configuration reloaded by " + sender.getName());
				sender.sendMessage("MoveFreq configuration reloaded");
				return true;
			}
			sender.sendMessage("/movefreq reload");
			return false;
		});
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

	public double getVelocityThreshold() {
		return velocityThreshold;
	}

}
