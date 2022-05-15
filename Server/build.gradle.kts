plugins {
	kotlin("jvm") version "1.6.10"
	`maven-publish`
}

//Netty Version
val nettyVersion = "4.1.75.Final"

/**
 * Fuck implementation
 */
dependencies {
	api("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")

//	api(project(":Util"))
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
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
		attributes(mapOf("Main-Class" to "cn.rwhps.server.Main"))
		attributes(mapOf("Launcher-Agent-Class" to  "cn.rwhps.server.dependent.LibraryManager"))
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.RW-HPS"
			artifactId = "Server"
			description = "Dedicated to Rusted Warfare(RustedWarfare) High Performance Server"
			version = "1.0.0"

			from (components.getByName("java"))
			//from (components.getByName("kotlin"))

			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}

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