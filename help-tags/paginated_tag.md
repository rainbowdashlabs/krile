# Paginated Tag

If your [Markdown tag](https://krile.chojo.dev/tags/markdown/) reaches 2000 characters is will become a paginated tag.
Those tags automatically split up the content into chunks not larger than 2000 characters.
You can control the split yourself by adding a page marker with `<new_page>` to your file.
The bot will first split pages on those markers and afterward in smaller chunks if it is still required.

[Example](https://github.com/rainbowdashlabs/krile/blob/main/tags/long%20tag.md)
