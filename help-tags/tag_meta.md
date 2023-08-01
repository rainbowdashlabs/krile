# Tag Meta

The tag meta is at the top of the file.
It is optional, but provides further configuration of a tag.
It starts and end with `---`.

After that the [content](https://krile.chojo.dev/tags/#content) follows.
If you do not set a meta you can directly start with the content.

A complete meta looks like this:

```yaml
---
id: my tag
tag: my cool tag
alias: ["my tag", "another tag"]
category: ["tutorial", "tags"]
image: https://krile.dev/my_image.png
type: TEXT
---
```

An explanation of the options can be found on the next page.

<new_page>

## Options

### id

**type:** `text`  
**default:** file name without .md

Change the tag id.
By default, the tag id will be the file name without .md
Setting the id here is helpful if you rename the file, but want to stick with the id.
It is not recommended to override this when using the deep option in the `krile.yaml`.
The id is used for e.g. counting statistics.

### tag

**type:** `text`  
**default:** id

The actual tag name.
By default, this will be the id

### alias

**type:** `text list`

A list of aliases this tag will be displayed.
Aliases have a lower priority than the actual tag name.
When a conflict arises the tag using the name directly takes precedence.

### category

**type:** `text list`

The categories of the tag.
This populates the search for tags and is also used in the discovery feature.

### image

**type:** `text`

An image url which should be displayed for the tag.
This will have no effect on embed tags, you need to set your image in the embed in that case.

### type

**type:** `EMBED, TEXT`  
**default:** `TEXT`

The embed type, this type describes the content type of the embed.
Paginated and markdown are both of type text which is the default value.

## JSON

Instead of using the yaml data format you can also use the json data format.

```json
{
  "$schema": "https://raw.githubusercontent.com/rainbowdashlabs/krile/main/.github/tag_schema.json",
  "id": "my tag",
  "tag": "my cool tag",
  "alias": [
    "my tag",
    "another tag"
  ],
  "category": [
    "tutorial",
    "tags"
  ],
  "image": "https://krile.dev/my_image.png",
  "type": "TEXT"
}
```

