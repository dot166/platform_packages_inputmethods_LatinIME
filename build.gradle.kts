import com.google.protobuf.gradle.*
plugins {
    id("com.android.application") version "9.2.1"
    id("com.google.protobuf") version "0.10.0"
}

android {
    compileSdk = 36
    namespace = "com.android.inputmethod.latin"

    defaultConfig {
        minSdk = 30
        targetSdk = 34
        versionName = "1.0"

        applicationId = "com.android.inputmethod.latin"
        vectorDrawables.useSupportLibrary = false

        signingConfig = signingConfigs.getByName("debug")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("java/shared.keystore")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard.flags"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    flavorDimensions += "default"

    sourceSets {
        getByName("main") {
            proto {
                srcDir("java/proto-gradle")
            }
            jniLibs.directories.add("java/lib")
            assets.directories.add("java/assets")
            res.directories.add("java/res")
            java.directories.addAll(listOf("common/src", "java/src"))
            manifest.srcFile("java/AndroidManifest-gradle.xml")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("native/jni/AndroidNdk.mk")
        }
    }

    androidResources {
        noCompress.add("dict")
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.protobuf:protobuf-javalite:4.35.0")
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.35.0"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}