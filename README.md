# hashcheck
![travis](https://travis-ci.org/teagithub/hashcheck.svg?branch=master)

generate hash code such as md5/sha-1/sha-256 of a file or a directory recursively and check it

Usage:
```
java -jar hashchek.jar generate <directory path> <algorithm, e.g. MD5(default)> //generate hash code
java -jar hashchek check <directory path> <algorithm, e.g. MD5(default)> //check hash code
```