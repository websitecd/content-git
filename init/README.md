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

### Locally

```shell
rm -rf /tmp/repos
CONFIG_PATH=examples/static-content-config.yaml TARGET_DIR=/tmp/repos npm start
```


### Docker

Use docker from [Quay](https://quay.io/repository/websitecd/content-git-api) or build your own.

Build image:

```shell script
docker build -t websitecd/content-git-init .
```

Prepare config
```shell script
rm -rf /tmp/repos; mkdir /tmp/repos
cp examples/static-content-config.yaml /tmp/repos/static-content-config.yaml
```

Run init:

```shell script
docker run --rm -i -e "CONFIG_PATH=/app/data/static-content-config.yaml" -e "TARGET_DIR=/app/data" -v "/tmp/repos/:/app/data/" websitecd/content-git-init
```

Push to repo

```shell
docker tag websitecd/content-git-init quay.io/websitecd/content-git-init
docker push quay.io/websitecd/content-git-init
```
