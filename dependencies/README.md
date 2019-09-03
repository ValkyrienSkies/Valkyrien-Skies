# If you're wondering the reason why we need a dependencies folder...

## PicoCLI

Forge is dumb and it's classloader will try to load the `package-info.class`
in the `java9` folder in the package `META-INF` even though it's not supposed to,
and ruins everything. So I had to download this jar and manually delete that folder.