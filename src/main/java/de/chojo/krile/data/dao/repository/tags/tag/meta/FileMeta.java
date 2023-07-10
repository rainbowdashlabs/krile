/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.dao.repository.tags.tag.meta;

import de.chojo.krile.data.dao.Author;

import java.time.Instant;

public class FileMeta {
    private final FileEvent created;
    private final FileEvent modified;
    private final String fileName;

    public FileMeta(String fileName, Instant created, Author createdBy, Instant modified, Author modifiedBy) {
        this.fileName = fileName;
        this.created = new FileEvent(created, createdBy);
        this.modified = new FileEvent(modified, modifiedBy);
    }

    public FileEvent created() {
        return created;
    }

    public FileEvent modified() {
        return modified;
    }

    public String fileName() {
        return fileName;
    }

    public record FileEvent(Instant when, Author who) {
    }
}
