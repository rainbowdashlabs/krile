# Create a repository

Repositories contain tags. One git repository can contain multiple Krile repositories.

## Setting up a repository

To enable krile for a repository you need to add a `krile.yml` or `krile.yaml` in your project.
This file can be located at:

- project root
- .github
- .gitlab
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

Note: You can also use `krile.json` if you like json more. A schema is also available.
```json
{
  "$schema": "https://raw.githubusercontent.com/rainbowdashlabs/krile/main/.github/repository_schema.json"
}
```

## Setting up a sub repository

If you do not want to directly include all your tags in one repository and want a more fine-grained distribution you can make use of sub repositories.
However, this will only allow users to import your repository via the [identifier](add.md#identifier).
To declare a sub repository all you need to do is create a directory with a krile repository configuration in it.
Tags can still be in a subdirectory, however the directory is now resolved from the directory your configuration is in.

There is also an example sub [repository](https://github.com/rainbowdashlabs/krile/tree/main/sub-tags). 
It is located in the `sub-tags` directory.
To import it, we can use the `github:rainbowdashlabs/krile/sub-tags` identifier.

Note that you can still have a krile file at the root of your project as well.

Sub repositories can be helpful when:

- You want to have multiple topics covered in your tag repository
- You want to provide tags in different languages
- You want to allow importing only some of your tags at once.

