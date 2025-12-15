# PLEASE SET UP VERIFIED COMMITS


# 🚀 Project Name

A short description of what project does and why it’s useful.

---

## 📖 Table of Contents
- [About](#-about)
- [Features](#-features)
- [Installation](#-installation)
- [Usage](#-usage)
- [Configuration](#-configuration)

---

## 📌 About
Explain what the project is, its purpose, and any background information.

---

## ✨ Features
- Feature 1
- Feature 2
- Feature 3

---

## ⚙️ Installation

Clone the repo:
```bash
git clone https://github.com/Ugur-Erkal/CS320
cd CS320
```
- [java oracle standard](https://www.oracle.com/docs/tech/java/codeconventions.pdf)

- [commit message standard](https://www.conventionalcommits.org/en/v1.0.0/)


Signed commits:
```bash
Step 1. Generate SSH key

ssh-keygen -t ed25519 -C "your_email@example.com"
// no need to name, just default save 


Step 2. Add public key to GitHub

Show the key:
cat ~/.ssh/id_ed25519.pub 
// use Get-Content $HOME\.ssh\id_ed25519.pub on windows, or just open the file.
Copy it → go to GitHub → Settings → SSH and GPG keys → New SSH key
Choose "Signing key" type.

Step 3. Tell Git to use SSH for signing

git config --global gpg.format ssh
git config --global user.signingkey ~/.ssh/id_ed25519.pub
git config --global commit.gpgsign true
// can be local too if desired.

Step 4. Make a signed commit to test
git commit -S -m "Your signed commit"
```
### testing and other extensions in use:

- Junit 5   
    - unit and integration tests, will be added as CI later

- JaCoCo
    - coverage report, also to be added to CI later

- linter TBD
    - spotless, google java formater maybe
