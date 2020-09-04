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
