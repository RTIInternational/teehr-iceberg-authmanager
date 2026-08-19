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

1. Create a Sonatype Central account and claim your namespace
2. Create Sonatype deployment credentials (token)
3. Create and export a GPG keypair used for artifact signing
4. Add GitHub Actions secrets in this repository:
	- `OSSRH_USERNAME`
	- `OSSRH_TOKEN`
	- `MAVEN_GPG_PRIVATE_KEY` (ASCII-armored private key)
	- `MAVEN_GPG_PASSPHRASE`
5. Create a GitHub release tag (for example `v0.0.1`)
6. The publish workflow runs `mvn -Prelease deploy` and releases automatically

For local manual deploys, configure `~/.m2/settings.xml` with an `ossrh` server entry:

```xml
<settings>
	<servers>
		<server>
			<id>ossrh</id>
			<username>YOUR_OSSRH_USERNAME</username>
			<password>YOUR_OSSRH_TOKEN</password>
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
