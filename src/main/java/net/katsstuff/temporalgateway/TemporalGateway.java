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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.Plugin;

import net.katsstuff.temporalgateway.handler.SnapshotHandler;
import net.katsstuff.temporalgateway.lib.LibMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Plugin(id = LibMod.ID, name = LibMod.NAME)
public class TemporalGateway {

	@Mod.Instance(LibMod.ID)
	public static TemporalGateway instance;
	private final SnapshotHandler snapshotHandler = new SnapshotHandler();

	@Mod.EventHandler
	public void onFMLPreInitialization(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(snapshotHandler);
		Sponge.getEventManager().registerListeners(this, snapshotHandler);
	}
}
