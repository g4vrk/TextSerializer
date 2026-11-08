dependencies {

    compileOnly(libs.paper.legacy)
    compileOnly(libs.adventure.text.minimessage.legacy)

    implementation(libs.caffeine)

    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.text.legacy)
    testImplementation(libs.adventure.text.plain)

}
