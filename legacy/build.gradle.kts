dependencies {

    compileOnly(libs.paper.legacy)

    implementation(libs.adventure.text.minimessage.legacy) {
        exclude(group = "net.kyori", module = "adventure-api")
    }

    implementation(project(":common"))
    implementation(project(path = ":legacy-engine", configuration = "shadow"))

    testImplementation(libs.paper.legacy)
    testImplementation(libs.adventure.text.minimessage.legacy)

}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.named<JavaCompile>("compileJava") {
    options.release.set(16)
}

tasks.named<JavaCompile>("compileTestJava") {
    options.release.set(17)
}

tasks {

}
