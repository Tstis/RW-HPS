# RW-HPS Start Server
## JVM 环境要求
- JVM：最低 Java 8 优先建议JDK,也可以使用JRE

> 要下载 JDK：
> - 手动下载安装如 [AdoptOpenJDK](https://adoptopenjdk.net/) 或者 [OracleJDK](https://www.oracle.com/java/technologies/javase-downloads.html) 

## 运行
### 使用Github的编译好的版本
1.在 [Releases](https://github.com/RW-HPS/RW-HPS/releases) 下载版本
> 你也可以去Jitpack用一些阴间的方法下载

#### Windows
2.在你喜欢的目录下使用 ~~Cmd~~(不推荐) 或者 PowerShell (Windows 系统按住Shift+鼠标右键，点击"在此处打开 PowerShell") 运行jar
```bash
java -Dfile.encoding=UTF-8 -jar Server.jar
```

#### Linux 
**不建议无任何基础的在Linux使用**
> 请注意 Linux需要保活  你可以使用Screen **参见文章最后**  

2.直接输入
```bash
java -Dfile.encoding=UTF-8 -jar Server.jar
```

### 手动编译最新的测试版本
#### 我不知道Gradle如何使用请移步
[Gradle教程](Gradle.md)  

1.需要安装Git Java11 Screen(或许可以使用你喜欢的保活方式)      
Centos使用  
```bash  
sudo yum install git java11 screen -y
```
Ubuntu使用  
```bash  
sudo add-apt-repository ppa:linuxuprising/java
sudo apt update
sudo apt-get install git oracle-java11-installer screen -y  
```
2.同步存储库
>根据个人喜好  
```bash
HTTPS  
git clone https://github.com/RW-HPS/RW-HPS.git
``` 
```bash  
SSH
git clone git@github.com:RW-HPS/RW-HPS.git  
```
3.开始编译最新版本
在命令行输入
```bash
./gwadlew jar
```
等待完毕即可

4.使用  
在目录build/libs下即可获得编译好的Server Jar

5.运行  
在你喜欢的目录下运行jar
```bash
java -Dfile.encoding=UTF-8 -jar Server.jar
```
但是这样会在SSH断开后被关闭 那么我们就使用上文的Screen


## 使用Screen
1.需要安装Screen(或许可以使用你喜欢的保活方式)      
Centos使用
```bash  
sudo yum install screen -y
```
Ubuntu使用
```bash  
sudo apt update
sudo apt-get install screen -y  
```

```bash
screen -S 你喜欢的名字
cd Jar的目录下
java -Dfile.encoding=UTF-8 -jar Server.jar

# 退出使用Ctrl + A + D
#重进使用
screen -r 你设置的名字
#进不去使用 获取id
screen -ls
screen -r id
```
