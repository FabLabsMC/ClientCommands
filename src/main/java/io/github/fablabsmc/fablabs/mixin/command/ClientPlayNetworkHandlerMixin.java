package io.github.fablabsmc.fablabs.mixin.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.fablabsmc.fablabs.impl.command.ClientCommandInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.server.command.CommandSource;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	private CommandDispatcher<CommandSource> commandDispatcher;

	@Inject(method = "onCommandTree", at = @At("RETURN"))
	private void onOnCommandTree(CommandTreeS2CPacket packet, CallbackInfo info) {
		ClientCommandInternals.addCommands('/', commandDispatcher);
	}
}
