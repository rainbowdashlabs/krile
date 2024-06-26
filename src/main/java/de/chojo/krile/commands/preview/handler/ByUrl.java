/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.commands.preview.handler;

import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ByUrl extends PreviewBase {
    private final HttpClient client = HttpClient.newBuilder().build();

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        String url = event.getOption("url", OptionMapping::getAsString);
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            event.reply(context.localize("error.url.invalidsource")).setEphemeral(true).queue();
            return;
        }
        HttpRequest build = HttpRequest.newBuilder(uri).GET().build();

        try {
            HttpResponse<String> send = client.sendAsync(build, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).orTimeout(5, TimeUnit.SECONDS).join();
            String body = send.body();
            showTag(body, event, context);
        } catch (Exception e) {
            event.reply(context.localize("error.repository.import")).setEphemeral(true).queue();
        }
    }
}
