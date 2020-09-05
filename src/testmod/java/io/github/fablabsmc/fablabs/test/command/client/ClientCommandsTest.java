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

package io.github.fablabsmc.fablabs.test.command.client;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.fablabsmc.fablabs.api.client.command.v1.ArgumentBuilders;
import io.github.fablabsmc.fablabs.api.client.command.v1.ClientCommandRegistrationCallback;

import net.minecraft.text.LiteralText;

import net.fabricmc.api.ClientModInitializer;

public final class ClientCommandsTest implements ClientModInitializer {
	private static final DynamicCommandExceptionType IS_NULL = new DynamicCommandExceptionType(x -> new LiteralText("The " + x + " is null"));

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.event().register(registerSimpleCommands("This is a client command"));
		// Using `\` as prefix
		ClientCommandRegistrationCallback.event('\\').register(registerSimpleCommands("This is a client command with backslashes"));
	}

	private static ClientCommandRegistrationCallback registerSimpleCommands(String message) {
		return dispatcher -> {
			dispatcher.register(ArgumentBuilders.literal("test-client-cmd").executes(context -> {
				context.getSource().sendFeedback(new LiteralText(message));

				if (context.getSource().getClient() == null) {
					throw IS_NULL.create("client");
				}

				if (context.getSource().getWorld() == null) {
					throw IS_NULL.create("world");
				}

				if (context.getSource().getPlayer() == null) {
					throw IS_NULL.create("player");
				}

				return 0;
			}));
		};
	}
}
