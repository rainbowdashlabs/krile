package de.chojo.krile.commands.discover.handler.repositories;

import de.chojo.jdautil.configuratino.Configuration;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.pagination.bag.ListPageBag;
import de.chojo.jdautil.util.Choice;
import de.chojo.jdautil.util.Completion;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.krile.configuration.ConfigFile;
import de.chojo.krile.configuration.elements.RepositoryLocation;
import de.chojo.krile.data.access.RepositoryData;
import de.chojo.krile.data.dao.Repository;
import de.chojo.krile.data.util.RepositoryFilter;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchRepo implements SlashHandler {
    private final Configuration<ConfigFile> configuration;
    private final RepositoryData repositoryData;

    public SearchRepo(Configuration<ConfigFile> configuration, RepositoryData repositoryData) {
        this.configuration = configuration;
        this.repositoryData = repositoryData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        Integer category = event.getOption("category", null, OptionMapping::getAsInt);
        String language = event.getOption("language", null, OptionMapping::getAsString);
        String name = event.getOption("name", null, OptionMapping::getAsString);
        String platform = event.getOption("platform", null, OptionMapping::getAsString);
        String user = event.getOption("user", null, OptionMapping::getAsString);
        String repo = event.getOption("repo", null, OptionMapping::getAsString);
        Integer tags = event.getOption("tags", null, OptionMapping::getAsInt);

        List<Repository> search = repositoryData.search(new RepositoryFilter(category, language, name, platform, user, repo, tags));
        var page = new ListPageBag<>(search) {
            @Override
            public CompletableFuture<MessageEditData> buildPage() {
                return CompletableFuture.completedFuture(MessageEditData.fromEmbeds(currentElement().infoEmbed(context)));
            }
        };
        context.registerPage(page, true);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event, EventContext context) {
        AutoCompleteQuery option = event.getFocusedOption();


        if (option.getName().equals("category")) {
            event.replyChoices(repositoryData.completeCategories(option.getValue()).stream().map(e -> new Command.Choice(e.category(), e.id())).toList()).queue();
            return;
        }

        if (option.getName().equals("language")) {
            event.replyChoices(Choice.toStringChoice(repositoryData.completeLanguage(option.getValue()))).queue();
            return;
        }

        if (option.getName().equals("platform")) {
            List<Command.Choice> complete = Completion.complete(option.getValue(), configuration.config().repositories().repositories().stream().map(RepositoryLocation::name));
            event.replyChoices(complete).queue();
            return;
        }

        if (option.getName().equals("user")) {
            event.replyChoices(Choice.toStringChoice(repositoryData.completeName(option.getValue()))).queue();
            return;
        }

        if (option.getName().equals("repo")) {
            event.replyChoices(Choice.toStringChoice(repositoryData.completeRepo(option.getValue()))).queue();
            return;
        }
    }
}
