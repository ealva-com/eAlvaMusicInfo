/*
 * Copyright (c) 2021  Eric A. Snell
 *
 * This file is part of eAlvaMusicInfo
 *
 * eAlvaMusicInfo is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version
 * 3 of  the License, or (at your option) any later version.
 *
 * eAlvaMusicInfo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * eAlvaMusicInfo. If not, see <http://www.gnu.org/licenses/>.
 */

plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  compileSdk = SdkVersion.COMPILE

  defaultConfig {
    minSdk = SdkVersion.MIN
    targetSdk = SdkVersion.TARGET

    applicationId = AppCoordinates.APP_ID
    versionCode = AppCoordinates.APP_VERSION_CODE
    versionName = AppCoordinates.APP_VERSION_NAME
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    signingConfig = signingConfigs.getByName("debug")
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
    }
  }

  buildFeatures {
    // Enables Jetpack Compose for this module
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Libs.AndroidX.Compose.COMPILER_VERSION
  }

  lint {
    warningsAsErrors = false
    abortOnError = false
  }

  kotlinOptions {
    jvmTarget = "1.8"
    languageVersion = "1.5"
    apiVersion = "1.5"
    suppressWarnings = false
    verbose = true
    freeCompilerArgs = listOf(
      "-Xopt-in=kotlin.RequiresOptIn",
    )
  }
}

dependencies {
  coreLibraryDesugaring(Libs.DESUGAR)
  implementation(project(":musicinfo"))
  implementation(kotlin("stdlib-jdk8"))

  implementation(Libs.AndroidX.APPCOMPAT)
  implementation(Libs.AndroidX.Activity.COMPOSE)
  implementation("com.android.support.constraint:constraint-layout:2.0.4")
  implementation(Libs.AndroidX.Ktx.CORE)
  implementation(Libs.AndroidX.Lifecycle.RUNTIME_KTX)
  implementation(Libs.AndroidX.Compose.UI)
  implementation(Libs.AndroidX.Compose.MATERIAL)
  implementation(Libs.AndroidX.Compose.TOOLING)

  implementation(Libs.Result.RESULT)
  implementation(Libs.Result.COROUTINES)

  implementation("androidx.activity:activity-ktx:1.4.0")
  implementation("androidx.fragment:fragment-ktx:1.4.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
  implementation("androidx.lifecycle:lifecycle-common-java8:2.4.0")
  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
  implementation("com.google.android.material:material:1.5.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

  implementation(Libs.EAlvaLog.EALVALOG)
  implementation(Libs.EAlvaLog.CORE)
  implementation(Libs.EAlvaLog.ANDROID)

  implementation(Libs.Koin.CORE)
  implementation(Libs.Koin.ANDROID)

  testImplementation(Libs.JUnit.JUNIT)
  testImplementation(Libs.AndroidX.Test.CORE)
  testImplementation(Libs.AndroidX.Test.RULES)
  testImplementation(Libs.Expect.EXPECT)
}
