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
