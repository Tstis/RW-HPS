
# Start

> 1.创建配置文件

```bash
mkdir -p ~/rw-hps/docker
# 创建
vi ~/rw-hps/docker/Config.json
# 或者下载
wget -O ~/rw-hps/docker/Config.json https://raw.githubusercontent.com/RW-HPS/RW-HPS/master/docker/Config.json
```

> 2.运行，Go!

```bash
docker run -d --restart=always -v ~/rw-hps/docker:/app/data -p 5123:5123 --name rw-hps-container yiyungent/rw-hps
```



# 注意

这里是将 `宿主机` 的 `~/rw-hps/docker` 挂载到 `容器内` 的 `/app/data`,   
也就是说 宿主机 `~/rw-hps/docker` 相当于 `data` 目录，成功启动容器后，会生成 `plugins Setting.bin Config.json`


# 补充

> 下方用于从源代码中自己构建镜像等

```bash
docker build -t rw-hps .
docker stop rw-hps-container
docker rm rw-hps-container
docker run -d -v ~/rw-hps/docker:/app/data -p 5123:5123 --name rw-hps-container rw-hps
docker run -it -v ~/rw-hps/docker:/app/data -p 5123:5123 --name rw-hps-container rw-hps bash
docker attach rw-hps-container
docker exec -it rw-hps-container bash
docker start -i rw-hps-container bash
```

```bash
docker build -t yiyungent/rw-hps .
docker push yiyungent/rw-hps
docker run -d -v ~/rw-hps/docker:/app/data -p 5123:5123 --name rw-hps-container yiyungent/rw-hps
docker run -it -v ~/rw-hps/docker:/app/data -p 5123:5123 --name rw-hps-container yiyungent/rw-hps bash
docker exec -it rw-hps-container bash
docker logs rw-hps-container
```

```bash
# 日志路径
docker inspect rw-hps-container | grep -i logpath
```

```bash
mkdir -p ~/rw-hps/docker
vi ~/rw-hps/docker/Config.json
# 或者下载
wget -O ~/rw-hps/docker/Config.json https://raw.githubusercontent.com/RW-HPS/RW-HPS/master/docker/Config.json
```

> ~/rw-hps/docker/Config.json

```json
{
	"readMap":"false",
	"deleteLib":"",
	"log":"ALL",
	"winOrLose":"false",
	"oneReadUnitList":"false",
	"serverName":"RW-HPS",
	"webApiSsl":"false",
	"maxMessageLen":"40",
	"reConnect":"true",
	"webApiSslKetPath":"",
	"maxPlayerAd":"",
	"startAd":"",
	"winOrLoseTime":"30000",
	"pluginNumber":"",
	"defIncome":"1.0",
	"enterServerAd":"",
	"maxUnit":"200",
	"modNumber":"",
	"iPCheckMultiLanguageSupport":"false",
	"webApiPort":"0",
	"maxPlayer":"10",
	"oneAdmin":"true",
	"tickSpeed":"0",
	"webApi":"false",
	"runPid":"2463",
	"startPlayerAd":"",
	"serverUpID":"",
	"startRelay":"true",
	"gameOverUpList":"false",
	"port":"5123",
	"passwd":"",
	"webUrl":"",
	"UDPSupport":"true",
	"passwdCheckApi":"false",
	"webApiSslPasswd":""
}
```



