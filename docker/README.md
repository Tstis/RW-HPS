


> 本镜像为方便管理 (后台运行+前台交互, 异常时自动重启), 已打包 `Supervisor`, 并将常用管理脚本打包

> PS: Docker 镜像使用 `GitHub Actions` 自动更新, 可选版本号, 或选择最新开发版 (tag: `{last_release}-beta`), [点击查看](https://github.com/RW-HPS/RW-HPS/pkgs/container/rw-hps)

## Docker

> 后台运行

```bash
docker run --name rw-hps -d -p 5123:5123 -v ~/rw-hps-data:/app/data ghcr.io/rw-hps/rw-hps
```

> 进入容器内部

```bash
docker exec -it rw-hps bash
```

> 查看当前状态 是否处于运行中

```bash
root@5616fb973882:/app# ./status.sh
rw-hps                           RUNNING   pid 9, uptime 0:26:01
```

> 进入前台交互/输入 RW-HPS 指令

```bash
./connect.sh
```

> 重启 rw-hps 服务

```bash
./restart.sh
```



## Docker Compose

```yml docker-compose.yml
version: '3.4'

services:
  rw-hps:
    image: ghcr.io/rw-hps/rw-hps
    container_name: rw-hps
    ports:
      - "5123:5123"
    restart: always
    environment:
      - TZ=Asia/Shanghai
    volumes:
      - ./data:/app/data
    privileged: true
    user: root
```

> 后台运行

```bash
docker-compose up -d
```

> 其它和 Docker 一致

