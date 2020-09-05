/*
 * Copyright 2020 FabLabsMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.fablabsmc.fablabs.mixin.command;

import io.github.fablabsmc.fablabs.api.client.command.v1.FabricClientCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

@Mixin(ClientCommandSource.class)
abstract class ClientCommandSourceMixin implements FabricClientCommandSource {
	@Shadow
	@Final
	private MinecraftClient client;

	@Override
	public void sendFeedback(Text message) {
		client.inGameHud.addChatMessage(MessageType.SYSTEM, message, Util.NIL_UUID);
	}

	@Override
	public void sendError(Text message) {
		client.inGameHud.addChatMessage(MessageType.SYSTEM, message.copy().formatted(Formatting.RED), Util.NIL_UUID);
	}

	@Override
	public MinecraftClient getClient() {
		return client;
	}

	@Override
	public ClientPlayerEntity getPlayer() {
		return client.player;
	}

	@Override
	public ClientWorld getWorld() {
		return client.world;
	}
}
