plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0"
}

group = "dev.lokspel.deathswap"
version = "1.0.7"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.helpch.at/releases/")
    maven("https://repo.faststats.dev/releases")
    maven("https://registry.whereareiam.me/maven/packages")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:26.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("com.github.retrooper:packetevents-spigot:2.14.0")

    implementation("dev.faststats.metrics:bukkit:0.30.2")

    implementation("net.kyori:adventure-api:5.2.0")
    implementation("net.kyori:adventure-text-serializer-legacy:5.2.0")
    implementation("net.kyori:adventure-text-minimessage:5.2.0")

    implementation("me.whereareiam:configura:2.1.0")
    implementation("me.whereareiam.configura.feature:comment:2.1.0")
    implementation("me.whereareiam:strata:2.0.0")
    implementation("me.whereareiam:strata-adapter-configura:2.0.0")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation("org.spigotmc:spigot-api:26.2-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    exclude("META-INF/versions/*/module-info.class", "module-info.class")
    relocate("net.kyori", "dev.lokspel.deathswap.thirdparty.kyori")
    relocate("me.whereareiam.configura", "dev.lokspel.deathswap.thirdparty.configura")
    relocate("me.whereareiam.strata", "dev.lokspel.deathswap.thirdparty.strata")
    relocate("com.fasterxml.jackson", "dev.lokspel.deathswap.thirdparty.jackson")
    relocate("org.yaml.snakeyaml", "dev.lokspel.deathswap.thirdparty.snakeyaml")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    mergeServiceFiles()
    archiveClassifier.set("")
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}
