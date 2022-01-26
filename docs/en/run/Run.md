## RW-HPS Start Server
## JVM Environment Requirements
- JVM: Minimum Java 11 preferred JDK, JRE can also be used

> To download the JDK.
> - Download and install manually such as [AdoptOpenJDK](https://adoptopenjdk.net/) or [OracleJDK](https://www.oracle.com/java/technologies/javase-downloads.html)

## Run
### Use the compiled version from Github
1. Download the version from [Releases](https://github.com/RW-HPS/RW-HPS/releases)
> You can also go to Jitpack and download it using some netherworld methods

#### Windows
2. Run the jar in your preferred directory using Cmd or PowerShell (on Windows systems hold down Shift+right mouse button and click "Open PowerShell here")
```bash
java -jar Server.jar
```

#### Linux
**Not recommended for use in Linux without any basic knowledge**
> Please note that Linux needs to be kept alive You can use Screen **see the end of the article**

2. Type directly
```bash
java -jar Server.jar
```

### Manually compile the latest test version
#### I don't know how to use Gradle please move to
[Gradle Tutorial](Gradle.md)

1. You need to install Git Java11 Screen (maybe you can use your favorite way to keep it alive)      
   Centos use
```bash  
sudo yum install git java11 screen -y
```
Ubuntu uses  
``bash  
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt-get install git oracle-java11-installer screen -y
```
2. Synchronize the repository
> according to personal preference  
```bash
HTTPS  
git clone https://github.com/RW-HPS/RW-HPS.git
``` 
```bash  
SSH
git clone git@github.com:RW-HPS/RW-HPS.git  
```
3. Start compiling the latest version
   At the command line, type
```bash
. /gwadlew jar
```
Just wait for it to finish

4. Use  
   You can get the compiled Server Jar under the directory build/libs

5.Run  
Run the jar in your preferred directory
```bash
java -jar Server.jar
```
But this will be closed when SSH is disconnected, so we'll use Screen as above


## Using Screen
1. Screen needs to be installed (maybe you can use your preferred way of keeping it alive)      
   Centos uses
```bash  
sudo yum install screen -y
```
Ubuntu uses
``bash  
sudo apt update
sudo apt-get install screen -y
```

``bash
screen -S the name you like
cd Jar's directory
java -jar Server.jar

# Quit using Ctrl + A + D
# Reenter using
screen -r The name you set
# Can't get in use Get id
screen -ls
screen -r id
```