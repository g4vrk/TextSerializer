dependencies {

    compileOnly(libs.paper.legacy)
    compileOnly(libs.adventure.text.minimessage.legacy)

    implementation(libs.caffeine)

    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.text.legacy)
    testImplementation(libs.adventure.text.plain)

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
