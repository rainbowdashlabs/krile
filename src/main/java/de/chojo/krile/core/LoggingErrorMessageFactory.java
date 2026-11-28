/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.core;

import de.chojo.logutil.marker.LogNotify;
import io.github.kaktushose.jdac.embeds.error.ErrorMessageFactory;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Reports interactions that failed with an exception to the bot admins and leaves the reply to the user to another
 * factory.
 */
public class LoggingErrorMessageFactory implements ErrorMessageFactory {
    private static final Logger log = getLogger(LoggingErrorMessageFactory.class);
    private final ErrorMessageFactory delegate;

    public LoggingErrorMessageFactory(ErrorMessageFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public MessageTopLevelComponent getTypeAdaptingFailedMessage(
            ErrorContext context, ConversionResult.Failure<?> failure) {
        return delegate.getTypeAdaptingFailedMessage(context, failure);
    }

    @Override
    public MessageTopLevelComponent getInsufficientPermissionsMessage(ErrorContext context) {
        return delegate.getInsufficientPermissionsMessage(context);
    }

    @Override
    public MessageTopLevelComponent getConstraintFailedMessage(ErrorContext context, String message) {
        return delegate.getConstraintFailedMessage(context, message);
    }

    @Override
    public MessageTopLevelComponent getInteractionExecutionFailedMessage(ErrorContext context, Throwable exception) {
        log.error(
                LogNotify.NOTIFY_ADMIN,
                "Interaction {} failed",
                context.definition().displayName(),
                exception);
        return delegate.getInteractionExecutionFailedMessage(context, exception);
    }

    @Override
    public MessageTopLevelComponent getTimedOutComponentMessage(GenericInteractionCreateEvent event) {
        return delegate.getTimedOutComponentMessage(event);
    }
}
