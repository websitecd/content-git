# content-git-api
Container with API for git repositories

## API

* `/api/list` - list of components (dirs)
* `/api/update/{dir}` - update particular component (dir)
* `/websiteinfo` - website info
* `/health/live`
* `/health/ready`

## How to run

## Docker

```shell
mkdir /tmp/repos/
docker run --rm -e "APP_DATA_DIR=/app/data" -v "/tmp/repos/:/app/data/" -p 8090:8090 quay.io/websitecd/content-git-api
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

docker build -f src/main/docker/Dockerfile.native -t websitecd/content-git-api .
```

```shell
docker run -i --rm -e APP_DATA_DIR=/app/data -v /tmp/repos:/app/data/ -p 8090:8090 websitecd/content-git-api
```
