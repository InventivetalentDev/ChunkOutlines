package org.inventivetalent.chunkoutline;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.boundingbox.BoundingBox;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.Command;
import org.inventivetalent.pluginannotations.command.OptionalArg;
import org.inventivetalent.pluginannotations.command.Permission;
import org.inventivetalent.vectors.d3.Vector3DDouble;

public class ChunkOutline extends JavaPlugin {

	public static final BoundingBox chunkBaseBounds = new BoundingBox(0, 0, 0, 16, 256, 16);

	@Override
	public void onEnable() {
		PluginAnnotations.loadAll(this, this);
	}

	@Command(name = "chunkoutline",
			 aliases = {
					 "showchunks"
			 },
			 usage = "[duration]",
			 description = "Show the outline of chunks around you",
			 min = 0,
			 max = 1,
			 fallbackPrefix = "chunkoutline")
	@Permission("chunkoutline.show")
	public void chunkOutline(final Player sender, @OptionalArg(def = "5") Integer duration) {
		Chunk chunk = sender.getLocation().getChunk();
		int highestY = sender.getWorld().getHighestBlockYAt(sender.getLocation().getBlockX(), sender.getLocation().getBlockY());
		spawnClouds(sender.getWorld(), getChunkBounds(chunk), Math.max(1, Math.min(sender.getLocation().getY() - 16, highestY)), duration);

		sender.sendMessage("§eShowing outline of the current chunk for §7" + duration + " seconds§e.");
	}

	public BoundingBox getChunkBounds(Chunk chunk) {
		return chunkBaseBounds.translate(chunk.getX() << 4, 0, chunk.getZ() << 4);
	}

	public void spawnClouds(World world, BoundingBox chunkBounds, double yOffset, int duration) {
		Vector3DDouble center = chunkBounds.getMinVector().midpoint(chunkBounds.getMaxVector());
		for (int i = 0; i < 32; i += 2) {
			spawnCloud(world, chunkBounds, center, yOffset + i, duration);
		}
	}

	public void spawnCloud(World world, BoundingBox chunkBounds, Vector3DDouble center, double y, int duration) {
		Vector3DDouble vector = new Vector3DDouble(center.getX(), y, center.getZ());
		final AreaEffectCloud effectCloud = world.spawn(vector.toBukkitLocation(world), AreaEffectCloud.class);
		effectCloud.setRadius(8);// 16/2
		effectCloud.setDuration(duration * 20);
		effectCloud.setDurationOnUse(0);
		effectCloud.setWaitTime(duration * 20);
		effectCloud.setParticle(Particle.ITEM_TAKE);// Should be invisible

		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				effectCloud.remove();
			}
		}, duration * 20);
	}

}
