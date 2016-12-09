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

import net.katsstuff.temporalgateway.helper.LogHelper
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.{NBTTagCompound, NBTTagDouble, NBTTagList}
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameType
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.FMLCommonHandler

case class PlayerData(
		uuid: UUID,
		pos: Vec3d,
		yaw: Float,
		pitch: Float,
		gameMode: GameType,
		data: NBTTagCompound
) extends Rollback {

	def this(player: EntityPlayerMP) {
		this(player.getUniqueID, player.getPositionVector, player.rotationYaw, player.rotationPitch, player.interactionManager.getGameType,
			player.writeToNBT(new NBTTagCompound))
	}

	override def rollBack(): Unit = {
		val server = FMLCommonHandler.instance().getMinecraftServerInstance
		val player = server.getPlayerList.getPlayerByUUID(uuid)

		if(player == null) {
			LogHelper.error(s"Could not find player for $uuid")
			???
		}

		player.setLocationAndAngles(pos.xCoord, pos.yCoord, pos.zCoord, yaw, pitch)
		player.setPositionAndUpdate(pos.xCoord, pos.yCoord, pos.zCoord)
		player.readFromNBT(data)
	}

	override def weight: Int = 1

	override def toNBT: NBTTagCompound = {
		val tag = new NBTTagCompound
		tag.setUniqueId("uuid", uuid)

		val posList = new NBTTagList
		posList.appendTag(new NBTTagDouble(pos.xCoord))
		posList.appendTag(new NBTTagDouble(pos.yCoord))
		posList.appendTag(new NBTTagDouble(pos.zCoord))
		tag.setTag("pos", posList)
		tag.setFloat("yaw", yaw)
		tag.setFloat("pitch", pitch)
		tag.setInteger("gameMode", gameMode.getID)
		tag.setTag("data", data)
		tag
	}
}
object PlayerData {

	def fromNBT(tag: NBTTagCompound): PlayerData = {
		val uuid = tag.getUniqueId("uuid")
		val posList = tag.getTagList("pos", Constants.NBT.TAG_DOUBLE)
		val x = posList.getDoubleAt(0)
		val y = posList.getDoubleAt(1)
		val z = posList.getDoubleAt(2)
		val pos = new Vec3d(x, y, z)
		val yaw = tag.getFloat("yaw")
		val pitch = tag.getFloat("pitch")
		val gameMode = GameType.getByID(tag.getInteger("gameMode"))
		val data = tag.getTag("data").asInstanceOf[NBTTagCompound]

		PlayerData(uuid, pos, yaw, pitch, gameMode, data)
	}
}