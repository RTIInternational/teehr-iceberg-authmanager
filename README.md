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

## Spark Consumption Sketch

```bash
--conf spark.sql.catalog.iceberg.rest.auth.type=org.teehr.iceberg.auth.TeehrBrokerAuthManager \
--conf spark.jars.packages=org.rtiamanzi:teehr-iceberg-authmanager:0.0.1
```
