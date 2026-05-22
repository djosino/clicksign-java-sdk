import java.nio.file.Files

plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
    jacoco
}

group = "com.br.josino.clicksign"
version = Files.readString(rootProject.file("REVISION").toPath()).trim()

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.2")
}

tasks.test {
    useJUnitPlatform {
        if (!project.hasProperty("includeIntegration")) {
            excludeTags("integration")
        }
    }
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            element = "PACKAGE"
            includes = listOf("com.clicksign.resources.notarial")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            element = "PACKAGE"
            includes = listOf("com.clicksign.resources")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            element = "PACKAGE"
            includes = listOf("com.clicksign.resources.types")
            limit {
                counter = "INSTRUCTION"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification, ":examples:compileJava")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

checkstyle {
    toolVersion = "10.14.2"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Clicksign Java SDK")
                description.set("Official Java SDK for the Clicksign e-signature API (v3)")
                url.set("https://github.com/djosino/clicksign-java-sdk")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("clicksign")
                        name.set("Clicksign")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/djosino/clicksign-java-sdk.git")
                    url.set("https://github.com/djosino/clicksign-java-sdk")
                }
            }
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            val isRelease = !version.toString().endsWith("-SNAPSHOT")
            url = uri(
                if (isRelease)
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                else
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            )
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val key = System.getenv("SIGNING_KEY")
    val password = System.getenv("SIGNING_PASSWORD")
    if (key != null && password != null) {
        useInMemoryPgpKeys(key, password)
        sign(publishing.publications["maven"])
    }
}
