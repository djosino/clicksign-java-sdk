plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.register<JavaExec>("runRetries") {
    group = "examples"
    description = "Executa RetriesExample (requer CLICKSIGN_API_KEY)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.clicksign.examples.RetriesExample")
}

tasks.register<JavaExec>("runBulkRequirements") {
    group = "examples"
    description = "Executa BulkRequirementsExample"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.clicksign.examples.BulkRequirementsExample")
}

tasks.register<JavaExec>("runWebhooks") {
    group = "examples"
    description = "Executa WebhooksExample"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.clicksign.examples.WebhooksExample")
}

tasks.register<JavaExec>("runMultiClient") {
    group = "examples"
    description = "Executa MultiClientExample"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.clicksign.examples.MultiClientExample")
}

tasks.register<JavaExec>("runListAndFilter") {
    group = "examples"
    description = "Executa ListAndFilterExample"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.clicksign.examples.ListAndFilterExample")
}

tasks.register<JavaExec>("runProductionLimitations") {
    group = "examples"
    description = "Executa ProductionLimitationsExample"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.clicksign.examples.ProductionLimitationsExample")
}
