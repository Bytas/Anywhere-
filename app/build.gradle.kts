import com.android.build.api.component.analytics.AnalyticsEnabledApplicationVariant
import com.android.build.api.variant.impl.ApplicationVariantImpl
import com.android.build.gradle.internal.dsl.BuildType
import java.nio.file.Paths

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("sdk-editor")
}

val verName = "2.3.2"
val verCode = 2030200

android {
    compileSdk = 31
    buildToolsVersion = "31.0.0"
    ndkVersion = "21.4.7075529"

    defaultConfig {
        applicationId = "com.absinthe.anywhere_"
        minSdk = 23
        targetSdk = 31
        versionCode = verCode
        versionName = verName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appName"] = "Anywhere-"
        ndk {
            abiFilters += arrayOf("armeabi-v7a", "arm64-v8a")
        }
        resourceConfigurations += arrayOf("en", "zh-rCN", "zh-rTW", "zh-rHK")
    }

    kapt {
        arguments {
            arg("room.incremental", "true")
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "Anywhere-β"
            buildConfigField("boolean", "BETA", "true")
        }
        release {
            isMinifyEnabled = true
            (this as BuildType).isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "BETA", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    androidComponents.onVariants { v ->
        val variant: ApplicationVariantImpl =
            if (v is ApplicationVariantImpl) v
            else (v as AnalyticsEnabledApplicationVariant).delegate as ApplicationVariantImpl
        variant.outputs.forEach {
            it.outputFileName.set("Anywhere-${verName}-${verCode}-${variant.name}.apk")
        }
    }

    dependenciesInfo.includeInApk = false

//    externalNativeBuild {
//        cmake {
//            path = file("CMakeLists.txt")
//        }
//    }

    packagingOptions {
        resources {
            excludes += "META-INF/**"
            excludes += "okhttp3/**"
            excludes += "kotlin/**"
            excludes += "org/**"
            excludes += "**.properties"
            excludes += "**.bin"
        }
    }

    lint {
        isAbortOnError = true
        isCheckReleaseBuilds = false
    }

    sourceSets {
        named("main") {
            jniLibs.srcDir("libs")
        }
    }
}

repositories {
    mavenCentral()
}

val optimizeReleaseRes = task("optimizeReleaseRes").doLast {
    val aapt2 = File(
        androidComponents.sdkComponents.sdkDirectory.get().asFile,
        "build-tools/${project.android.buildToolsVersion}/aapt2"
    )
    val zip = Paths.get(
        project.buildDir.path,
        "intermediates",
        "shrunk_processed_res",
        "release",
        "resources-release-stripped.ap_"
    )
    val optimized = File("${zip}.opt")
    val cmd = exec {
        commandLine(
            aapt2, "optimize",
            "--collapse-resource-names",
            "--shorten-resource-paths",
            "--resources-config-path", "aapt2-resources.cfg",
            "-o", optimized,
            zip
        )
        isIgnoreExitValue = false
    }
    if (cmd.exitValue == 0) {
        delete(zip)
        optimized.renameTo(zip.toFile())
    }
}

tasks.whenTaskAdded {
    if (name == "shrinkReleaseRes") {
        finalizedBy(optimizeReleaseRes)
    }
}

configurations.all {
    exclude(group = "androidx.appcompat", module = "appcompat")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(files("libs/color-picker.aar"))
    implementation(files("libs/IceBox-SDK-1.0.5.aar"))
    implementation(files("libs/speed-dial-3.1.1.aar"))
//    implementation(files("libs/blurkit-1.1.1.aar"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1")

    implementation("com.github.zhaobozhen.libraries:me:1.0.2")
    implementation("com.github.zhaobozhen.libraries:utils:1.0.2")

    val appCenterSdkVersion = "4.2.0"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")

    //Android X
    val roomVersion = "2.3.0"
    implementation("androidx.room:room-runtime:${roomVersion}")
    implementation("androidx.room:room-ktx:${roomVersion}")
    kapt("org.xerial:sqlite-jdbc:3.36.0.1") //Work around on Apple Silicon
    kapt("androidx.room:room-compiler:${roomVersion}")
    androidTestImplementation("androidx.room:room-testing:${roomVersion}")

    val lifecycleVersion = "2.3.1"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycleVersion}")

    implementation("androidx.browser:browser:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")

    //KTX
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("androidx.activity:activity-ktx:1.3.0")
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.preference:preference-ktx:1.1.1")

    //Google
    implementation("com.google.android.material:material:1.4.0")

    //Function
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.code.gson:gson:2.8.7")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.blankj:utilcodex:1.30.6")
    implementation("com.tencent:mmkv-static:1.2.10")
    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.6")
    implementation("com.github.heruoxin.Delegated-Scopes-Manager:client:master-SNAPSHOT")
    implementation("com.github.topjohnwu.libsu:core:3.1.1")
    implementation("com.github.thegrizzlylabs:sardine-android:0.8")
    implementation("com.jonathanfinerty.once:once:1.3.1")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:2.0")

    //UX
    implementation("com.drakeet.about:about:2.4.1")
    implementation("com.drakeet.about:about-extension:2.4.1")
    implementation("com.drakeet.multitype:multitype:4.3.0")
    implementation("com.drakeet.drawer:drawer:1.0.3")
    implementation("com.github.sephiroth74:android-target-tooltip:2.0.4")
    implementation("me.zhanghai.android.fastscroll:library:1.1.7")

    val shizukuVersion = "11.0.3"
    // required by Shizuku and Sui
    implementation("dev.rikka.shizuku:api:$shizukuVersion")
    // required by Shizuku
    implementation("dev.rikka.shizuku:provider:$shizukuVersion")

    implementation("dev.rikka.rikkax.appcompat:appcompat:1.2.0-rc01")
    implementation("dev.rikka.rikkax.core:core:1.3.2")
    implementation("dev.rikka.rikkax.material:material:1.6.5")
    implementation("dev.rikka.rikkax.recyclerview:recyclerview-ktx:1.2.2")
    implementation("dev.rikka.rikkax.widget:borderview:1.0.1")
    implementation("dev.rikka.rikkax.preference:simplemenu-preference:1.0.3")
    implementation("dev.rikka.rikkax.insets:insets:1.1.0")

    //Network
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okio:okio:2.10.0")

    //Rx
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("org.reactivestreams:reactive-streams:1.0.3")

    //Debug
    testImplementation("junit:junit:4.13.2")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}