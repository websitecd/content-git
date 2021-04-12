# content-git-api
Container with API for git repositories

## API

* `/api/list` - list of components (dirs)
* `/api/info/{dir}` - info about component (dir)
* `/api/update/{dir}` - update particular component (dir)
* `/health/live`
* `/health/ready`

### /api/info/{dir}

Example of info API:

```json
{
  "url": "https://github.com/spaship/spaship-examples.git",
  "branch": "main",
  "lastCommit": {
    "message": "Merge pull request #3 from spaship/renovate/pin-dependencies\n\nPin dependency nodemon to 2.0.7",
    "author": "Libor Krzyzanek",
    "timestamp": "2021-03-25 10:49:32"
  }
}
```

## How to run

## Docker

```shell
mkdir /tmp/repos/
docker run --rm -e "APP_DATA_DIR=/app/data" -v "/tmp/repos/:/app/data/" -p 8090:8090 quay.io/spaship/content-git-api
```

### Locally

#### JVM Mode

```shell
mvn package
```

Run: 
```shell
APP_DATA_DIR=/tmp/repos java -jar target/quarkus-app/quarkus-run.jar
```

#### Native Mode

```shell
# Requires GraalVM installed (https://quarkus.io/guides/building-native-image#configuring-graalvm)
mvn package -Pnative
```

Run:
```shell
APP_DATA_DIR=/tmp/repos ./target/content-git-api-1.1.1-SNAPSHOT-runner
```

#### Docker

```shell
mvn clean package -Pnative -Dquarkus.native.container-build=true

docker build -f src/main/docker/Dockerfile.native -t spaship/content-git-api .
```

```shell
docker run -i --rm -e APP_DATA_DIR=/app/data -v /tmp/repos:/app/data/ -p 8090:8090 spaship/content-git-api
```
