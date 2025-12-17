# Consistency and Terminology Review - Food Order System

**Purpose:** This document identifies terminology inconsistencies and provides recommendations for maintaining consistent language across all project documents.

---

## 1. Terminology Dictionary

### 1.1 Core Domain Terms

| Term | Definition | Usage Context | Documents to Verify |
|------|------------|--------------|-------------------|
| **Cart** | A collection of menu items selected by a user for ordering | Ordering process | SRS, SDD, STP |
| **Order** | A completed cart that has been submitted | Order management | SRS, SDD, STP |
| **User** | Generic term for any system user | User management | All documents |
| **Customer** | User type with ordering capabilities | User roles | All documents |
| **Manager** | User type with restaurant management capabilities | User roles | All documents |
| **Restaurant** | Food service establishment | Restaurant management | All documents |
| **MenuItem** | Individual food item available for order | Menu management | All documents |
| **Rating** | User feedback on order/restaurant (1-5 scale) | Rating system | All documents |
| **Review** | Text comment accompanying a rating | Rating system | SRS, SDD, STP |
| **Discount** | Price reduction applied to menu items | Pricing | SRS, SDD, STP |

**Review Checklist:**
- [ ] Verify consistent use of "Cart" vs "Order" across documents
- [ ] Check if "User" is used generically or specifically
- [ ] Verify "Rating" vs "Review" terminology
- [ ] Ensure "MenuItem" is consistently used (not "Product" or "Food Item")

---

## 2. Terminology Inconsistencies to Check

### 2.1 User-Related Terms

**Potential Inconsistencies:**
- [ ] "User" vs "Customer" vs "Client"
- [ ] "User Account" vs "Account" vs "Profile"
- [ ] "User Registration" vs "Account Creation" vs "Sign Up"
- [ ] "User Authentication" vs "Login" vs "Sign In"
- [ ] "User Type" vs "User Role" vs "User Category"

**Recommendation:**
- Use "User" as the generic term
- Use "Customer" and "Manager" for specific user types
- Use "User Registration" and "User Authentication" in formal documents
- Use "Login" and "Logout" in user-facing contexts

---

### 2.2 Order-Related Terms

**Potential Inconsistencies:**
- [ ] "Cart" vs "Shopping Cart" vs "Order Cart"
- [ ] "Order" vs "Purchase" vs "Transaction"
- [ ] "Order Status" vs "Cart Status" vs "Order State"
- [ ] "Order Placement" vs "Checkout" vs "Order Submission"
- [ ] "Order History" vs "Order List" vs "Past Orders"

**Recommendation:**
- Use "Cart" for items being selected
- Use "Order" for completed/submitted carts
- Use "Cart Status" for cart state (preparing, sent, accepted)
- Use "Order Placement" in formal documents, "Checkout" in user-facing contexts

---

### 2.3 Restaurant-Related Terms

**Potential Inconsistencies:**
- [ ] "Restaurant" vs "Vendor" vs "Establishment"
- [ ] "Restaurant Management" vs "Vendor Management"
- [ ] "Restaurant Owner" vs "Manager" vs "Vendor"
- [ ] "Cuisine Type" vs "Food Category" vs "Restaurant Category"

**Recommendation:**
- Consistently use "Restaurant"
- Use "Manager" for restaurant management role
- Use "Cuisine Type" as defined in database schema

---

### 2.4 Menu-Related Terms

**Potential Inconsistencies:**
- [ ] "MenuItem" vs "Product" vs "Food Item" vs "Dish"
- [ ] "Menu" vs "Menu Catalog" vs "Product Catalog"
- [ ] "Menu Display" vs "Product Listing"
- [ ] "Menu Item Price" vs "Product Price" vs "Item Cost"

**Recommendation:**
- Consistently use "MenuItem" (matches database schema)
- Use "Menu" for the collection of menu items
- Use "Price" for menu item pricing

---

### 2.5 Rating-Related Terms

**Potential Inconsistencies:**
- [ ] "Rating" vs "Review" vs "Feedback" vs "Evaluation"
- [ ] "Rating Score" vs "Rating Value" vs "Rating"
- [ ] "Rating Comment" vs "Review Comment" vs "Feedback Text"
- [ ] "Rating Submission" vs "Review Submission"

**Recommendation:**
- Use "Rating" for the numerical score (1-5)
- Use "Comment" for the text feedback
- Use "Review" when referring to the combination of rating + comment
- Use "Rating Submission" for the action

---

## 3. Technical Terminology Consistency

### 3.1 Architecture Terms

| Term | Standard Usage | Documents to Verify |
|------|----------------|-------------------|
| **Controller** | Spring MVC Controller | SDD, Code |
| **Service** | Business logic layer | SDD, Code |
| **Repository** | Data access layer | SDD, Code |
| **DTO** | Data Transfer Object | SDD, Code |
| **Entity** | Database entity/table | SDD, Code |

**Review Checklist:**
- [ ] Verify consistent use of layer terminology
- [ ] Check if "DAO" is used instead of "Repository"
- [ ] Ensure "Model" vs "Entity" usage is consistent

---

### 3.2 Database Terms

| Term | Standard Usage | Documents to Verify |
|------|----------------|-------------------|
| **Table** | Database table | SDD, SRS |
| **Column** | Table column/field | SDD, SRS |
| **Row** | Table record | SDD, SRS |
| **Foreign Key** | Referential constraint | SDD, SRS |
| **Primary Key** | Unique identifier | SDD, SRS |
| **Constraint** | Data validation rule | SDD, SRS |

**Review Checklist:**
- [ ] Verify consistent use of "Table" vs "Entity"
- [ ] Check "Column" vs "Field" vs "Attribute" usage
- [ ] Ensure "Foreign Key" is consistently referenced

---

### 3.3 Testing Terms

| Term | Standard Usage | Documents to Verify |
|------|----------------|-------------------|
| **Unit Test** | Component-level test | STP |
| **Integration Test** | Component interaction test | STP |
| **System Test** | End-to-end test | STP |
| **Test Case** | Specific test scenario | STP |
| **Test Suite** | Collection of test cases | STP |

**Review Checklist:**
- [ ] Verify consistent test level terminology
- [ ] Check "Test Case" vs "Test Scenario" usage
- [ ] Ensure "Test Plan" vs "Test Strategy" distinction

---

## 4. Status and State Terminology

### 4.1 Cart Status Values

**Database Definition:** `ENUM('preparing', 'sent', 'accepted')`

**Consistency Check:**
- [ ] Verify all documents use these exact values
- [ ] Check if "pending" or "processing" are used instead
- [ ] Ensure status transitions are clearly documented

**Recommendation:**
- Use exact enum values: "preparing", "sent", "accepted"
- Document status transition rules
- Avoid synonyms or alternative terms

---

### 4.2 User Type Values

**Database Definition:** `ENUM('Customer', 'Manager')`

**Consistency Check:**
- [ ] Verify capitalization matches exactly
- [ ] Check if "Admin" or "Administrator" is used
- [ ] Ensure role-based access is clearly defined

**Recommendation:**
- Use exact enum values: "Customer", "Manager"
- Capitalize first letter consistently
- Document role permissions clearly

---

## 5. Abbreviation and Acronym Consistency

### 5.1 System Abbreviations

| Abbreviation | Full Form | Usage Context |
|-------------|-----------|---------------|
| **FOS** | Food Order System | Project name |
| **SDP** | Software Development Plan | Document reference |
| **SRS** | Software Requirements Specification | Document reference |
| **STP** | Software Test Plan | Document reference |
| **SDD** | Software Detailed Design | Document reference |
| **MVC** | Model-View-Controller | Architecture pattern |
| **DTO** | Data Transfer Object | Design pattern |
| **JDBC** | Java Database Connectivity | Technology |
| **SQL** | Structured Query Language | Technology |

**Review Checklist:**
- [ ] Verify abbreviations are defined on first use
- [ ] Check consistent capitalization
- [ ] Ensure acronyms match industry standards

---

## 6. Date and Time Terminology

**Potential Inconsistencies:**
- [ ] "Date" vs "DateTime" vs "Timestamp"
- [ ] "Created Date" vs "Creation Date" vs "Date Created"
- [ ] "Start Date" vs "StartDateTime" vs "Start Time"
- [ ] "End Date" vs "EndDateTime" vs "End Time"

**Database Usage:**
- `AcceptedAt DATETIME` in Cart table
- `RatingDate DATETIME` in Ratings table
- `StartDate DATETIME, EndDate DATETIME` in Applied table

**Recommendation:**
- Use "Date" for date-only values
- Use "DateTime" or "Timestamp" for date+time values
- Use consistent naming: "CreatedAt", "UpdatedAt", "StartDate", "EndDate"

---

## 7. Action and Operation Terminology

### 7.1 User Actions

| Action | Standard Term | Alternative Terms to Avoid |
|--------|---------------|---------------------------|
| Create account | "Register" or "User Registration" | "Sign Up", "Create Account" |
| Access system | "Login" or "Authenticate" | "Sign In", "Log In" |
| Exit system | "Logout" | "Sign Out", "Log Out" |
| Add to cart | "Add to Cart" | "Add Item", "Add Product" |
| Submit order | "Place Order" or "Checkout" | "Submit", "Complete Order" |

**Review Checklist:**
- [ ] Verify consistent action terminology
- [ ] Check if formal vs informal terms are mixed
- [ ] Ensure user-facing vs technical terms are appropriate

---

## 8. Error and Exception Terminology

**Potential Inconsistencies:**
- [ ] "Error" vs "Exception" vs "Failure"
- [ ] "Error Message" vs "Error Notification" vs "Alert"
- [ ] "Validation Error" vs "Input Error" vs "Format Error"
- [ ] "System Error" vs "Application Error" vs "Server Error"

**Recommendation:**
- Use "Error" for user-facing messages
- Use "Exception" for technical/developer contexts
- Use "Validation Error" for input validation issues
- Use "System Error" for infrastructure problems

---

## 9. Consistency Review Checklist

### 9.1 Document-Level Consistency

- [ ] **SRS**: All terms defined in glossary section
- [ ] **SDD**: Technical terms match SRS definitions
- [ ] **STP**: Test terminology is consistent
- [ ] **SDP**: Project terminology aligns with other documents

### 9.2 Cross-Document Consistency

- [ ] Same terms used for same concepts across all documents
- [ ] Abbreviations defined consistently
- [ ] Status values match database definitions
- [ ] Action terms are consistent

### 9.3 Code-Document Consistency

- [ ] Class names match SDD component names
- [ ] Method names match operation descriptions
- [ ] Database table/column names match documentation
- [ ] Enum values match documented values

---

## 10. Recommendations

### 10.1 Immediate Actions

1. **Create Terminology Glossary**: Add to project documentation
2. **Standardize Abbreviations**: Define all acronyms
3. **Review Status Values**: Ensure enum values are consistent
4. **Document Naming Conventions**: Establish clear standards

### 10.2 Process Improvements

1. **Terminology Review Process**: Include in document review checklist
2. **Automated Checks**: Consider tools for terminology validation
3. **Team Training**: Ensure all team members use consistent terms

### 10.3 Documentation Updates

1. **Add Glossary Section**: To each document (if not present)
2. **Cross-Reference Terms**: Link related terms
3. **Version Control**: Track terminology changes

---

## 11. Terminology Issues Log

**Format:** Document | Section | Term | Issue | Recommendation

| Document | Section | Term | Issue | Recommendation |
|----------|--------|------|-------|---------------|
| [To be filled] | [To be filled] | [To be filled] | [To be filled] | [To be filled] |

---

## 12. Review Summary

**Consistency Status:**
- Terminology Definitions: [To be assessed]
- Cross-Document Consistency: [To be assessed]
- Code-Document Alignment: [To be assessed]

**Next Steps:**
1. Complete terminology review of all documents
2. Document identified inconsistencies
3. Create terminology glossary
4. Update supporting documentation with standard terms

---

**End of Consistency and Terminology Review**
