# Framework implementation

This directory contains Mork's dashboard implementation: components, stores, services, API models,
utilities, configuration, and tests. All source is editable. Internal APIs, file locations, and DOM/CSS
structure may change in any Mork version; users are responsible for merging their modifications.

For supported customization, put project code in [`../custom/`](../custom/README.md) and import the
documented [`../extension-api/public-api.ts`](../extension-api/public-api.ts) contracts. Importing or
modifying framework internals is allowed, but does not carry those compatibility guarantees.

Framework components must receive custom registrations through dependency injection. The integration
point is `src/main.ts`; framework implementation and extension contracts do not import project code.

See the [ownership policy](../../README.md#ownership-and-upgrades) and [migration guide](../../MIGRATIONS.md).
