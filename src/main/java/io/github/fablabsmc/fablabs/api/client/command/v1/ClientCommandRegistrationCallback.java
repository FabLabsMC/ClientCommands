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

package io.github.fablabsmc.fablabs.api.client.command.v1;

import com.mojang.brigadier.CommandDispatcher;
import io.github.fablabsmc.fablabs.impl.command.ClientCommandInternals;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;

/**
 * Callback for registering client-sided commands.
 *
 * <p>Client-sided commands are fully executed on the client,
 * so players can use them in both singleplayer and multiplayer.
 *
 * <p>Client-sided commands also support using custom command
 * prefixes instead of the {@code /} symbol. You can customize
 * the used prefix with {@link #event(char)}.
 *
 * <p>The commands are run on the client game thread by default.
 * Avoid doing any heavy calculations here as that can freeze the game's rendering.
 * For example, you can move heavy code to another thread.
 *
 * <h2>Example</h2>
 * <pre>
 * {@code
 * ClientCommandRegistrationCallback.event().register(dispatcher -> {
 *     dispatcher.register(ArgumentBuilders.literal("hello").executes(context -> {
 *         context.getSource().sendFeedback(new LiteralText("Hello, world!"));
 *         return 0;
 *     }));
 * });
 * }
 * </pre>
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ClientCommandRegistrationCallback {
	/**
	 * Gets the command registration event for commands with the {@code /} prefix.
	 *
	 * @return the event object
	 */
	static Event<ClientCommandRegistrationCallback> event() {
		return event('/');
	}

	/**
	 * Gets the command registration event with a custom command prefix.
	 *
	 * @param prefix the command prefix
	 * @return the event object
	 * @throws IllegalArgumentException if the prefix {@linkplain Character#isLetterOrDigit(char) is a letter or a digit},
	 *                                  or if it {@linkplain Character#isWhitespace(char) is whitespace}.
	 */
	static Event<ClientCommandRegistrationCallback> event(char prefix) {
		return ClientCommandInternals.event(prefix);
	}

	/**
	 * Called when a client-side command dispatcher is registering commands.
	 *
	 * @param dispatcher the command dispatcher to register commands to
	 */
	void register(CommandDispatcher<FabricClientCommandSource> dispatcher);
}
