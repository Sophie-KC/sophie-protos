# sophie-protos

Single source of truth for every sophie gRPC contract. This is the **only** repo that runs `protoc`.
It publishes one Gradle artifact — `org.sophie:sophie-protos` — containing the generated Java + grpc-java
stubs for all 7 service contracts, plus the `.proto` sources themselves (under `proto/` in the jar) for
future non-Java (Dart/TypeScript) codegen.

Contracts owned here: `chat_service`, `doc_service`, `file_service`, `notification_service`,
`org_service`, `search_service`, `task_service` (`src/main/proto/`).

The proto/grpc/protobuf toolchain versions are pinned in one place — the `ext { … }` block in
`build.gradle` (protoc 3.25.5, grpc-java 1.68.0, protobuf-java 3.25.5).

## Build & publish locally

```bash
./gradlew publishToMavenLocal
```

Publishes `org.sophie:sophie-protos:0.1.0` to `~/.m2`. Every consumer repo lists `mavenLocal()` first,
so a local publish is all you need to iterate — no registry round-trip.

## Consuming it

Consumers depend on `org.sophie:sophie-protos:0.1.0` and get `grpc-stub`, `grpc-protobuf` and
`protobuf-java` transitively (they are `api` here). Consumers keep only their runtime bits
(`net.devh` starter; `protobuf-java-util` where they use `JsonFormat`; `grpc-services` in search).

## GitHub Packages (registry) setup — REQUIRED for a clean checkout

`mavenLocal()` covers a machine that has run `publishToMavenLocal`. A **fresh** checkout that has not
built `sophie-protos` locally resolves the artifact from GitHub Packages, which needs credentials.

1. Create a GitHub Personal Access Token (classic) with **`read:packages`** (add `write:packages` only
   if you will publish). https://github.com/settings/tokens
2. Put credentials + the repo owner in `~/.gradle/gradle.properties` (NOT in any repo):

   ```properties
   gpr.owner=<github-org-or-user-that-hosts-sophie-protos>
   gpr.user=<your-github-username>
   gpr.key=<your-PAT>
   ```

   Or export env vars instead: `GITHUB_ACTOR` (username) and `GITHUB_TOKEN` (PAT). The `gpr.owner`
   still has to be set as a property (or edit the placeholder in the build files).
3. **`gpr.owner` is currently the placeholder `REPLACE_WITH_GITHUB_OWNER`** in this repo's
   `build.gradle` and in all 8 consumer `build.gradle` files. Set the real owner via the property, or
   do a one-time find/replace of that placeholder once the GitHub repo exists.

To publish to the registry (CI, or a maintainer):

```bash
./gradlew publish
```
