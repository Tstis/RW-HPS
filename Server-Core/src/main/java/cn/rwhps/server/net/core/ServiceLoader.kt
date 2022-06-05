/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.core

import cn.rwhps.server.data.plugin.Value
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.log.exp.ImplementedException
import java.lang.reflect.Constructor

object ServiceLoader {
    private val ServiceLoaderData:MutableMap<String,Class<*>> = HashMap()
    private val ServiceObjectData:MutableMap<String,Value<*>> = HashMap()

    /**
     * 获取服务实例 (有参)
     * @param serviceType ServiceType            : 服务类型
     * @param serviceName String                 : 名称
     * @param parameterTypes Array<out Class<*>> : 构造参数
     * @return Constructor<*>                    : 实例
     */
    fun getService(serviceType: ServiceType, serviceName: String, vararg parameterTypes: Class<*>): Constructor<*>  {
        val serviceClass = ServiceLoaderData[serviceType.name+serviceName]
        if (serviceClass != null) {
            return ReflectionUtils.accessibleConstructor(serviceClass, *parameterTypes)
        } else {
            throw ImplementedException("${serviceType.name}:$serviceName")
        }
    }

    /**
     * 获取服务 Class
     * @param serviceType ServiceType : 服务类型
     * @param serviceName String      : 名称
     * @return Class<*>               : Class
     */
    fun getServiceClass(serviceType: ServiceType, serviceName: String): Class<*>  {
        val serviceClass = ServiceLoaderData[serviceType.name+serviceName]
        if (serviceClass != null) {
            return serviceClass
        } else {
            throw ImplementedException("${serviceType.name}:$serviceName")
        }
    }

    fun addService(serviceType: ServiceType, serviceName: String, service: Class<*>) {
        // 跳过已经存在的 只取第一个
        if (ServiceLoaderData.containsKey(serviceType.name+serviceName)) {
            return
        }
        ServiceLoaderData[serviceType.name+serviceName] = service
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T> getIRwHpsObject(serviceType: ServiceType, serviceName: String, netType: IRwHps.NetType): T {
        val serviceClass = ServiceLoaderData[serviceType.name+serviceName]
        var serviceObject = ServiceObjectData[serviceType.name+serviceName]
        if (serviceClass != null) {
            serviceObject = Value(ReflectionUtils.accessibleConstructor(serviceClass, IRwHps.NetType::class.java).newInstance(netType))
        }
        return serviceObject?.data as T
    }


    enum class ServiceType {
        Core,
        IRwHps,
        Protocol,
        ProtocolPacket,
        ProtocolType;
    }
}