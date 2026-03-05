# Release and Tag Help

This repository publishes to pub.dev from GitHub Actions when a version tag is pushed.

## Current Publishing Behavior

1. Workflow: `.github/workflows/publish.yml`
2. Trigger: tag push matching semantic version format, for example `0.0.13`
3. Validation: pushed tag must exactly match `version:` in `pubspec.yaml`
4. Result: if checks pass, `flutter pub publish --force` runs in CI

## Create and Push a Tag

Use this when you want to trigger publishing.

1. Make sure `pubspec.yaml` contains the target version, for example `version: 0.0.13`.
2. Commit your changes.
3. Create and push the tag:

```bash
git tag 0.0.13
git push origin 0.0.13
```

## Delete a Tag

Use this if a tag was created by mistake.

```bash
git tag -d 0.0.13
git push origin --delete 0.0.13
```

## How Tag Changes Affect Publishing

1. Pushing a valid version tag triggers the publish workflow.
2. Deleting a tag does not remove an already published version from pub.dev.
3. A published pub.dev version cannot be published again with the same version number.
4. If a publish failed before upload, you can fix issues and push the same tag again.
5. If a version was already published, bump `pubspec.yaml` to a new version, then create and push a new tag.

## Important Note About `release.yml`

The manual release workflow (`.github/workflows/release.yml`) currently pushes tags using `GITHUB_TOKEN`.
Tags pushed that way may not trigger another workflow in GitHub Actions.

Reliable publish trigger: create and push the version tag from local git commands.
