  1. `$ cd parent/`
  1. `$ mvn versions:set -DnewVersion=RELEASED_VERSION`
  1. `$ mvn clean package assembly:assembly`
  1. `$ cp target/samm-RELEASE_VERSION-bin.tar.gz ../wiki/`
  1. `$ cd ..`
  1. `$ git add *pom.xml`
  1. `$ git add wiki/samm-RELEASE_VERSION-bin.tar.gz`
  1. `$ git commit -m 'releasing RELEASE_VERSION'`
  1. `$ git push origin --tags`
  1. `$ mvn versions:set -DnewVersion=NEXT_VERSION-SNAPSHOT`

**Todo:**

Consider using mvn release plugin