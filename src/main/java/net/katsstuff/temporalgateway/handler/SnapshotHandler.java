/*
 * This file is part of Temporal Gateway, licensed under the MIT License (MIT).
 *
 * Copyright (c) Katrix 2016
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.katsstuff.temporalgateway.handler;

import java.util.List;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import net.katsstuff.temporalgateway.data.SnapshotData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SnapshotHandler {

	private static final int MAX_WEIGHT = 1000;
	private static final long MAX_DURATION = 12_000;
	private static final float MAX_MOVEMENT = 1F;

	private SnapshotData lastSnapshot = null;
	private long lastSnapshotTime;
	private boolean shouldCreateNewSnapshot = false;

	public void forceNewSnapshot(boolean force) {
		shouldCreateNewSnapshot = force;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onTickServerTick(TickEvent.ServerTickEvent event) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		WorldServer overWorld = server.worldServerForDimension(0);
		long time = overWorld.getWorldInfo().getWorldTotalTime();

		if(timeForNewSnapshot(time)) {
			List<EntityPlayerMP> players = server.getPlayerList().getPlayers();

			long targeting = players.stream().mapToLong(
					player -> player.world.getEntitiesInAABBexcluding(player, player.getEntityBoundingBox().expandXyz(16D),
							e -> e instanceof EntityLiving && ((EntityLiving)e).getAttackTarget() == player).size()).sum();

			if(targeting == 0 && players.stream().allMatch(this::playerNotMoving)) {
				lastSnapshot = SnapshotData.apply();
				shouldCreateNewSnapshot = false;
				lastSnapshotTime = time;

				Style style = new Style().setColor(TextFormatting.GRAY).setBold(true);
				ITextComponent text = new TextComponentString("New snapshot set").setStyle(style);
				players.forEach(p -> p.sendStatusMessage(text, true));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event) {
		if(lastSnapshot != null && event.getEntityLiving() instanceof EntityPlayer) {
			event.setCanceled(true);
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			lastSnapshot.rollBack();

			Style style = new Style().setColor(TextFormatting.GRAY).setBold(true);
			ITextComponent text = new TextComponentString("New snapshot set").setStyle(style);
			player.sendStatusMessage(text, true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWorldLoad(WorldEvent.Load event) {
		if(lastSnapshot != null) {
			lastSnapshot.addWorld(event.getWorld());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onChunkLoad(ChunkEvent.Load event) {
		if(lastSnapshot != null) {
			lastSnapshot.addChunk(event.getChunk());
		}
	}

	@Listener(order = Order.POST)
	public void onChangeBlock(ChangeBlockEvent event) {
		if(lastSnapshot != null) {
			List<Transaction<org.spongepowered.api.block.BlockSnapshot>> transactions = event.getTransactions();
			for(Transaction<org.spongepowered.api.block.BlockSnapshot> transaction : transactions) {
				lastSnapshot.terrainData().addBlock(transaction.getOriginal());
			}
		}
	}

	private boolean playerNotMoving(EntityPlayer player) {
		return player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ < MAX_MOVEMENT;
	}

	private boolean timeForNewSnapshot(long currentTime) {
		return shouldCreateNewSnapshot || (lastSnapshot != null && (lastSnapshot.weight() > MAX_WEIGHT
				|| currentTime - lastSnapshotTime > MAX_DURATION));
	}
}
