import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    libs.plugins.run {
        alias(kt.jvm)
        alias(kt.serialization)
        alias(dokka)
        alias(detekt)
        alias(kover)
        alias(ktlint)
    }
    `java-library`
    `maven-publish`
}

val jvmTargetVersion = JvmTarget.JVM_17

val reportFolder = rootProject.file("reports")
val generatedFolder = layout.buildDirectory.dir("generated").get()
val dokkaOutputDir = rootProject.file("dokka")

detekt {
    ignoreFailures = System.getenv("DETEKT_IGNORE_FAILURES")?.toBooleanStrictOrNull() ?: false
    config.from(file("config/detekt/detekt.yml"))
    reportsDir = reportFolder.resolve("detekt")
}

kover {
    reports {
        val reportKoverFolder = reportFolder.resolve("kover")

        total {
            xml {
                this.xmlFile.set(reportKoverFolder.resolve("xml/result.xml"))
            }
            html {
                this.htmlDir.set(reportKoverFolder.resolve("html"))
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.bundles.ktor)
    api(libs.kt.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.kt.coroutines.test)
    testImplementation(libs.ktor.cio)
    testImplementation(libs.ktor.logging)
    testImplementation(libs.slf4j.simple)
}

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
    jvmToolchain(jvmTargetVersion.target.toInt())

    sourceSets {
        main {
            kotlin {
                srcDir(generatedFolder)
            }
        }

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = jvmTargetVersion
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        reporters {
            reporter(ReporterType.HTML)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    withType<org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask> {
        reportsOutputDirectory.set(reportFolder.resolve("klint/$name"))
    }

    test {
        useJUnitPlatform()
    }

    clean {
        delete(dokkaOutputDir)
    }

    val deleteDokkaOutputDir by register<Delete>("deleteDokkaOutputDirectory") {
        group = "documentation"
        delete(dokkaOutputDir)
    }

    dokkaHtml.configure {
        // CompileJava should be executed to build library in Jitpack
        dependsOn(deleteDokkaOutputDir, compileJava.get())
        outputDirectory.set(dokkaOutputDir)
    }

    withType<Detekt>().configureEach {
        jvmTarget = jvmTargetVersion.target
        exclude("**/${generatedFolder.asFile.name}/**")

        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar =
    tasks.register<Jar>("javadocJar") {
        group = "documentation"
        dependsOn(tasks.dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaOutputDir)
    }

publishing {
    val projectName = project.name

    publications {
        val projectOrganizationPath = "Rushyverse/$projectName"
        val projectGitUrl = "https://github.com/$projectOrganizationPath"

        create<MavenPublication>(projectName) {
            from(components["kotlin"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            pom {
                name.set(projectName)
                description.set(project.description)
                url.set(projectGitUrl)

                issueManagement {
                    system.set("GitHub")
                    url.set("$projectGitUrl/issues")
                }

                ciManagement {
                    system.set("GitHub Actions")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org")
                    }
                }

                developers {
                    developer {
                        name.set("Distractic")
                        email.set("Distractic@outlook.fr")
                        url.set("https://github.com/Distractic")
                    }
                }

                scm {
                    connection.set("scm:git:$projectGitUrl.git")
                    developerConnection.set("scm:git:git@github.com:$projectOrganizationPath.git")
                    url.set(projectGitUrl)
                }

                distributionManagement {
                    downloadUrl.set("$projectGitUrl/releases")
                }
            }
        }
    }
}
