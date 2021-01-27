# content-git-init
Container for initializing content from git repositories

## Configuration

Configured via these env variables:
* `CONFIG_PATH` - full path to the config yaml file
* `TARGET_DIR` - target dir to clone repos
* `GIT_SSL_NO_VERIFY` if true SSL verification is not performed and git repos are so configured

## How to run

### Locally

```shell
rm -rf /tmp/repos; mkdir /tmp/repos
CONFIG_PATH=examples/static-content-config.yaml GIT_SSL_NO_VERIFY=true TARGET_DIR=/tmp/repos npm start
```


### Docker

Use docker from [Quay](https://quay.io/repository/websitecd/content-git-init) or build your own.

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
docker run --rm -e "CONFIG_PATH=/app/data/static-content-config.yaml" -e "TARGET_DIR=/app/data" -e "GIT_SSL_NO_VERIFY=true" -v "/tmp/repos/:/app/data/" websitecd/content-git-init
```

Push to repo

```shell
docker tag websitecd/content-git-init quay.io/websitecd/content-git-init
docker push quay.io/websitecd/content-git-init
```
