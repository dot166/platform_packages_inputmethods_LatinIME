use chrono::NaiveDate;
use regex::Regex;
use std::collections::HashMap;
use std::env;
use std::error::Error;
use std::path::Path;
use std::{fs, process::Command};

#[derive(Debug, Clone)]
struct DictEntry {
    url: String,
    date: NaiveDate,
}

#[derive(Debug)]
struct LanguageComparison {
    normal: Option<DictEntry>,
    experimental: Option<DictEntry>,
}

fn dict_new() -> Result<HashMap<String, String>, Box<dyn Error>> {
    let raw_url = "https://codeberg.org/Helium314/aosp-dictionaries/raw/branch/main/README.md";
    let client = reqwest::blocking::Client::builder()
        .user_agent("gen-dicts/0.1.0 (https://github.com/dot166/platform_packages_inputmethods_LatinIME)")
        .build()?;
    let body = client.get(raw_url).send()?.text()?;
    let row_regex = Regex::new(
        r"(?x)
        ^\|\s*([^|]+?)\s*
        \|\s*\[([^]]+)]\(([^)]+)\)\s*
        \|\s*(yes|no)\s*
        \|\s*[^|]+\s*
        \|\s*[^|]+\s*
        \|\s*(\d{4}-\d{2}-\d{2})\s*\|
    ",
    )?;
    let mut language_groups: HashMap<String, LanguageComparison> = HashMap::new();
    for line in body.lines() {
        if let Some(caps) = row_regex.captures(line) {
            let language = caps[1].trim().to_string();
            let dict_type = caps[2].trim().to_string();
            let url = caps[3].trim().to_string();
            let is_experimental = &caps[4] == "yes";
            let date = match NaiveDate::parse_from_str(&caps[5], "%Y-%m-%d") {
                Ok(d) => d,
                Err(_) => continue, // Skip rows with invalid dates
            };
            if dict_type != "main" {
                continue;
            }
            let entry = DictEntry {
                url,
                date,
            };
            let comp = language_groups.entry(language).or_insert(LanguageComparison {
                normal: None,
                experimental: None,
            });
            if is_experimental {
                comp.experimental = Some(entry);
            } else {
                comp.normal = Some(entry);
            }
        }
    }
    let mut languages: Vec<&String> = language_groups.keys().collect();
    languages.sort();
    let mut results: HashMap<String, String> = HashMap::new();
    for lang in languages {
        if let Some(comp) = language_groups.get(lang) {
            match (&comp.normal, &comp.experimental) {
                (Some(normal), Some(experimental)) => {
                    if experimental.date > normal.date {
                        results.insert(format_locale(&experimental.url), experimental.url.clone());
                    } else {
                        results.insert(format_locale(&normal.url), normal.url.clone());
                    }
                }
                (Some(normal), None) => {
                    results.insert(format_locale(&normal.url), normal.url.clone());
                }
                (None, Some(experimental)) => {
                    results.insert(format_locale(&experimental.url), experimental.url.clone());
                }
                (None, None) => {
                    eprintln!("No dictionary available for {}", lang);
                }
            }
        }
    }

    Ok(results)
}

fn main() {
    let contents = Vec::from([
        // stuff I use
        "en_gb",
        //"ja", // LatinIME updates mozc already, no need to update it separately here
        // bundled AOSP dicts
        "en",
        "de",
        "es",
        "fr",
        "it",
        "pt_br",
        "ru",
        // potentially ones I want to support in the future go below this comment
    ]);

    let jobs: Vec<String> = contents
        .into_iter()
        .map(String::from)
        .collect();

    let mut count = 0;

    for job in jobs {
        let result: Result<(), String>;
        result = process_dict(job.clone());
        if let Err(e) = result {
            panic!("{} failure: {}", job, e);
        } else if let Ok(()) = result {
            count += 1;
        }
    }

    println!("Successfully processed {} dictionaries.", count);
    env::set_current_dir(&Path::new("../..")).unwrap();
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
        .arg("[AUTO] Update dicts")
        .status();

    if let Err(e) = status {
        panic!("Error committing changes: {}", e);
    }
}

fn process_dict(
    job: String,
) -> Result<(), String> {
    let dict_map = dict_new().unwrap();
    let api_url = dict_map.get(&get_loc(&job)).unwrap();

    let client = reqwest::blocking::Client::builder()
        .user_agent("gen-dicts/0.1.0 (https://github.com/dot166/platform_packages_inputmethods_LatinIME)")
        .build()
        .unwrap();

    let resp = client
        .get(api_url)
        .send()
        .map_err(|e| format!("HTTP error: {}", e))?;

    if !resp.status().is_success() {
        return Err(format!("Codeberg returned {}", resp.status()));
    }

    let body = resp.text()
        .map_err(|e| format!("read body: {}", e))?;

    let _ = fs::write(format!("../../java/res/raw/main_{}.dict", job.to_lowercase()), &body);

    Ok(())
}

fn format_locale(url: &String) -> String {
    let regex = Regex::new("main_.*\\.").unwrap();
    regex.captures(&*url).unwrap()[0].trim().to_string().replace("main_", "").replace(".", "")
}

fn get_loc(loc: &String) -> String {
    if loc.len() == 2 {
        if loc == "en" {
            // I want to use en_gb, but AOSP maps en to en_us, so, match it
            println!("using en_us for locale en");
            "en_us".to_string()
        } else {
            loc.to_lowercase()
            //format!("{}_{}", loc.to_lowercase(), loc.to_lowercase())
        }
    } else {
        loc.to_lowercase()
    }
}
