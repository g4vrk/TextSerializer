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

tasks {

}
