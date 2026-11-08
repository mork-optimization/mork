# Project customization

This directory belongs to your project. Preserve its contents when updating Mork. Everything in the
frontend remains editable, but using the public extension API avoids depending on framework internals.

- `configuration.ts`: register your solution visualizers here. No edits to `app/app.config.ts` are needed.
- `solution-views/`: put standalone Angular visualizer components and their tests here.
- `styles.scss`: project styles, loaded after the framework stylesheet. Prefer component-scoped styles;
  selectors targeting internal dashboard markup may require merging on upgrades.

Use imports from `../extension-api/public-api` (or `../../extension-api/public-api` in `solution-views/`).
The [extension API guide](../extension-api/README.md) includes a complete component and registration example.
Keep browser assets in `public/custom/` if needed; `build:static` recreates the generated static directory,
so do not store source assets there.

When upgrading, review [MIGRATIONS.md](../../MIGRATIONS.md), preserve this directory, and merge any changes
you made to framework/build files. Custom code only needs contract-related changes when a documented
migration applies; Angular/toolchain upgrades may still require dependency or language changes.
