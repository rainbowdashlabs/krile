# Create a repository

Repositories contain tags. One git repository can contain multiple Krile repositories.

## Setting up a repository

To enable krile for a repository you need to add a `krile.yml` or `krile.yaml` in your project.
This file can be located at:

- project root
- .github
- .gitlab
- .krile

You can also leave the file empty if you just want the basic functionality.
For the available values see the [repository meta](https://krile.chojo.dev/repositories/meta/) page.

Note: You can also use `krile.json` if you like json more. A schema is also available.
```json
{
  "$schema": "https://raw.githubusercontent.com/rainbowdashlabs/krile/main/.github/repository_schema.json"
}
```

## Setting up a sub repository

If you do not want to directly include all your tags in one repository and want a more fine-grained distribution you can make use of sub repositories.
However, this will only allow users to import your repository via the [identifier](https://krile.chojo.dev/repositories/add/#identifier).
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

