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

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler

case class WorldData(
		dim: Int,
		time: Long,
		cleanWeatherTime: Int,
		rainTime: Int,
		thunderTime: Int,
		isRaining: Boolean,
		isThundering: Boolean
) extends Rollback {

	def this(world: World) {
		this(
			world.provider.getDimension,
			world.getWorldTime,
			world.getWorldInfo.getCleanWeatherTime,
			world.getWorldInfo.getRainTime,
			world.getWorldInfo.getThunderTime,
			world.getWorldInfo.isRaining,
			world.getWorldInfo.isThundering
		)
	}

	override def rollBack(): Unit = {
		val world = FMLCommonHandler.instance().getMinecraftServerInstance.worldServerForDimension(dim)
		val info = world.getWorldInfo

		world.setWorldTime(time)
		info.setCleanWeatherTime(cleanWeatherTime)
		info.setRainTime(rainTime)
		info.setThunderTime(thunderTime)
		info.setRaining(isRaining)
		info.setThundering(isThundering)
	}
	override def weight: Int = 1
	override def toNBT: NBTTagCompound = {
		val tag = new NBTTagCompound
		tag.setInteger("dimension", dim)
		tag.setLong("time", time)
		tag.setInteger("cleanWeatherTime", cleanWeatherTime)
		tag.setInteger("rainTime", rainTime)
		tag.setInteger("thunderTime", thunderTime)
		tag.setBoolean("isRaining", isRaining)
		tag.setBoolean("isThundering", isThundering)
		tag
	}
}
object WorldData {

	def fromNBT(tag: NBTTagCompound): WorldData = {
		val dim = tag.getInteger("dimension")
		val time = tag.getLong("time")
		val cleanWeatherTime = tag.getInteger("cleanWeatherTime")
		val rainTime = tag.getInteger("rainTime")
		val thunderTime = tag.getInteger("thunderTime")
		val isRaining = tag.getBoolean("isRaining")
		val isThundering = tag.getBoolean("isThundering")

		WorldData(dim, time, cleanWeatherTime, rainTime, thunderTime, isRaining, isThundering)
	}
}
