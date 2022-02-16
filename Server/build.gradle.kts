import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
	kotlin("jvm") version "1.6.10"
	`maven-publish`
}

//Netty Version
val nettyVersion = "4.1.73.Final"

/**
 * 这里全部采用的compileOnly 为了适配idea 但是编译的时候会切到implementation
 * 需要帮助!
 */
dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

	//implementation project(":RUDP")
	//implementation "cn.hutool:hutool-socket:5.7.5"

	implementation("io.netty:netty-buffer:$nettyVersion")
	implementation("io.netty:netty-codec:$nettyVersion")
	implementation("io.netty:netty-handler:$nettyVersion")
	implementation("io.netty:netty-transport:$nettyVersion")
	implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-aarch_64")
	implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")

//	implementation (group: "io.netty", name: "netty-all", version: "4.1.73.Final") {
//		exclude group: "io.netty", module: "netty-resolver"
//	}

	//implementation fileTree(dir:"libs",include:["ChainMarket-23fc7f989f.jar"])

	implementation("com.github.deng-rui:RUDP:2.0.0")
	implementation("com.github.deng-rui:Compress-Zip:1.0.0")

	implementation("com.google.code.gson:gson:2.8.9")

	//compileOnly group: "com.ip2location", name: "ip2location-java", version: "8.5.0"
	//compileOnly group: "com.alibaba", name: "fastjson", version: "1.2.58"
	implementation("com.squareup.okhttp3:okhttp:4.9.3") {
		exclude(group = "org.jetbrains.kotlin")
	}

	//compileOnly("commons-io:commons-io:2.11.0")
	//compileOnly("it.unimi.dsi:fastutil-core:8.5.8")
	//compileOnly group: "org.bouncycastle", name: "bcprov-jdk15on", version: "1.69"

	//compileOnly fileTree(dir:"libs",include:["*.jar"])
	//compileOnly group: "org.quartz-scheduler", name: "quartz", version: "2.3.2"
	//compileOnly group: "com.github.oshi", name: "oshi-core", version: "5.5.0"


}

tasks {
	withType<KotlinCompile>().all {
		kotlinOptions.jvmTarget = "1.8"
	}
}


tasks.jar {
	exclude("META-INF/versions/9/module-info.class")
	exclude("META-INF/LICENSE.txt")
	exclude("META-INF/LICENSE")
	exclude("META-INF/NOTICE.txt")
	exclude("META-INF/NOTICE")

	// Fuck Netty !!!!!!
	exclude("META-INF/INDEX.LIST")
	exclude("META-INF/*.properties")
	exclude("META-INF/*/*.properties")
	exclude("META-INF/*/*/*.properties")
	exclude("META-INF/*/*/*/*.properties")
	exclude("META-INF/*.xml")
	exclude("META-INF/*/*.xml")
	exclude("META-INF/*/*/*.xml")
	exclude("META-INF/*/*/*/*.xml")

	manifest {
		 attributes(mapOf("Main-Class" to "com.github.dr.rwserver.Main"))
		 attributes(mapOf("Launcher-Agent-Class" to  "com.github.dr.rwserver.dependent.LibraryManager"))
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.RW-HPS"
			artifactId = "Server"
			version = "1.0.0"
			from(components["java"])

			/*
			Need Help 需要帮助
			无法分模块创建Pom.Xml
			pom.withXml {
			}
 			*/
		}
	}
}
