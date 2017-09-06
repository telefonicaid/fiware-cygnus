# HDFS backend
## `HDFSBackend` interface
This class enumerates the methods any [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html) backend implementation must expose. In this case, the following ones:

    void createDir(String dirPath) throws Exception;

> Creates a HDFS directory, given its path.

    void createFile(String filePath, String data) throws Exception;

> Creates a HDFS file, given its path, and writes initial data to it.

    void append(String filePath, String data) throws Exception;

> Appends new data to an already existent given HDFS file.

    boolean exists(String filePath) throws Exception;

> Checks if a HDFS file, given its path, exists ot not.

## `HDFSBackendImpl` class
This is a convenience backend class for HDFS that extends the `HttpBackend` abstract class (provides common logic for any Http connection-based backend) and implements the `HDFSBackend` interface described above.

`HDFSBackendImpl` really wraps the [WebHDFS API](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html).

It must be said this backend implementation enforces UTF-8 encoding through the usage of a `Content-Type` http header with a value of `text/plain; charset=utf-8`.
