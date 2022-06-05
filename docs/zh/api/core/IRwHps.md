# RW-HPS - IRwHps API

**目录**
- [1. 介绍](#介绍)
- [2. 接口](#接口)
- [3. 注册](#注册)
- [4. 获取实例](#获取实例)


# IRwHps
RW-HPS Protocol 接口. 是 RW-HPS 协议实现的接口.

## 介绍
### 获取
通常在服务器ServerLoad事件后 就可以使用 `ServiceLoader.getIRwHpsObject` 方法获取实例

### 使用 `IRwHps` 的接口
**`IRwHps` 中的接口通常是稳定**

### 稳定性
#### 使用稳定
所有接口默认是可以稳定使用的

#### 继承不稳定
**`IRwHps` 可能会增加新的抽象属性或函数. 因此不适合被继承或实现.**

## 接口
`IRwHps` 提供两个接口, 分别是
### typeConnect
通过本接口 您可以获取到Packet解析器

### abstractNetPacket
通过本接口 您可以获取到 `RW-HPS` 的 部分包协议实现  
核心部分实现由 `TypeConnect` 内的 `GameVersion*` 实现

## 注册
在 `Initialization` 已经注册了默认实现
```kotlin
ServiceLoader.addService(
    ServiceType.ProtocolType, 
    IRwHps.NetType.ServerProtocol.name,
    TypeRwHps::class.java
)
```
`RW-HPS` 强制性要求格式为
```kotlin
ServiceLoader.addService(
    服务的类型(ServiceLoader.ServiceType), 
    服务运行在何种协议.name (IRwHps.NetType),
    Class
)
```

## 获取实例
当 `IRwHps` 被实例化时 它应该做  
将 `typeConnect` `abstractNetPacket` 通过反射进行实例化  
失败则默认返回空实现
```kotlin
// RW-HPS
override val typeConnect: TypeConnect =
        try {
            val protocolClass = ServiceLoader.getServiceClass(ServiceType.Protocol,netType.name)
            ServiceLoader.getService(ServiceType.ProtocolType,netType.name,Class::class.java).newInstance(protocolClass) as TypeConnect
        } catch (e: Exception) {
            Log.fatal(e)
            NullTypeConnect()
        }

override val abstractNetPacket: AbstractNetPacket =
    try {
        ServiceLoader.getService(ServiceType.ProtocolPacket,"ALLProtocol").newInstance() as AbstractNetPacket
    } catch (e: Exception) {
        Log.fatal(e)
        NullNetPacket()
    }
```
