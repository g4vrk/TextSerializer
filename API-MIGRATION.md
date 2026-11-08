# Resolver API migration

The resolver overload requires MiniMessage 4.10 or newer. The legacy distribution
now uses 4.10 and keeps MiniMessage in its canonical package: relocating the public
TagResolver parameter would make callers using the standard API binary-incompatible.
Consumers of the old shaded API must rebuild. Validate supported Paper versions
before releasing this change; this is a breaking distribution change.

Use universalSerializer().serialize(source, resolvers) for mixed legacy/MiniMessage
input. Resolver results bypass source-only caches. Use Placeholder.unparsed or
Placeholder.component for untrusted values; parsed placeholders deliberately allow
markup and must only receive trusted content. Existing no-resolver calls retain
their bounded caches. A standalone or trailing legacy reset is interpreted as a
reset rather than visible text. Other formatting-only sequences retain their
previous behavior.

Custom MiniMessage configurations can supply both parsing functions to
MiniMessageTextSerializer. The one-function constructor uses the standard
MiniMessage parser for resolver calls.

Validation: full Gradle build and all 158 tests pass under JDK 21, including the
legacy and native modules. Live cross-version server compatibility has not been
verified. The common module is exported as an API dependency; GROUP and VERSION
publication overrides also govern project dependency coordinates.
