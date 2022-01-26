# RW-HPS - Gradle
## Gradle
### What is Gradle
Gradle is an automated project builder based on the Apache Ant and Apache Maven concepts. It uses a domain-specific language based on Groovy to declare project settings instead of traditional XML. currently its support is limited to Java, Groovy and Scala, with plans to support more languages in the future.

### Gradle how to use
1. Gradle runs on the JVM, which is the environment where java runs. So you need to install jdk and jre, it is recommended to use Java11, because Java is backward compatible.
2. Then go to Gradle official website to get the Gradle package. Address, this page inside and two ways, a manual installation, one through the script installation. I generally like to do it myself, so that it is easier to clean up in the future
3. download the package, decompress it, and then configure the environment variables, manually installed jdk people should be very familiar with the configuration of environment variables. The way to configure environment variables is different under each platform

## Compile with Gradle
### Initially
1. open a terminal  
   Open Cmd or PowerShell in the directory you want
2. Start compiling  
   At the command line, type
```bash
. /gwadlew jar
```
Just wait for it to finish

3. Use  
   You can get the compiled Server Jar under the directory build/libs

**For more tutorials, see [Google](https://google.com) or [Baidu](https://baidu.com) or [Bing](https://bing.com)**