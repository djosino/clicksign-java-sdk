import java.nio.file.Files

plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
}

group = "com.clicksign"
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
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.2")
}

tasks.test {
    useJUnitPlatform()
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
                url.set("https://github.com/clicksign/clicksign-java-sdk")
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
                    connection.set("scm:git:git://github.com/clicksign/clicksign-java-sdk.git")
                    url.set("https://github.com/clicksign/clicksign-java-sdk")
                }
            }
        }
    }
}
