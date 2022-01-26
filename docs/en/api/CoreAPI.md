# RW-HPS - Concise API

> Note:
> - This section shows examples of the more commonly used APIs for `RW-HPS-core-api`.
> - Please check with `RW-HPS-core-api` source code
> - This chapter only provides a rough introduction to the API

----------------------

# GameServer

## StartNet

`StartNet` is used to create a new `StartNet`

``` java
StartNet bot = new StartNet();
```

## Misc utils

## Log

All RW-HPS logs are output via `Log`, see the `Log` source comments for more information

## FileUtil

`FileUtil` represents an external file

Constructing a `FileUtil` can be done in the following way

```java
// because no file is created and the location is in the same directory as the jar
FileUtil.getFile("filename");
// because no files are created, only directories are created
FileUtil.getFolder("folderName"); 
// will only create directories
FileUtil.getFolder("folderName").toFile("filename");
/**
The three instances of FileUtil will do nothing and will not create directories or files
If you need to go to a directory before a file, then use FileUtil.toFolder(folder name).toFile(file name)
If you need to go into multiple directories first, then use FileUtil.toFolder(folder name).toFolder(folder name)
File creation is only done when FileUtil().read/Write is used
Attention:
The initial directory of FileUtil.toFolder is the directory of Server.jar or the Main submission parameter directory
toFolder is just an entry
*/
// Get temporary files
FileUtil.getTempFile("filename")
// Get the temporary folder
FileUtil.getTempDirectory("folderName")
// will create the folder and try to create the file
FileUtil().mkdir();
// will try to create the file
FileUtil().createNewFile();
```

# Events
[Events](Events.md)

# Net framework
![](../img/NetArchitecture.png)