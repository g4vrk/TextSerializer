dependencies {
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.text.gson)
}

tasks.shadowJar {
    relocate("net.kyori", "com.g4vrk.text.internal.kyori")
    relocate("com.google.gson", "com.g4vrk.text.internal.gson")
}
