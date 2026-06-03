import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

// Load and bump version from version.properties
val versionPropertiesFile = file("version.properties")
val versionProperties = Properties()
if (versionPropertiesFile.exists()) {
    FileInputStream(versionPropertiesFile).use { fis ->
        versionProperties.load(fis)
    }
}

var currentVersionCode = (versionProperties.getProperty("VERSION_CODE") ?: "1").toInt()
var currentVersionName = versionProperties.getProperty("VERSION_NAME_BASE") ?: "1.0"

// Determine if we are building to bump version
val isBuildTask = gradle.startParameter.taskNames.any { taskName ->
    val lower = taskName.lowercase()
    lower.contains("assemble") || lower.contains("bundle") || lower.contains("install") || lower.contains("build") || lower.contains("package")
}

if (isBuildTask) {
    currentVersionCode += 1
    
    // Bump version name (e.g. 1.0 -> 1.1)
    val parts = currentVersionName.split(".")
    currentVersionName = if (parts.isNotEmpty()) {
        val lastIdx = parts.size - 1
        val lastVal = parts[lastIdx].toIntOrNull()
        if (lastVal != null) {
            val updatedParts = parts.toMutableList()
            updatedParts[lastIdx] = (lastVal + 1).toString()
            updatedParts.joinToString(".")
        } else {
            "${currentVersionName}.1"
        }
    } else {
        "1.1"
    }

    versionProperties.setProperty("VERSION_CODE", currentVersionCode.toString())
    versionProperties.setProperty("VERSION_NAME_BASE", currentVersionName)
    FileOutputStream(versionPropertiesFile).use { fos ->
        versionProperties.store(fos, "Auto-incremented during build")
    }
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.hrshd1eux.teatracker"
    minSdk = 24
    targetSdk = 36
    versionCode = currentVersionCode
    versionName = currentVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val hasReleaseKey = !System.getenv("STORE_PASSWORD").isNullOrEmpty() && !System.getenv("KEY_PASSWORD").isNullOrEmpty()
      signingConfig = if (hasReleaseKey) {
        signingConfigs.getByName("release")
      } else {
        signingConfigs.getByName("debug")
      }
    }
    debug {
      // signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}


// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
