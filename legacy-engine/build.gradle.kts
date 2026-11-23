dependencies {
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.text.gson)
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

tasks.shadowJar {
    relocate("net.kyori", "com.g4vrk.text.internal.kyori")
    relocate("com.google.gson", "com.g4vrk.text.internal.gson")
}
