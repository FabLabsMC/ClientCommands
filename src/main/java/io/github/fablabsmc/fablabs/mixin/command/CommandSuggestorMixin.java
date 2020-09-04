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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import io.github.fablabsmc.fablabs.impl.command.ClientCommandInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.server.command.CommandSource;

@Mixin(CommandSuggestor.class)
abstract class CommandSuggestorMixin {
	/* @Nullable */
	@Unique
	private CommandDispatcher<CommandSource> currentDispatcher = null;

	@Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void onRefresh(CallbackInfo info, String message, StringReader reader, boolean slash, boolean suggestSlashCommands) {
		if (suggestSlashCommands) {
			return; // The slash prefix is handled separately.
		}

		if (!reader.canRead()) {
			return; // Nothing to suggest here.
		}

		currentDispatcher = ClientCommandInternals.getDispatcher(reader.peek());
	}

	@ModifyVariable(method = "refresh", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;getCommandDispatcher()Lcom/mojang/brigadier/CommandDispatcher;"))
	private CommandDispatcher<CommandSource> modifyCommandDispatcher(CommandDispatcher<CommandSource> existing) {
		if (currentDispatcher != null) {
			return currentDispatcher;
		}

		return existing;
	}

	@Inject(method = "refresh", at = @At("RETURN"))
	private void onRefreshReturn(CallbackInfo info) {
		currentDispatcher = null;
	}
}
