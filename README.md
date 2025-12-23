# PLEASE SET UP VERIFIED COMMITS


# 🚀 Food Ordering System FOS

A Java Spring Boot-based platform enabling users to browse menus, place orders, and manage restaurant operations efficiently. It bridges the gap between local restaurants and customers through a streamlined digital interface.

---

## 📖 Table of Contents
- [About](#-about)
- [Features](#-features)
- [Installation](#-installation)
- [Usage](#-usage)
- [Configuration](#-configuration)

---

## 📌 About
This project is developed as part of the CS320 curriculum. It uses a Spring Boot backend and a MySQL database to handle real-time food ordering, cart management, and restaurant analytics. The system focuses on robust data integrity and role-based access for both customers and restaurant managers.
---

## ✨ Features
User Management: Secure registration and login for both customers and restaurant managers.

Advanced Browsing: Filter restaurants by cuisine, rating, price range, and delivery time.

Smart Shopping Cart: Automatic recalculation of totals (including taxes) with a strict "one restaurant at a time" constraint.

Order Tracking: Real-time status updates from order placement to manager acceptance.

Manager Dashboard: CRUD operations for menu items, restaurant availability toggling, and sales statistics (revenue and order counts).

Feedback System: Verified rating and review system accessible only after order completion.

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

JaCoCo: Generates code coverage reports.

