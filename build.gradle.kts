plugins {
    base
}

tasks.register("buildPlugin") {
    dependsOn(":intellij:buildPlugin")
}

tasks.register("runIde") {
    dependsOn(":intellij:runIde")
}
