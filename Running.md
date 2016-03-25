# Running SAMM #

To start working with SAMM first download the archive from here

Unpack archive and execute start.sh script with a configuration file name passed as command-line argument:

```
./start.sh configuration-file.xml
```

To stop SAMM use stop.sh script:

```
./stop.sh
```

There is also a separate script for debugging purposes which suspends the JVM at start and opens a remote debugging server at port 9999:

```
./start_debug.sh configuration-file.xml
```