# content-git / init
Container for initializing content from git repositories

## Configuration

```shell script
export CONFIG_PATH=/full/path/to/config.yaml
export TARGET_DIR=/full/target/path
node init-content.js 
```

The result is that all git repose are cloned based on configurations

## How to run

### Docker

Use docker from [Docker Hub](https://hub.docker.com/r/websitecd/content-git-init) or build your own.

Build image:

```shell script
docker build -t websitecd/content-git-init .
```

Prepare config for docker
```shell script
rm -rf /tmp/repos; mkdir /tmp/repos
cp examples/static-content-config.yaml /tmp/repos/static-content-config.yaml
```

Run init:

```shell script
docker run --rm -i -e "CONFIG_PATH=/app/data/static-content-config.yaml" -e "TARGET_DIR=/app/data" -v "/tmp/repos/:/app/data/" websitecd/content-git-init node init-content.js
```

Push to repo

```shell
docker tag websitecd/content-git-init quay.io/websitecd/content-git-init
docker push quay.io/websitecd/content-git-init
```