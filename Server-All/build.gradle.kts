dependencies {
	implementation(project(":Server-Core"))
}

tasks.jar {
	// Fuck Java 9
	exclude("**/module-info.class")

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

	from(configurations.runtimeClasspath.get().map {
		if (it.isDirectory) it else zipTree(it)
	})
}
