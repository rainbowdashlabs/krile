# Add a repository

There are multiple ways to add a repository.

## Importing a repository

To import a repository use the `/repositories add` command. 
You can either import is by an url or by defining the repository and user which owns it.
Repositories also have a [unique identifier](#identifier), which allows you to import them.
For example this repository has the identifier `github:rainbowdashlabs/krile`.

Once you execute the command the repository will be imported if not already done.
After that the tags will be available on the server.

## Identifier

The identifier is probably the easiest way to import a repository.
It is a unique way to point to a repository and sub repository.

For the general repository it looks like this:  
`platform:username/repository`

For sub repositories you just need to add the directory the repository is located in.
`plaform:username/repository`

Ideally repository owners provide this identifier somewhere in their readme, to make it easier to import it.
