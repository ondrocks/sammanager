# Details #

In daily work You can simply use:

```
mvn package
```

To produce a release package (with start/stop scripts etc):

```
mvn clean package assembly:assembly -Prelease
```