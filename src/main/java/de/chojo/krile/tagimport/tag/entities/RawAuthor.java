/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.tagimport.tag.entities;

import org.eclipse.jgit.lib.PersonIdent;

public record RawAuthor(String name, String mail) {
    public static RawAuthor of(PersonIdent authorIdent) {
        return new RawAuthor(authorIdent.getName(), authorIdent.getEmailAddress());
    }
}
