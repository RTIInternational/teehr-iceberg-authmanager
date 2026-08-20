# TEEHR Iceberg AuthManager

Standalone Maven project for the custom Iceberg REST AuthManager used by TEEHR Polaris integrations.

## What It Does

- Implements `org.teehr.iceberg.auth.TeehrBrokerAuthManager`
- Fetches short-lived Polaris access tokens from the teehr-api broker
- Caches and refreshes tokens before expiry
- Injects `Authorization: Bearer <token>` on Iceberg REST catalog requests

## Build

```bash
mvn -DskipTests package
```

The built jar is produced under `target/teehr-iceberg-authmanager-<version>.jar`.

## CI

- Pull requests and pushes to `main` run Maven verification via `.github/workflows/ci.yml`
- Releases publish to Maven Central via `.github/workflows/publish.yml`

## Publish To Maven Central

Published Maven coordinates:

- Group ID: `org.rtiamanzi`
- Artifact ID: `teehr-iceberg-authmanager`

Release process for a new version such as `0.0.2`:

1. Update the project version in `pom.xml`
2. Update any version references in this README and in downstream consumers
3. Commit the version change to `main`
4. Create a Git tag matching the release, for example `v0.0.2`
5. Push the tag and publish a GitHub Release from that tag
6. Wait for `.github/workflows/publish.yml` to complete and verify the artifact in Maven Central

Downstream consumers to check after release:

- `teehr/src/teehr/evaluation/spark_session_utils.py` if the default package coordinate should move to the new version
- Any deployment or documentation that hard-codes `org.rtiamanzi:teehr-iceberg-authmanager:<version>`

1. Create a Sonatype Central account and claim the `org.rtiamanzi` namespace
2. Create Sonatype deployment credentials (token)
3. Create and export a GPG keypair used for artifact signing
4. Add GitHub Actions secrets in this repository:
	- `CENTRAL_TOKEN_USERNAME`
	- `CENTRAL_TOKEN_PASSWORD`
	- `MAVEN_GPG_PRIVATE_KEY` (ASCII-armored private key)
	- `MAVEN_GPG_PASSPHRASE`
5. Create a GitHub release tag (for example `v0.0.1`)
6. The publish workflow runs `mvn -Prelease deploy` and releases automatically

Typical command sequence for `0.0.2`:

```bash
git checkout main
git pull --ff-only
git commit -am "Release 0.0.2"
git tag v0.0.2
git push origin main
git push origin v0.0.2
```

Then create the GitHub Release for `v0.0.2` if you did not use the GitHub UI to create the tag first. The publish workflow is configured for both `release.published` and `workflow_dispatch`, but the normal release path should be a tagged GitHub Release.

For local manual deploys, configure `~/.m2/settings.xml` with a `central` server entry:

```xml
<settings>
	<servers>
		<server>
			<id>central</id>
			<username>YOUR_CENTRAL_TOKEN_USERNAME</username>
			<password>YOUR_CENTRAL_TOKEN_PASSWORD</password>
		</server>
	</servers>
</settings>
```

Then run:

```bash
mvn -DskipTests -Prelease deploy
```

That local deploy path is useful for validating Central credentials and signing before cutting the GitHub Release, but it still publishes the exact version declared in `pom.xml`, so make sure the version has been updated first.

## Spark Consumption Sketch

```bash
--conf spark.sql.catalog.iceberg.rest.auth.type=org.teehr.iceberg.auth.TeehrBrokerAuthManager \
--conf spark.jars.packages=org.rtiamanzi:teehr-iceberg-authmanager:0.0.2
```
