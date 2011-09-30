package com.nickeddy.MinecartLauncher;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class MinecartLauncher extends JavaPlugin {

	private Logger log = Logger.getLogger("Minecraft");
	private HashSet<Minecart> minecarts = new HashSet<Minecart>();

	private MCLVehicleListener mclVehicleListener = new MCLVehicleListener(this);

	public void onEnable() {
		log.info(this.getDescription().getName() + " "
				+ this.getDescription().getVersion() + " enabled!");
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvent(Event.Type.VEHICLE_EXIT,
				mclVehicleListener, Event.Priority.Normal, this);
	}

	public void onDisable() {
		log.info(this.getDescription().getName() + " "
				+ this.getDescription().getVersion() + " disabled!");
		minecarts.clear();// clear the minecarts
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("launch")) {
			if (canLaunch(sender)) {
				Player player = (Player) sender;
				// TODO
				World world = player.getWorld();
				int blockPlayerIsOn = world.getBlockTypeIdAt(player
						.getLocation());
				// if block player is on is a rail, powered rail, or detector
				// rail
				if (blockPlayerIsOn == 27 || blockPlayerIsOn == 28
						|| blockPlayerIsOn == 66) {

					Vector v = player.getLocation().getDirection().clone();

					double playerRotation = (player.getLocation().getYaw() - 90.0D) % 360.0D;
					if (playerRotation < 0.0D) {
						playerRotation += 360.0D;
					}
					// calculate north/west/east/south
					if (playerRotation <= 45.0 || playerRotation > 315.0) {
						// send north
						v.setX(-8);
					} else if (playerRotation > 45.0 && playerRotation <= 135.0) {
						// send east
						v.setZ(-8);
					} else if (playerRotation > 135.0
							&& playerRotation <= 225.0) {
						// send south
						v.setX(8);
					} else if (playerRotation > 225.0
							&& playerRotation <= 315.0) {
						// send west
						v.setZ(8);
					}

					// spawn a minecart and put the player in it, and then set
					// the velocity.
					Minecart minecart = world.spawn(player.getLocation(),
							Minecart.class);
					minecart.setPassenger(player);
					minecart.setVelocity(v);
					this.minecarts.add(minecart);
					return true;
				} else {
					sender
							.sendMessage("/launch requires that you be on a minecart rail!");
					return false;
				}
			} else {
				sender.sendMessage("You don't have permission to do that!");
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean canLaunch(CommandSender sender) {
		return sender.hasPermission("nickeddy.MinecartLauncher.launch");
	}

	private class MCLVehicleListener extends VehicleListener {
		MinecartLauncher launcher;

		public MCLVehicleListener(MinecartLauncher launcher) {
			this.launcher = launcher;
		}

		public void onVehicleCollision(VehicleCollisionEvent event) {
			if (event.getVehicle() instanceof Minecart) {
				// TODO prevent minecart collisions!
			}
		}

		@Override
		public void onVehicleExit(VehicleExitEvent event) {
			Vehicle vehicle = event.getVehicle();
			if (vehicle instanceof Minecart) {
				if (launcher.minecarts.contains(vehicle)) {
					vehicle.remove();
					launcher.minecarts.remove(vehicle);
				}
			}
		}
	}
}
