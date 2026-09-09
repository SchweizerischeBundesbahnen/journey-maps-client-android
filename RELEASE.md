# Release Process

## How to Release

1. Add one of these labels to your PR:
   - `sdkRelease:patch` — patch bump (1.0.0 → 1.0.1)
   - `sdkRelease:minor` — minor bump (1.0.0 → 1.1.0)
   - `sdkRelease:major` — major bump (1.0.0 → 2.0.0)
2. Merge the PR to `main`

That's it. Everything else is automatic:
- Version in `gradle.properties` is bumped automatically
- SDK is published to Maven Central
- Git tag `v{version}` is created
- Example app AAB is built and uploaded to Artifactory
- Release label is removed from the PR

## What Happens

| Step | Workflow | Action |
|------|----------|--------|
| 1 | SDK | Detects label, bumps version |
| 2 | SDK | Publishes to Maven Central via JReleaser |
| 3 | SDK | Creates git tag, commits new version to main |
| 4 | Example App | Triggered automatically after SDK succeeds |
| 5 | Example App | Builds signed AAB, uploads to Artifactory |

## No Label = No Release

If you merge a PR without a `sdkRelease:*` label, nothing happens. Just a normal merge.

## Version

`PUBLISH_VERSION` in `gradle.properties` is managed automatically. You don't need to edit it manually.
