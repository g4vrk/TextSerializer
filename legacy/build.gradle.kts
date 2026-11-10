dependencies {

    compileOnly(libs.paper.legacy)

    implementation(libs.adventure.text.minimessage.legacy) {
        exclude(group = "net.kyori", module = "adventure-api")
    }

    implementation(project(":common"))
    implementation(project(path = ":legacy-engine", configuration = "shadow"))

    testImplementation(libs.paper.legacy)

}

tasks {

    shadowJar {

        relocate(
            "net.kyori.adventure.text.minimessage",
            "com.g4vrk.text.shaded.adventure.minimessage"
        )

    }

}
