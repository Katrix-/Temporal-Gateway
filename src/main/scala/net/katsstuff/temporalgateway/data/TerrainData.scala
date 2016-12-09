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

import java.util.Optional

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.spongepowered.api.block.{BlockSnapshot => SpongeBlockSnapshot}

import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.util.BlockSnapshot

/**
	* General terrain data.
	* The tag data in the map can be null to avoid extra objects being created
	*/
case class TerrainData(
		blocks: mutable.Map[(Int, BlockPos), BlockSnapshot]
) extends Rollback {

	def addBlock(world: World, pos: BlockPos, state: IBlockState): Unit = {
		val dimPos = (world.provider.getDimension, pos)
		if(!blocks.contains(dimPos)) {
			blocks.put(dimPos, new BlockSnapshot(world, pos, state))
		}
	}

	def addBlock(snapshot: BlockSnapshot): Unit = {
		val dimPos = (snapshot.getDimId, snapshot.getPos)
		if(!blocks.contains(dimPos)) {
			blocks.put(dimPos, snapshot)
		}
	}

	def addBlock(snapshot: SpongeBlockSnapshot): Unit = {
		val optDimPos: Optional[(Int, BlockPos)] = snapshot.getLocation.filter(loc => loc.getExtent.isInstanceOf[World]).map(loc => {
			val dim = loc.getExtent.asInstanceOf[World].provider.getDimension
			val pos = new BlockPos(loc.getBlockX, loc.getBlockY, loc.getBlockZ)
			(dim, pos)
		})

		if(optDimPos.isPresent && blocks.contains(optDimPos.get())) {
			val dimPos = optDimPos.get()
			val world = snapshot.getLocation.get().getExtent.asInstanceOf[World]


			blocks.put(dimPos, new BlockSnapshot(world, dimPos._2, world.getBlockState(dimPos._2)))
		}
	}

	def addBlock(world: World, pos: BlockPos): Unit = addBlock(world, pos, world.getBlockState(pos))

	override def rollBack(): Unit = {
		for((_, snapShot) <- blocks) {
			snapShot.restore(true)
		}
	}

	override def weight: Int = blocks.size / 100

	override def toNBT: NBTTagCompound = {
		val tag = new NBTTagCompound

		for(((dim, pos), snapshot) <- blocks ) {
			val dimTag = tag.getCompoundTag(dim.toString)
			val snapShotNBT = new NBTTagCompound
			snapshot.writeToNBT(snapShotNBT)

			dimTag.setTag(pos.toLong.toString, snapShotNBT)

			tag.setTag(dim.toString, dimTag)
		}

		tag
	}
}
object TerrainData {

	def populateMapForChunk(map: mutable.Map[(Int, BlockPos), BlockSnapshot], chunk: Chunk): Unit = {
		val tilEntities = chunk.getTileEntityMap.asScala
		val world = chunk.getWorld
		val dim = world.provider.getDimension

		for((pos, _) <- tilEntities) {
			val state = chunk.getBlockState(pos)
			val dimPos = (dim, pos)
			if(!map.contains(dimPos)) {
				map.put(dimPos, new BlockSnapshot(world, pos, state))
			}
		}
	}

	def populateMapForWorld(map: mutable.Map[(Int, BlockPos), BlockSnapshot], world: World): Unit = {
		val tileEntityList = world.loadedTileEntityList.asScala
		val dim = world.provider.getDimension
		for(tileEntity <- tileEntityList) {
			val pos = tileEntity.getPos
			val dimPos = (dim, pos)
			if(!map.contains(dimPos)) {
				val state = world.getBlockState(pos)
				map.put(dimPos, new BlockSnapshot(world, pos, state))
			}
		}
	}

	def fromNBT(tag: NBTTagCompound): TerrainData = {
		val dimKeys = tag.getKeySet.asScala
		val res = for(dimString <- dimKeys) yield {
			val dim = dimString.toInt
			val dimTag = tag.getCompoundTag(dimString)

			val posKeys = dimTag.getKeySet.asScala
			for(posString <- posKeys) yield {
				val pos = BlockPos.fromLong(posString.toLong)
				val snapshotTag = dimTag.getCompoundTag(posString)

				(dim, pos) -> BlockSnapshot.readFromNBT(snapshotTag)
			}
		}

		TerrainData(mutable.Map(res.flatten.toSeq: _*))
	}
}