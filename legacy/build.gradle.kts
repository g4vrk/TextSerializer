dependencies {

    compileOnly(libs.paper.legacy)

    implementation(libs.adventure.text.minimessage.legacy) {
        exclude(group = "net.kyori", module = "adventure-api")
    }

    api(project(":common"))

    testImplementation(libs.paper.legacy)

}

// MiniMessage is part of the public TagResolver API and must retain its canonical package.
