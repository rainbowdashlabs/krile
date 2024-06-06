/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */

package de.chojo.krile.data.base;

import de.chojo.krile.data.dao.repository.tags.tag.meta.TagMeta;

public class BaseMeta {
    protected TagMeta tagMeta;

    public BaseMeta() {
    }

    public BaseMeta(TagMeta tagMeta) {
        this.tagMeta = tagMeta;
    }

    public TagMeta tagMeta() {
            return tagMeta;
        }
}
