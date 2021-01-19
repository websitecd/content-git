# content-git / API
Container with API for git repositories

## How to run

### Locally

#### JVM Mode

```shell
mvn package
```

Run: 
```shell
APP_TARGET_DIR=/tmp APP_CONFIG_PATH=src/test/resources/config-test.yaml java -jar target/content-git-init-1.0.0-SNAPSHOT-runner.jar
```

#### Native Mode

```shell
mvn package -Pnative
```

Run:
```shell
APP_TARGET_DIR=/tmp APP_CONFIG_PATH=src/test/resources/config-test.yaml ./target/content-git-init-1.0.0-SNAPSHOT-runner
```
