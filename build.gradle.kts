plugins {
    id("java")
    alias(libs.plugins.shadow)
}

group = "dev.lokspel.deathswap"
version = "1.0.8"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.helpch.at/releases/")
    maven("https://repo.faststats.dev/releases")
    maven("https://registry.whereareiam.me/maven/packages")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.packetevents.spigot)

    implementation(libs.faststats.bukkit)
    implementation(libs.bundles.adventure)
    implementation(libs.bundles.configura)
    implementation(libs.bundles.strata)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.spigot.api)
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
