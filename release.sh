#!/usr/bin/env bash
set -euo pipefail

usage() {
    printf 'Usage: %s [--dry-run] RELEASE_VERSION NEXT_DEVELOPMENT_VERSION\n' "$0"
    printf 'Example: %s 0.23.1 0.24-SNAPSHOT\n' "$0"
}

dry_run=false
case "${1:-}" in
    -h|--help)
        usage
        exit 0
        ;;
    --dry-run)
        dry_run=true
        shift
        ;;
esac

if [[ $# -ne 2 ]]; then
    usage >&2
    exit 2
fi

release_version=$1
development_version=$2

if [[ -z "$release_version" || "$release_version" == *-SNAPSHOT ]]; then
    printf 'Release version must be non-empty and must not end in -SNAPSHOT: %s\n' "$release_version" >&2
    exit 2
fi
if [[ "$development_version" != *-SNAPSHOT ]]; then
    printf 'Next development version must end in -SNAPSHOT: %s\n' "$development_version" >&2
    exit 2
fi

if [[ -n "$(git status --porcelain)" ]]; then
    printf 'The working tree must be clean before releasing:\n' >&2
    git status --short >&2
    exit 1
fi

tag="mork-parent-$release_version"
printf 'Release version:          %s\n' "$release_version"
printf 'Next development version: %s\n' "$development_version"
printf 'SCM tag:                  %s\n' "$tag"
printf 'Dry run:                  %s\n' "$dry_run"

maven_arguments=(
    -B
    "-DreleaseVersion=$release_version"
    "-DdevelopmentVersion=$development_version"
    "-Dtag=$tag"
    -DreleaseProfiles=release
)
goals=(release:clean release:prepare)

if [[ "$dry_run" == true ]]; then
    maven_arguments+=(-DdryRun=true)
    printf 'Dry-run files will remain available for inspection; clean them with ./mvnw release:clean.\n'
else
    printf 'Mork release signing check\n' | gpg --clearsign >/dev/null
    goals+=(release:perform)
fi

./mvnw "${maven_arguments[@]}" "${goals[@]}"
