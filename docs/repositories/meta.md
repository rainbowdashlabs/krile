# Repository Meta

The repository meta allows you to add additional data to your repository.
It also allows to control the import process to some extent.

```yaml
name: "My repository"
description: "Cool repository"
category: ["tips", "java", "discord"]
public: true
language: en
directory: tags
include: ["included_tag"]
exclude: ["excluded_tag"]
deep: true
```

## Options

### name

**type:** `text`

A pretty name for your repository. If not set the repository identifier will be used instead.

### description

**type:** `text`

A meaningful description what people can expect to find in this repository.

### category

**type:** `text list`

A list of categories of the tags this repository contains

### public

**type:** `boolean`

Mark this repository as public.
This will make it appear in the [discovery](../features/discovery.md).
To appear in the discovery you also need to define a name, description and set a language.

People can still import your repository via the identifier or url even if your repository is not marked as public.

### language

**type:** `text`

Set the language of the repo.
Please use a [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) code like `en`, `fr` or `de`

### directory

**type:** `text`

Change the directory where the tags are located
Default is root
This allows you to include your tags in your project instead of an extra repository.
If this is a [sub repository](create.md#setting-up-a-sub-repository) the path needs to be relative to the repository directory.

### include

**type:** `text list`

This is a list of included tags from the defined directory.
You may only set include or exclude

### exclude

**type:** `text list`

This is a list of excluded tags from the defined directory.
You may only set include or exclude

### deep

**type:** `boolean`

This changes if files in subdirectories of [`directory`](#directory) are processed.
The default is `false`.
