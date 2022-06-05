plugins {
	`maven-publish`
}


//Netty Version
val nettyVersion = "4.1.77.Final"

/**
 * Fuck implementation
 */
dependencies {
	api("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")

	implementation(project(":TimeTaskQuartz"))
	//api "cn.hutool:hutool-socket:5.7.5"

	api("io.netty:netty-buffer:$nettyVersion")
	api("io.netty:netty-codec:$nettyVersion")
	api("io.netty:netty-codec-http:$nettyVersion")
	api("io.netty:netty-handler:$nettyVersion")
	api("io.netty:netty-transport:$nettyVersion")
	api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-aarch_64")
	api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")

//	api (group: "io.netty", name: "netty-all", version: "4.1.73.val") {
//		exclude group: "io.netty", module: "netty-resolver"
//	}

	//api fileTree(dir:"libs",include:["ChainMarket-23fc7f989f.jar"])

	api("com.github.deng-rui:RUDP:2.0.0")
	//api("com.github.deng-rui:Compress-Zip:1.0.0")

	api("com.google.code.gson:gson:2.9.0")

	api("org.apache.commons:commons-compress:1.21")


	//compileOnly group: "com.ip2location", name: "ip2location-java", version: "8.5.0"
	//compileOnly group: "com.alibaba", name: "fastjson", version: "1.2.58"
	api("com.squareup.okhttp3:okhttp:4.9.3") {
		exclude(group = "org.jetbrains.kotlin")
	}

	implementation("org.jline:jline-reader:3.21.0")
	implementation("org.fusesource.jansi:jansi:2.4.0")


	compileOnly("commons-io:commons-io:2.11.0")
	compileOnly("it.unimi.dsi:fastutil:8.5.8")
	//compileOnly group: "org.bouncycastle", name: "bcprov-jdk15on", version: "1.69"

	//compileOnly fileTree(dir:"libs",include:["*.jar"])
	//compileOnly group: "org.quartz-scheduler", name: "quartz", version: "2.3.2"
	//compileOnly group: "com.github.oshi", name: "oshi-core", version: "5.5.0"

	testApi("org.junit.jupiter:junit-jupiter-engine:5.8.2")


}

tasks.test {
	useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.RW-HPS"
			artifactId = "Server-Core"
			description = "Dedicated to Rusted Warfare(RustedWarfare) High Performance Server"
			version = "1.0.0"

			from(components.getByName("java"))

			pom {
				scm {
					url.set("https://github.com/RW-HPS/RW-HPS")
					connection.set("scm:https://github.com/RW-HPS/RW-HPS.git")
					developerConnection.set("scm:git@github.com:RW-HPS/RW-HPS.git")
				}

				licenses {
					license {
						name.set("GNU AGPLv3")
						url.set("https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE")
					}
				}

				developers {
					developer {
						id.set("RW-HPS")
						name.set("RW-HPS Technologies")
					}
				}

			}

			pom.withXml {
				val root = asNode()
				root.appendNode("description", project.description)
				root.appendNode("name", project.name)
				root.appendNode("url", "https://github.com/RW-HPS/RW-HPS")
			}
		}
	}
}
