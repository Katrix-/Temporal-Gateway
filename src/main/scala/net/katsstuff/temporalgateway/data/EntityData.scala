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

import java.util.UUID

import scala.collection.JavaConverters._
import scala.collection.mutable

import net.minecraft.entity.EntityList
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.FMLCommonHandler

case class EntityData(
		entities: mutable.Map[(Int, UUID), NBTTagCompound]
) extends Rollback {

	override def rollBack(): Unit = {
		val server = FMLCommonHandler.instance().getMinecraftServerInstance

		for(((dim, uuid), tag) <- entities) {
			val world = server.worldServerForDimension(dim)
			val entity = world.getEntityFromUuid(uuid)
			if(entity != null) {
				entity.deserializeNBT(tag)
			}
			else {
				EntityList.createEntityFromNBT(tag, world)
			}
		}
	}

	override def weight: Int = entities.size / 100

	override def toNBT: NBTTagCompound = {
		val tag = new NBTTagCompound
	}
}
object EntityData {

	def apply(): EntityData = {
		val map = new mutable.HashMap[(Int, UUID), NBTTagCompound]()
		val worlds = DimensionManager.getWorlds

		for {
			world <- worlds
			entity <- world.getLoadedEntityList.asScala
			if !entity.isInstanceOf[EntityPlayer]
		} {
			map.put((world.provider.getDimension, entity.getUniqueID), entity.serializeNBT())
		}

		EntityData(map)
	}

	def populateMapForChunk(map: mutable.Map[(Int, UUID), NBTTagCompound], chunk: Chunk): Unit = {
		val entities = chunk.getEntityLists.toIterator.flatMap(_.iterator().asScala)
		val world = chunk.getWorld
		val dim = world.provider.getDimension

		for(entity <- entities if !entity.isInstanceOf[EntityPlayer]) {
			val dimId = (dim, entity.getUniqueID)
			if(!map.contains(dimId)) {
				map.put(dimId, entity.serializeNBT())
			}
		}
	}

	def populateMapForWorld(map: mutable.Map[(Int, UUID), NBTTagCompound], world: World): Unit = {
		val entities = world.loadedEntityList.asScala
		val dim = world.provider.getDimension

		for(entity <- entities if !entity.isInstanceOf[EntityPlayer]) {
			val dimId = (dim, entity.getUniqueID)
			if(!map.contains(dimId)) {
				map.put(dimId, entity.serializeNBT())
			}
		}
	}
}