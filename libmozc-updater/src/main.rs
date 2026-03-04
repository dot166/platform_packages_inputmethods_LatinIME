use std::path::{Path, PathBuf};
use std::process::Command;
use std::{env, fs};

pub fn get_script_dir() -> Option<PathBuf> {
    let top_file = "libmozc-updater/.gitignore";

    // Check the current directory and navigate upwards if necessary
    let current_dir = env::current_dir().unwrap();
    let mut pwd = current_dir.clone();

    while pwd != Path::new("/") {
        let top_path = pwd.join(top_file);
        if top_path.exists() {
            return Some(pwd);
        }
        pwd = pwd.parent().unwrap_or(Path::new("/")).to_path_buf();
    }

    None
}

fn main() {
    unsafe { env::set_var("IS_CI", "true"); }
    env::set_current_dir(&get_script_dir().unwrap()).expect("Failed to change directory");
    let status = Command::new("git").arg("clone").arg("https://github.com/google/mozc.git").status().unwrap();
    if !status.success() {
        panic!("Failed to clone mozc");
    }
    env::set_current_dir(&Path::new("mozc/src")).unwrap();
    let status = Command::new("bazelisk").arg("build").arg("package").arg("--config").arg("oss_android").arg("--config").arg("release_build").status().unwrap();
    if !status.success() {
        panic!("Failed to build mozc");
    }
    let status = Command::new("unzip").arg("bazel-bin/android/jni/native_libs.zip").status().unwrap();
    if !status.success() {
        panic!("Failed to unzip mozc");
    }
    copy_dir_all("libs", "../../java/lib");
    let status = Command::new("bazelisk").arg("build").arg("//data_manager/oss:mozc_dataset_for_oss").arg("--config").arg("linux").arg("--config").arg("release_build").status().unwrap();
    if !status.success() {
        panic!("Failed to build mozc");
    }
    fs::create_dir_all("../../java/assets");
    fs::copy("bazel-bin/data_manager/oss/mozc.data", "../../java/assets/mozc.data");
    copy_proto_files("protocol", "../../java/proto/protocol");
    env::set_current_dir(&Path::new("../..")).unwrap();
    fs::remove_dir_all("mozc");
    let status = Command::new("git")
        .arg("add")
        .arg(".")
        .status();

    if let Err(e) = status {
        panic!("Error adding changes: {}", e);
    }
    let status = Command::new("git")
        .arg("commit")
        .arg("-m")
        .arg("LatinIME: Updated libmozc")
        .status();

    if let Err(e) = status {
        panic!("Error committing changes: {}", e);
    }

    let status = Command::new("git")
        .arg("push")
        .status();

    if let Err(e) = status {
        panic!("Error pushing changes: {}", e);
    }
}

fn copy_dir_all(src: impl AsRef<Path>, dst: impl AsRef<Path>) -> std::io::Result<()> {
    fs::create_dir_all(&dst)?;
    for entry in fs::read_dir(src)? {
        let entry = entry?;
        let ty = entry.file_type()?;
        let dest_path = dst.as_ref().join(entry.file_name());

        if ty.is_dir() {
            copy_dir_all(entry.path(), dest_path)?;
        } else {
            fs::copy(entry.path(), dest_path)?;
        }
    }
    Ok(())
}

fn copy_proto_files(src: impl AsRef<Path>, dst: impl AsRef<Path>) {
    let src_path = src.as_ref();
    let dst_path = dst.as_ref();
    fs::create_dir_all(dst_path).expect("Failed to create destination directory");
    for entry in fs::read_dir(src_path).expect("Failed to read source directory") {
        let entry = entry.expect("Failed to read directory entry");
        let entry_path = entry.path();

        if entry_path.is_dir() {
            let sub_dir = dst_path.join(entry.file_name());
            copy_proto_files(entry_path, sub_dir);
        } else if entry_path.extension() == Some(std::ffi::OsStr::new("proto")) {
            let dest_file = dst_path.join(entry.file_name());
            fs::copy(&entry_path, &dest_file).expect("Failed to copy proto file");
            let file_content = fs::read_to_string(&dest_file).expect("Failed to read proto file");
            let updated_content = file_content.replace(
                "import \"protocol/", 
                "import \"packages/inputmethods/LatinIME/java/proto/protocol/"
            );

            fs::write(dest_file, updated_content).expect("Failed to write updated proto file");
        }
    }
}
