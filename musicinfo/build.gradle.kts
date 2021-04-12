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
version = MusicInfoCoordinates.LIBRARY_VERSION

plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("kapt")
  id("org.jetbrains.dokka")
  id("com.vanniktech.maven.publish")
}

val localProperties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir)
val musicInfoAppName: String = localProperties.getProperty("MUSICINFO_APP_NAME", "\"\"")
val musicInfoAppVersion: String = localProperties.getProperty("MUSICINFO_APP_VERSION", "\"\"")
val musicInfoEmail: String = localProperties.getProperty("MUSICINFO_CONTACT_EMAIL", "\"\"")
val lastFmApiKey: String = localProperties.getProperty("LASTFM_API_KEY", "\"\"")

android {
  compileSdkVersion(Sdk.COMPILE_SDK_VERSION)

  defaultConfig {
    minSdkVersion(Sdk.MIN_SDK_VERSION)
    targetSdkVersion(Sdk.TARGET_SDK_VERSION)

    versionCode = MusicInfoCoordinates.LIBRARY_VERSION_CODE
    versionName = MusicInfoCoordinates.LIBRARY_VERSION

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  packagingOptions {
    resources {
      excludes += listOf(
        "META-INF/AL2.0",
        "META-INF/LGPL2.1"
      )
    }
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
      "-Xexplicit-api=warning",
      "-Xuse-14-inline-classes-mangling-scheme"
    )
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    getByName("debug") {
      buildConfigField("String", "MUSICINFO_APP_NAME", musicInfoAppName)
      buildConfigField("String", "MUSICINFO_APP_VERSION", musicInfoAppVersion)
      buildConfigField("String", "MUSICINFO_CONTACT_EMAIL", musicInfoEmail)
      buildConfigField("String", "LASTFM_API_KEY", lastFmApiKey)
      isTestCoverageEnabled = false
    }

    getByName("release") {
      isMinifyEnabled = false
    }
  }

  sourceSets {
    val sharedTestDir = "src/sharedTest/java"
    getByName("test").java.srcDir(sharedTestDir)
    getByName("androidTest").java.srcDir(sharedTestDir)
  }

  lint {
    isWarningsAsErrors = false
    isAbortOnError = false
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
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
      "-Xexplicit-api=warning",
      "-Xuse-14-inline-classes-mangling-scheme"
    )
  }
}

dependencies {
  coreLibraryDesugaring(ToolsLib.DESUGARING)
  implementation(kotlin("stdlib-jdk8"))
  implementation(SupportLibs.ANDROIDX_APPCOMPAT)
  implementation(SupportLibs.ANDROIDX_CORE_KTX)
  implementation(SupportLibs.ANDROIDX_STARTUP)
  implementation(ThirdParty.EALVABRAINZ)
  implementation(ThirdParty.EALVABRAINZ_SERVICE)
  implementation(ThirdParty.EALVALOG)
  implementation(ThirdParty.EALVALOG_CORE)
  implementation(ThirdParty.COROUTINE_CORE)
  implementation(ThirdParty.COROUTINE_ANDROID)

  implementation(ThirdParty.RETROFIT)
  implementation(ThirdParty.MOSHI)
  implementation(ThirdParty.MOSHI_RETROFIT)
  implementation(ThirdParty.OKHTTP)
  implementation(ThirdParty.OKHTTP_LOGGING)
  kapt(ThirdParty.MOSHI_CODEGEN)

  implementation(ThirdParty.KOTLIN_RESULT)

  testImplementation(TestingLib.JUNIT)
  testImplementation(AndroidTestingLib.ANDROIDX_TEST_CORE) {
    exclude("junit", "junit")
  }
  testImplementation(AndroidTestingLib.ANDROIDX_TEST_RULES) {
    exclude("junit", "junit")
  }
  testImplementation(TestingLib.EXPECT)
  testImplementation(TestingLib.ROBOLECTRIC)
  testImplementation(TestingLib.COROUTINE_TEST)
  testImplementation(TestingLib.MOCKITO_KOTLIN)
  testImplementation(TestingLib.MOCKITO_INLINE)

  androidTestImplementation(AndroidTestingLib.ANDROIDX_TEST_RUNNER) {
    exclude("junit", "junit")
  }
  androidTestImplementation(AndroidTestingLib.ANDROIDX_TEST_EXT_JUNIT) {
    exclude("junit", "junit")
  }
  androidTestImplementation(TestingLib.JUNIT)
  androidTestImplementation(TestingLib.EXPECT)
  androidTestImplementation(TestingLib.COROUTINE_TEST)
}
