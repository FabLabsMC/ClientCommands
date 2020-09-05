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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.server.command.CommandSource;

@Mixin(CommandSuggestor.class)
abstract class CommandSuggestorMixin {
	// Should be slashOptional, see https://github.com/FabricMC/yarn/issues/1744
	@Shadow
	@Final
	private boolean slashRequired;

	/* @Nullable */
	@Unique
	private CommandDispatcher<CommandSource> currentDispatcher = null;

	@Redirect(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;peek()C", remap = false))
	private char replacePrefix(StringReader reader) {
		char original = reader.peek();

		if (ClientCommandInternals.isPrefixUsed(original)) {
			// Replace any custom prefixes with / so vanilla's check succeeds
			return '/';
		}

		return original;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Inject(method = "refresh", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getText()Ljava/lang/String;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private void checkPrefix(CallbackInfo info, String message) {
		if (message.isEmpty() || slashRequired) {
			// Prefix changes don't have to be done for empty messages
			// or command blocks (where the slash is optional).
			return;
		}

		char prefix = message.charAt(0);

		// Handle non-slash prefixes
		if (prefix != '/') {
			currentDispatcher = (CommandDispatcher) ClientCommandInternals.getDispatcher(prefix);
		}
	}

	/**
	 * Selects the command dispatcher to use for suggestions.
	 */
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
