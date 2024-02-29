plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.21"
  id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.bekmnsrw.plugin.recomposer"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

intellij {
  localPath.set("C:\\Users\\ilyab\\AppData\\Local\\Programs\\Android Studio 2")
  plugins.set(listOf("Kotlin", "java"))
}

tasks {
  withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
  }

  patchPluginXml {
    sinceBuild.set("231")
    untilBuild.set("241.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
