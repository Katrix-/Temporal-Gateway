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
package net.katsstuff.temporalgateway.data

import scala.collection.JavaConverters._
import scala.collection.mutable

import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.FMLCommonHandler

case class SnapshotData(
		playerData: mutable.Seq[PlayerData],
		worldData: mutable.Seq[WorldData],
		terrainData: TerrainData,
		entityData: EntityData
) extends Rollback {

	def addChunk(chunk: Chunk): Unit = {
		TerrainData.populateMapForChunk(terrainData.blocks, chunk)
		EntityData.populateMapForChunk(entityData.entities, chunk)
	}

	def addWorld(world: World): Unit = {
		TerrainData.populateMapForWorld(terrainData.blocks, world)
		EntityData.populateMapForWorld(entityData.entities, world)
	}

	override def rollBack(): Unit = {
		terrainData.rollBack()
		entityData.rollBack()
		worldData.foreach(_.rollBack())
		playerData.foreach(_.rollBack())
	}

	override def weight: Int = playerData.map(_.weight).sum + worldData.map(_.weight).sum + terrainData.weight + entityData.weight
	override def toNBT: NBTTagCompound = {
		val tag = new NBTTagCompound

		val playerList = new NBTTagList
		playerData.foreach(p => playerList.appendTag(p.toNBT))
		tag.setTag("player", playerList)

		val worldList = new NBTTagList
		worldData.foreach(w => worldList.appendTag(w.toNBT))
		tag.setTag("world", worldList)

		tag.setTag("terrain", terrainData.toNBT)
		tag.setTag("entity", entityData.toNBT)
		tag
	}
}

object SnapshotData {

	def apply: SnapshotData = {
		val server = FMLCommonHandler.instance().getMinecraftServerInstance

		val playerData = server.getPlayerList.getPlayers.asScala.map(new PlayerData(_))
		val worldData = DimensionManager.getWorlds.map(new WorldData(_))
		val terrainData = TerrainData(mutable.Map())
		val entityData = EntityData()

		SnapshotData(playerData, worldData, terrainData, entityData)
	}

	def fromNBT(tag: NBTTagCompound): SnapshotData = {
		val playerList = tag.getTagList("player", Constants.NBT.TAG_COMPOUND)
		val playerData = for(i <- 0 until playerList.tagCount()) yield {
			val player = playerList.getCompoundTagAt(i)
			PlayerData.fromNBT(player)
		}

		val worldList = tag.getTagList("world", Constants.NBT.TAG_COMPOUND)
		val worldData = for(i <- 0 until worldList.tagCount()) yield {
			val world = worldList.getCompoundTagAt(i)
			WorldData.fromNBT(world)
		}

		SnapshotData(playerData.toBuffer, worldData.toBuffer, ???, ???)
	}
}
