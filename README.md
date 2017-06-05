Fedora 4 ModeShape Utils
========================

This project exposes a direct ModeShape backup/restore capability.

The difference between this backup/restore and the one available via Fedora is that this backup excludes binary data. 
That way, the resources of a repository that contains significant amounts of content may reasonably be exported. 

[![Build Status](https://travis-ci.org/awoods/fcrepo-modeshape-utils.png?branch=master)](https://travis-ci.org/awoods/fcrepo-modeshape-utils)

## Usage Examples

1. Stop your running Fedora repository

2. Backing up a repository:
```java
java -jar fcrepo-modeshape-utils.jar /path/to/repository.json /directory/of/backup b
```

3. Restoring a repository:
```java
java -jar fcrepo-modeshape-utils.jar /path/to/repository.json /directory/of/backup r
```

4. Start your running Fedora repository

### Notes
The repository.json file should define the paths to objects and binary directories:
* https://github.com/fcrepo4/fcrepo4/blob/fcrepo-4.7.2/fcrepo-configs/src/main/resources/config/file-simple/repository.json#L12
* https://github.com/fcrepo4/fcrepo4/blob/fcrepo-4.7.2/fcrepo-configs/src/main/resources/config/file-simple/repository.json#L16

Additionally, since no Fedora libraries are included, the Fedora-specific security providers should be removed from the repository.json:
* https://github.com/fcrepo4/fcrepo4/blob/fcrepo-4.7.2/fcrepo-configs/src/main/resources/config/file-simple/repository.json#L25-L27

Current Release
---------------
* [0.2.0](https://github.com/awoods/fcrepo-modeshape-utils/releases/tag/fcrepo-modeshape-utils-0.2.0)
