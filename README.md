[![wakatime](https://wakatime.com/badge/user/59659c8f-065c-4750-9d78-132c2e51f4bf/project/84590aa1-1fc9-4672-bfbf-7fc9e2174572.svg)](https://wakatime.com/badge/user/59659c8f-065c-4750-9d78-132c2e51f4bf/project/84590aa1-1fc9-4672-bfbf-7fc9e2174572)

# Krile

Krile is a discord bot to manage public provided tags.
Those tags are provided by git repositories.

Servers can import the repositories they need and make the tags inside them available to their community.

## Importing a repository

To import a repository use the `/repositories add` command. 
You can either import is by an url or by defining the repository and user which owns it.
Repositories also have a unique identifier, which allows you to import them.
For example this repository has the identifier `github:rainbowdashlabs/krile`.

Once you execute the command the repository will be imported if not already done.
After that the tags will be available on the server.

## Setting up a repository

To enable krile for a repository you need to add a `krile.yml` or `krile.yaml` in your project.
This file can be located at:
- project root
- .github
- .krile

This file then may contain the following keys. 
You can also leave the file empty.

```yaml
# The pretty name of the repository. Otherwise, the identifier is used.
name: "My repository"
# The repository description to describe what the tags contain
description: "Cool repository"
# A list of categories, which describe this repository
category: ["tips", "java", "discord"]
# Mark this repository as public.
# This will make it appear in the search.
# People can still import your repository via the identifier or url.
# To appear in the search you also need to define a name, description and set a language
public: true
# Set the language of the repo
language: en
# Change the directory where the tags are located
# Default is root
# This allows you to include your tags in your project instead of an extra repository.
# Our tags are contained in a directory called "tags"
directory: tags
# This is a list of included tags from the defined directory.
# You may only set include or exclude
include: ["included_tag"]
# This is a list of excluded tags from the defined directory.
# You may only set include or exclude
exclude: ["excluded_tag"]
```

Note: You can also use krile.json if you like json more. A schema is available.
```json
{
  "$schema": "https://raw.githubusercontent.com/rainbowdashlabs/krile/main/.github/repository_schema.json",
}
```


## Setting up a tag

Tags in krile are not only tags, but also contain some metadata.
Metadata is set via a file header at the start of a file.
This header is optional, and you can also simply add markdown and call it a day.
Tag files need to be of type .md otherwise they are ignored.

The tag itself supports any kind of markdown, that is supported by [discord](https://support.discord.com/hc/en-us/articles/210298617-Markdown-Text-101-Chat-Formatting-Bold-Italic-Underline-)

```md
---
# Change the tag id.
# By default the tag id will be the file name without .md
# Setting the id here is helpful if you rename the file, but want to stick with the id.
# The id is used for e.g. counting statistics.
id: my tag
# The actual tag name
# By default this will be the id
tag: my cool tag
# A list of aliases this tag will be displayed.
# Aliases have a lower priority than the actual tag name.
# When a conflict arises the tag using the name directly takes precedence.
alias: ["my tag", "another tag"]
# The categories of the tag. This populates the search for tags and is also used in the discovery feature.
category: ["java", "tutorial", "tags"]
# An image which should be displayed for the tag
image: https://krile.dev/my_image.png
---

# Awesome tag

You can use any kind of markdown here supported by [Discord](https://discord.com)
```

Note: You can also use json in the header as well if you like json more. A schema is available.

```json
{
  "$schema": "https://raw.githubusercontent.com/rainbowdashlabs/krile/main/.github/tag_schema.json"
}
```

