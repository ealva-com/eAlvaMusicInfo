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
  compileSdkVersion(Sdk.COMPILE_SDK_VERSION)

  defaultConfig {
    minSdkVersion(Sdk.MIN_SDK_VERSION)
    targetSdkVersion(Sdk.TARGET_SDK_VERSION)

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

  lint {
    isWarningsAsErrors = false
    isAbortOnError = false
  }

  kotlinOptions {
    jvmTarget = "1.8"
    languageVersion = "1.5"
    apiVersion = "1.5"
    suppressWarnings = false
    verbose = true
    freeCompilerArgs = listOf(
      "-XXLanguage:+InlineClasses",
      "-Xinline-classes",
      "-Xopt-in=kotlin.RequiresOptIn",
      "-Xuse-14-inline-classes-mangling-scheme",
      "-Xskip-prerelease-check"
    )
  }
}

dependencies {
  coreLibraryDesugaring(ToolsLib.DESUGARING)
  implementation(project(":musicinfo"))
  implementation(kotlin("stdlib-jdk8"))

  implementation(SupportLibs.ANDROIDX_APPCOMPAT)
  implementation(SupportLibs.ANDROIDX_CONSTRAINT_LAYOUT)
  implementation(SupportLibs.ANDROIDX_CORE_KTX)
  implementation(SupportLibs.ANDROIDX_LIFECYCLE_RUNTIME_KTX)

  implementation(ThirdParty.KOTLIN_RESULT)

  implementation("androidx.activity:activity-ktx:1.2.2")
  implementation("androidx.fragment:fragment-ktx:1.3.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
  implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
  implementation("com.google.android.material:material:1.3.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")

  implementation(ThirdParty.EALVALOG)
  implementation(ThirdParty.EALVALOG_CORE)
  implementation(ThirdParty.EALVALOG_ANDROID)

  implementation(ThirdParty.KOIN)
  implementation(ThirdParty.KOIN_ANDROID)

  testImplementation(TestingLib.JUNIT)
  testImplementation(AndroidTestingLib.ANDROIDX_TEST_CORE) {
    exclude("junit", "junit")
  }
  testImplementation(AndroidTestingLib.ANDROIDX_TEST_RULES) {
    exclude("junit", "junit")
  }
  testImplementation(TestingLib.EXPECT)
}
