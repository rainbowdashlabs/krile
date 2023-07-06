package de.chojo.krile.commands.tag.handler;

import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.menus.MenuAction;
import de.chojo.jdautil.menus.MenuActionBuilder;
import de.chojo.jdautil.menus.entries.MenuEntry;
import de.chojo.jdautil.pagination.bag.PageButton;
import de.chojo.jdautil.pagination.bag.PrivateListPageBag;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.data.access.GuildData;
import de.chojo.krile.data.dao.TagGuild;
import de.chojo.krile.data.dao.repository.tags.Tag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class Show implements SlashHandler {
    public static final Button delete = Button.danger("delete", "delete").withEmoji(Emoji.fromUnicode("🗑️"));
    public static final Button info = Button.primary("info", "info").withEmoji(Emoji.fromUnicode("❓"));
    private final GuildData guilds;
    private static final Logger log = getLogger(Show.class);

    public Show(GuildData guilds) {
        this.guilds = guilds;
    }

    private static List<PageButton> buttons(Tag tag, EventContext context, boolean delete) {
        // not really a nice solution
        if (delete) {
            return List.of(
                    PageButton.of(
                            page -> Show.delete,
                            (page, event) -> {
                                log.trace("Deleting post");
                                event.getMessage().delete().complete();
                            }),
                    PageButton.of(
                            page -> info,
                            (page, button) -> button.replyEmbeds(tag.infoEmbed(context)).setEphemeral(true).queue()));
        }
        return List.of(
                PageButton.of(
                        page -> info,
                        (page, button) -> button.replyEmbeds(tag.infoEmbed(context)).setEphemeral(true).queue()));
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Integer id = event.getOption("tag", OptionMapping::getAsInt);
        event.deferReply().queue();
        Optional<Tag> byId = guilds.guild(event.getGuild()).tags().getById(id);
        if (byId.isEmpty()) {
            event.reply("Invalid tag").setEphemeral(true).queue();
            return;
        }
        Tag tag = byId.get();
        showTag(event, context, guilds.guild(event), tag);
    }

    public static void showTag(SlashCommandInteractionEvent event, EventContext context, TagGuild tagGuild, Tag tag) {
        tagGuild.tags().used(tag);
        showTag(event, context, tag);
    }

    public static void showTag(SlashCommandInteractionEvent event, EventContext context, Tag tag) {
        boolean view = event.getGuild().getSelfMember().hasPermission(event.getChannel().asGuildMessageChannel(), Permission.VIEW_CHANNEL);
        if (tag.isPaged()) {
            context.registerPage(new PrivateListPageBag<>(tag.paged(), event.getUser().getIdLong()) {
                @Override
                public CompletableFuture<MessageEditData> buildPage() {
                    MessageEditBuilder builder = new MessageEditBuilder().setContent(currentElement());
                    tag.meta().image().ifPresent(image -> builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
                    return CompletableFuture.completedFuture(builder.build());
                }

                @Override
                public List<PageButton> buttons() {
                    return Show.buttons(tag, context, view);
                }
            });
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder().setContent(tag.text());
        tag.meta().image().ifPresent(image -> builder.setEmbeds(new EmbedBuilder().setImage(image).build()));
        MenuActionBuilder menu = MenuAction.forCallback(builder.build(), event);
        if (view) {
            menu.addComponent(MenuEntry.of(delete, ctx -> ctx.event().getMessage().delete().queue()));
        }
        menu.addComponent(MenuEntry.of(info, ctx -> ctx.event().replyEmbeds(tag.infoEmbed(context)).setEphemeral(true).queue()));
        context.registerMenu(menu.build());
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        List<Command.Choice> choices = guilds.guild(event).tags()
                .complete(event.getFocusedOption().getValue())
                .stream()
                .map(e -> new Command.Choice(e.name(), e.id()))
                .toList();
        event.replyChoices(choices).queue();
    }
}
