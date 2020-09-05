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

package io.github.fablabsmc.fablabs.impl.command;

import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.fablabsmc.fablabs.api.client.command.v1.ClientCommandRegistrationCallback;
import io.github.fablabsmc.fablabs.api.client.command.v1.FabricClientCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public final class ClientCommandInternals {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final Map<Character, Event<ClientCommandRegistrationCallback>> events = new HashMap<>();
	private static final Map<Character, CommandDispatcher<FabricClientCommandSource>> dispatchers = new HashMap<>();

	public static Event<ClientCommandRegistrationCallback> event(char prefix) {
		if (isInvalidCommandPrefix(prefix)) {
			throw new IllegalArgumentException("Command prefix '" + prefix + "' cannot be a letter, a digit or whitespace!");
		}

		return events.computeIfAbsent(prefix, c -> EventFactory.createArrayBacked(ClientCommandRegistrationCallback.class, callbacks -> dispatcher -> {
			for (ClientCommandRegistrationCallback callback : callbacks) {
				callback.register(dispatcher);
			}
		}));
	}

	/**
	 * Executes a client-sided command from a message.
	 *
	 * @param message the command message
	 * @return true if the message should not be sent to the server, false otherwise
	 */
	public static boolean executeCommand(String message) {
		if (message.isEmpty()) {
			return false; // Nothing to process
		}

		char prefix = message.charAt(0);
		CommandDispatcher<FabricClientCommandSource> dispatcher = getDispatcher(prefix);

		if (dispatcher == null) {
			return false; // Unknown prefix, won't execute anything.
		}

		MinecraftClient client = MinecraftClient.getInstance();

		// The interface is implemented on ClientCommandSource with a mixin
		FabricClientCommandSource commandSource = (FabricClientCommandSource) client.getNetworkHandler().getCommandSource();

		try {
			dispatcher.execute(message.substring(1), commandSource);
			return true;
		} catch (CommandSyntaxException e) {
			// Allow the message be sent to the server since it's not a valid command.
			// TODO: Should this be tweaked?
			return false;
		} catch (CommandException e) {
			LOGGER.warn("Error while executing client-sided command", e);
			commandSource.sendError(e.getTextMessage());
			return true;
		} catch (RuntimeException e) {
			LOGGER.warn("Error while executing client-sided command", e);
			commandSource.sendError(Text.of(e.getMessage()));
			return true;
		}
	}

	/* @Nullable */
	public static CommandDispatcher<FabricClientCommandSource> getDispatcher(char prefix) {
		// TODO: Find a place to build these ahead of time
		return dispatchers.computeIfAbsent(prefix, c -> {
			Event<ClientCommandRegistrationCallback> event = events.get(prefix);

			if (event == null) {
				return null;
			}

			CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
			addCommands(prefix, dispatcher);
			return dispatcher;
		});
	}

	public static void addCommands(char prefix, CommandDispatcher<FabricClientCommandSource> dispatcher) {
		Event<ClientCommandRegistrationCallback> event = events.get(prefix);

		if (event != null) {
			event.invoker().register(dispatcher);
		}
	}

	public static boolean isInvalidCommandPrefix(char prefix) {
		return Character.isLetterOrDigit(prefix) || Character.isWhitespace(prefix);
	}

	public static boolean isPrefixUsed(char prefix) {
		return events.containsKey(prefix);
	}
}
