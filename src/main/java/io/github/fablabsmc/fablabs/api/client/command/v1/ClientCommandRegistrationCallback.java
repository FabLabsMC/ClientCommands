package io.github.fablabsmc.fablabs.api.client.command.v1;

import com.mojang.brigadier.CommandDispatcher;
import io.github.fablabsmc.fablabs.impl.command.ClientCommandInternals;

import net.minecraft.server.command.CommandSource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ClientCommandRegistrationCallback {
	static Event<ClientCommandRegistrationCallback> event() {
		return event('/');
	}

	static Event<ClientCommandRegistrationCallback> event(char prefix) {
		return ClientCommandInternals.event(prefix);
	}

	void register(CommandDispatcher<CommandSource> dispatcher);
}
