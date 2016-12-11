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
package net.katsstuff.temporalgateway;

import net.katsstuff.temporalgateway.entity.agent.EntityAgentSkeleton;
import net.katsstuff.temporalgateway.helper.LogHelper;
import net.katsstuff.temporalgateway.lib.LibEntityNames;
import net.katsstuff.temporalgateway.lib.LibMod;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {

	private static int entityId;

	public void registerEntityRenderers() {}

	@SubscribeEvent
	public static void onRegistryRegister(RegistryEvent.Register<EntityEntry> event) {
	}

	private static void registerEntity(String name, Class<? extends Entity> clazz) {
		EntityRegistry.registerModEntity(new ResourceLocation(LibMod.ID, name), clazz, name, entityId++, TemporalGateway.instance, 64, 3, false);
	}

	private static void registerEntity(String name, Class<? extends Entity> clazz, int color1, int color2) {
		EntityRegistry.registerModEntity(new ResourceLocation(LibMod.ID, name), clazz, name, entityId++, TemporalGateway.instance, 64, 3, false,
				color1, color2);
	}
}
