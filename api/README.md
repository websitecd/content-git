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
APP_DATA_DIR=/tmp/repos java -jar target/content-git-api-1.0.0-SNAPSHOT-runner.jar
```

#### Native Mode

```shell
mvn package -Pnative
```

Run:
```shell
APP_DATA_DIR=/tmp/repos ./target/content-git-api-1.0.0-SNAPSHOT-runner
```
