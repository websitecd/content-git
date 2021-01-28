# Contributing

Feel free to create an issue, or a pull request.

## Release process

Perform test first

```shell
mvn clean package -Pnative
```

1. Maven release
```
mvn clean release:prepare release:perform
```
2. Wait till completes.
3. Create a [Github release](https://github.com/jbossorg/feedsaggregator/releases) based on the latest tag and document the release.