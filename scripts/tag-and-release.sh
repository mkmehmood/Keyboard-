#!/usr/bin/env bash
set -e

# ==============================================================================
# Helper Script: Create Tag and Trigger GitHub Release
# ==============================================================================

TAG="${1:-}"

if [ -z "$TAG" ]; then
    echo "Usage: ./scripts/tag-and-release.sh <tag_name>"
    echo "Example: ./scripts/tag-and-release.sh v1.0.0"
    exit 1
fi

# Ensure tag starts with 'v'
if [[ ! "$TAG" =~ ^v ]]; then
    TAG="v$TAG"
fi

echo "==> Creating git tag: $TAG"
git tag -a "$TAG" -m "Release $TAG - Gboard Replica with Unlimited Clipboard"

echo "==> Pushing tag $TAG to origin to trigger GitHub Release workflow..."
git push origin "$TAG"

echo "==> Done! GitHub Actions is now building and uploading the APK to:"
echo "    https://github.com/<your-username>/<repo-name>/releases/tag/$TAG"
