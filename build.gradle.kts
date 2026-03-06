import com.google.protobuf.gradle.*
plugins {
    id("com.android.application") version "8.13.2"
    id("com.google.protobuf") version "0.9.6"
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

    sourceSets["main"].proto {
        srcDir("java/proto-gradle")
    }
    sourceSets["main"].jniLibs {
        srcDir("java/lib")
    }
    sourceSets["main"].assets {
        srcDir("java/assets")
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
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard.flags"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    flavorDimensions += "default"

    sourceSets {
        getByName("main") {
            res.srcDirs("java/res")
            java.srcDirs("common/src", "java/src")
            manifest.srcFile("java/AndroidManifest.xml")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("native/jni/AndroidNdk.mk")
        }
    }

    androidResources {
        noCompress("dict")
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
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