rootProject.name = "chunky-littletiles-plugin"

// Include Chunky as a composite build for dependency resolution
includeBuild("../chunky") {
    dependencySubstitution {
        substitute(module("se.llbit:chunky-core")).using(project(":chunky"))
    }
}
