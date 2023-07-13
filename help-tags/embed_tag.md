# Embed Tag

An embed tag uses a json object to build an embed.
To use an embed tag you need to first set the tag type inside your tag meta to `EMBED`.

```yaml
---
type: EMBED
---
{
  "content": "Cool message",
  "embeds": [...]
}
```

After that you just paste the output of an embed generator like [this one](https://glitchii.github.io/embedbuilder/).

[Example](https://github.com/rainbowdashlabs/krile/blob/main/tags/embed_tag.md)

## Limitations

- All embeds together can not exceed 6000 characters.
- You can not use pagination
- You can not add more than 10 embeds.

