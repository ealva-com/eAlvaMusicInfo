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
version = MusicInfoCoordinates.VERSION

plugins {
  id("com.android.library")
  kotlin("android")
  id("kotlin-parcelize")
  kotlin("kapt")
  id("org.jetbrains.dokka")
  id("com.vanniktech.maven.publish")
}

val localProperties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir)
val musicInfoAppName: String = localProperties.getProperty("MUSICINFO_APP_NAME", "\"\"")
val musicInfoAppVersion: String = localProperties.getProperty("MUSICINFO_APP_VERSION", "\"\"")
val musicInfoEmail: String = localProperties.getProperty("MUSICINFO_CONTACT_EMAIL", "\"\"")
val lastFmApiKey: String = localProperties.getProperty("LASTFM_API_KEY", "\"\"")
val spotifyClientId: String = localProperties.getProperty("SPOTIFY_CLIENT_ID", "\"\"")
val spotifyClientSecret: String = localProperties.getProperty("SPOTIFY_CLIENT_SECRET", "\"\"")

android {
  compileSdk = SdkVersion.COMPILE

  defaultConfig {
    minSdk = SdkVersion.MIN
    targetSdk = SdkVersion.TARGET

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

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    debug {
      buildConfigField("String", "MUSICINFO_APP_NAME", musicInfoAppName)
      buildConfigField("String", "MUSICINFO_APP_VERSION", musicInfoAppVersion)
      buildConfigField("String", "MUSICINFO_CONTACT_EMAIL", musicInfoEmail)
      buildConfigField("String", "LASTFM_API_KEY", lastFmApiKey)
      buildConfigField("String", "SPOTIFY_CLIENT_ID", spotifyClientId)
      buildConfigField("String", "SPOTIFY_CLIENT_SECRET", spotifyClientSecret)
      isTestCoverageEnabled = false
    }

    release {
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
      "-Xopt-in=kotlin.RequiresOptIn",
      "-Xexplicit-api=warning"
    )
  }
}

dependencies {
  coreLibraryDesugaring(Libs.DESUGAR)
  implementation(kotlin("stdlib-jdk8"))
  implementation(Libs.AndroidX.APPCOMPAT)
  implementation(Libs.AndroidX.Ktx.CORE)
  implementation(Libs.AndroidX.STARTUP)

  implementation(Libs.EAlvaBrainz.BRAINZ)
  implementation(Libs.EAlvaBrainz.BRAINZ_SERVICE)
  implementation(Libs.EAlvaLog.EALVALOG)
  implementation(Libs.EAlvaLog.CORE)
  implementation(Libs.Kotlin.Coroutines.CORE)
  implementation(Libs.Kotlin.Coroutines.ANDROID)
  implementation(Libs.Spotify.API)

  implementation(Libs.Square.RETROFIT)
  implementation(Libs.Square.MOSHI)
  implementation(Libs.Square.MOSHI_RETROFIT)
  implementation(Libs.Square.OKHTTP)
  implementation(Libs.Square.OKHTTP_LOGGING)
  kapt(Libs.Square.MOSHI_CODEGEN)

  implementation(Libs.Result.RESULT)
  implementation(Libs.Result.COROUTINES)

  testImplementation(Libs.JUnit.JUNIT)
  testImplementation(Libs.AndroidX.Test.CORE)
  testImplementation(Libs.AndroidX.Test.RULES)
  testImplementation(Libs.Expect.EXPECT)
  testImplementation(Libs.Robolectric.ROBOLECTRIC)
  testImplementation(Libs.Kotlin.Coroutines.TEST)
  testImplementation(Libs.Mockito.KOTLIN)
  testImplementation(Libs.Mockito.INLINE)

  androidTestImplementation(Libs.AndroidX.Test.RUNNER)
  androidTestImplementation(Libs.AndroidX.Test.Ext.JUNIT)
  androidTestImplementation(Libs.JUnit.JUNIT)
  androidTestImplementation(Libs.Expect.EXPECT)
  androidTestImplementation(Libs.Kotlin.Coroutines.TEST)
}
