# Cygnus translators

## `cygnus-translator-0.1-to-0.2.sh`

Input parameters:

* `hdfsUser`: HDFS username owning the files to be translated.
* `srcHDFSFolder`: absolute HDFS path to the folder containing the 0.1-like files to be translated.
* `dstHDFSFolder`: absolute HDFS path to the folder that will contain the 0.2-like files to be translated.

Example:

    $ ./cygnus-translator-0.1-to-0.2 johndoe /user/johndoe/old_dataset/ /user/johndoe/new_dataset/

Thus, if having this 0.1-like files in `/user/johndoe/old_dataset/`:

    /user/johndoe/old_dataset/Room1-Room-temperature-centigrade.txt
    /user/johndoe/old_dataset/Room1-Room-pressure-milibars.txt
    /user/johndoe/old_dataset/Room1-Room-humidity-percentage.txt
    /user/johndoe/old_dataset/Room2-Room-temperature-centigrade.txt
    /user/johndoe/old_dataset/Room2-Room-pressure-milibars.txt
    /user/johndoe/old_dataset/Room2-Room-humidity-percentage.txt

The 0.2-like result will be:

    /user/johndoe/new_dataset/cygnus-Room1-Room.txt
    /user/johndoe/new_dataset/cygnus-Room2-Room.txt

I.e. a single file per enity, as specified by Cygnus 0.2 and higher.

## `cygnus-translator-0.1-to-0.3.sh`

Input parameters:

* `hdfsUser`: HDFS username owning the files to be translated.
* `prefixName`: Custom prefix to be added to the 0.3-like files, if any (use `""` for a null prefix).
* `srcHDFSFolder`: absolute HDFS path to the folder containing the 0.1-like files to be translated.
* `dstHDFSFolder`: absolute HDFS path to the folder that will contain the 0.3-like files to be translated.

Example:

    $ ./cygnus-translator-0.1-to-0.2 johndoe "" /user/johndoe/old_dataset/ /user/johndoe/new_dataset/

Thus, if having this 0.1-like files in `/user/johndoe/old_dataset/`:

    /user/johndoe/old_dataset/Room1-Room-temperature-centigrade.txt
    /user/johndoe/old_dataset/Room1-Room-pressure-milibars.txt
    /user/johndoe/old_dataset/Room1-Room-humidity-percentage.txt
    /user/johndoe/old_dataset/Room2-Room-temperature-centigrade.txt
    /user/johndoe/old_dataset/Room2-Room-pressure-milibars.txt
    /user/johndoe/old_dataset/Room2-Room-humidity-percentage.txt

The 0.3-like result will be:

    /user/johndoe/new_dataset/Room1-Room/Room1-Room.txt
    /user/johndoe/new_dataset/Room2-Room/Room2-Room.txt

Observe the entity data, represented by its entity descriptor file, is preceded by a folder having the same name than the entity descriptor. This is a requirement for Hive tables creation, which needs folders instead of files.
