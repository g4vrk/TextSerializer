import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import util.VersionUtility

plugins {
    base
    alias(libs.plugins.shadow) apply false
}


group = "com.g4vrk"

val baseVersion = "1.0.0"
version = VersionUtility.version(project, baseVersion)

description = "Text serializing for Minecraft stuff (Adventure components)"


subprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    group = rootProject.group
    version = rootProject.version
    description = rootProject.description


    repositories {

        mavenCentral()

        maven {

            url = uri("https://repo.papermc.io/repository/maven-public/")

        }

        maven {

            url = uri("https://jitpack.io/")

        }

    }

    plugins.withType<JavaPlugin> {

        dependencies {
            "testImplementation"(
                platform(rootProject.libs.junit.bom)
            )

            "testImplementation"(
                rootProject.libs.junit.jupiter
            )

            "testRuntimeOnly"(
                rootProject.libs.junit.platform.launcher
            )
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    extensions.configure<JavaPluginExtension> {

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()

    }


    tasks {

        withType<JavaCompile>().configureEach {

            options.encoding = "UTF-8"

        }

        withType<ShadowJar>().configureEach {

            archiveFileName = "${rootProject.name}-${project.name}-${project.version}.jar"
            archiveClassifier = ""

            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            mergeServiceFiles()

        }

        named("build") {

            dependsOn(named("shadowJar"))

        }

    }


    extensions.configure<PublishingExtension> {

        publications {

            create<MavenPublication>("maven") {

                groupId = System.getenv("GROUP") ?: project.group.toString()
                artifactId = project.name
                version = System.getenv("VERSION") ?: project.version.toString()

                artifact(tasks.named("shadowJar"))
                artifact(tasks.named("sourcesJar"))

                pom {
                    name.set("${rootProject.name}-${project.name}")
                    description.set(provider { project.description })
                }

            }

        }

    }

}


tasks {

    clean {

        dependsOn(subprojects.map { "${it.path}:clean" })

    }

    build {

        dependsOn(subprojects.map { "${it.path}:build" })

    }

}

defaultTasks("clean", "build")
