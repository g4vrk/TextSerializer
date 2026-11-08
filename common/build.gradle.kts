dependencies {

    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly(libs.adventure.text.legacy)
    compileOnlyApi(libs.adventure.text.minimessage)

    implementation(libs.caffeine)

    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.text.legacy)
    testImplementation(libs.adventure.text.plain)

}
