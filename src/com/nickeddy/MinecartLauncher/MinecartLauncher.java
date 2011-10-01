package com.nickeddy.MinecartLauncher;

import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
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
		pluginManager.registerEvent(Event.Type.VEHICLE_COLLISION_ENTITY,
				mclVehicleListener, Event.Priority.Normal, this);
	}

	public void onDisable() {
		log.info(this.getDescription().getName() + " "
				+ this.getDescription().getVersion() + " disabled!");
		minecarts.clear();// clear the minecarts
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {

		/*
		 * ***********Launch************
		 */
		if (command.getName().equalsIgnoreCase("launch")) {
			if (canLaunch(sender)) {
				Player player = (Player) sender;
				World world = player.getWorld();
				int blockPlayerIsOn = world.getBlockTypeIdAt(player
						.getLocation());
				// if block player is on is a rail, powered rail, or detector
				// rail...
				if (blockPlayerIsOn == 27 || blockPlayerIsOn == 28
						|| blockPlayerIsOn == 66) {

					Vector v = getLaunchDirection(player.getLocation());

					// spawn a minecart and put the player in it, and then set
					// the velocity.
					Minecart minecart = world.spawn(player.getLocation(),
							Minecart.class);
					minecart.setPassenger(player);
					minecart.setVelocity(v);
					this.minecarts.add(minecart);
					return true;
				} else {
					sender.sendMessage("You must be on a minecart rail!");
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

	private Vector getLaunchDirection(Location location) {
		Vector v = location.getDirection().clone();

		double rotation = (location.getYaw() - 90.0D) % 360.0D;
		if (rotation < 0.0D) {
			rotation += 360.0D;
		}
		// calculate north/west/east/south
		if (rotation <= 45.0 || rotation > 315.0) {
			// send north
			v.setX(-8);
		} else if (rotation > 45.0 && rotation <= 135.0) {
			// send east
			v.setZ(-8);
		} else if (rotation > 135.0 && rotation <= 225.0) {
			// send south
			v.setX(8);
		} else if (rotation > 225.0 && rotation <= 315.0) {
			// send west
			v.setZ(8);
		}

		return v;
	}

	private boolean canLaunch(CommandSender sender) {
		return sender.hasPermission("nickeddy.MinecartLauncher.launch");
	}

	private class MCLVehicleListener extends VehicleListener {
		MinecartLauncher launcher;

		public MCLVehicleListener(MinecartLauncher launcher) {
			this.launcher = launcher;
		}

		@Override
		public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
			if (event.getVehicle() instanceof Minecart
					&& event.getEntity() instanceof Minecart) {
				// TODO prevent minecart collisions!
				event.setCollisionCancelled(true);
				event.setCancelled(true);

				Location vehicleLocation = event.getVehicle().getLocation()
						.clone();
				Location entityLocation = event.getEntity().getLocation()
						.clone();

				Vector vehicleVector = getLaunchDirection(vehicleLocation);
				Vector entityVector = getLaunchDirection(entityLocation);

				event.getEntity().teleport(vehicleLocation);
				event.getVehicle().teleport(entityLocation);
				event.getVehicle().setVelocity(vehicleVector);
				event.getEntity().setVelocity(entityVector);

			}
		}

		@Override
		public void onVehicleExit(VehicleExitEvent event) {
			Vehicle vehicle = event.getVehicle();
			if (launcher.minecarts.contains(vehicle)) {
				vehicle.remove();
				launcher.minecarts.remove(vehicle);
			}
		}
	}
}
