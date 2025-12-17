# Traceability Matrix Review - Food Order System

**Purpose:** This document provides a traceability analysis framework to verify that requirements, design elements, and test cases are properly linked across the SRS, SDD, and STP documents.

---

## 1. Requirements Traceability Matrix (SRS → SDD → STP)

### 1.1 User Management Requirements

| SRS Requirement ID | Requirement Description | SDD Component | STP Test Case ID | Status |
|-------------------|------------------------|---------------|------------------|--------|
| REQ-USER-001 | User registration with Customer/Manager roles | AuthController.register() | TEST-REG-001 | [ ] |
| REQ-USER-002 | User authentication and login | AuthController.login() | TEST-AUTH-001 | [ ] |
| REQ-USER-003 | User profile management (phone, address) | UserService, User entity | TEST-PROF-001 | [ ] |
| REQ-USER-004 | Session management | Spring Session | TEST-SESS-001 | [ ] |

**Review Notes:**
- Verify that AuthController methods align with registration requirements
- Check if phone number and address management is fully covered
- Ensure session timeout and security are addressed

---

### 1.2 Restaurant Management Requirements

| SRS Requirement ID | Requirement Description | SDD Component | STP Test Case ID | Status |
|-------------------|------------------------|---------------|------------------|--------|
| REQ-REST-001 | Restaurant listing and display | SearchService, Restaurant entity | TEST-REST-001 | [ ] |
| REQ-REST-002 | Restaurant search functionality | SearchController, SearchService | TEST-SEARCH-001 | [ ] |
| REQ-REST-003 | Restaurant keyword association | AssociatedWith table, SearchService | TEST-KEY-001 | [ ] |
| REQ-REST-004 | Manager-restaurant relationship | Manages table, UserService | TEST-MGR-001 | [ ] |

**Review Notes:**
- Verify search algorithm is documented in SDD
- Check keyword matching logic coverage
- Ensure manager permissions are properly tested

---

### 1.3 Menu and Ordering Requirements

| SRS Requirement ID | Requirement Description | SDD Component | STP Test Case ID | Status |
|-------------------|------------------------|---------------|------------------|--------|
| REQ-MENU-001 | Menu item display | MenuItem entity, Has table | TEST-MENU-001 | [ ] |
| REQ-MENU-002 | Shopping cart functionality | CartController, Cart entity | TEST-CART-001 | [ ] |
| REQ-MENU-003 | Cart status management | Cart.status ENUM | TEST-STATUS-001 | [ ] |
| REQ-MENU-004 | Discount application | Applied table, Discount logic | TEST-DISC-001 | [ ] |
| REQ-MENU-005 | Order placement | CartController, Belongs table | TEST-ORDER-001 | [ ] |

**Review Notes:**
- Verify cart state transitions are documented (preparing → sent → accepted)
- Check discount calculation logic
- Ensure quantity validation (CHECK constraint: Quantity > 0) is tested

---

### 1.4 Rating System Requirements

| SRS Requirement ID | Requirement Description | SDD Component | STP Test Case ID | Status |
|-------------------|------------------------|---------------|------------------|--------|
| REQ-RATE-001 | Rating submission (1-5 scale) | Ratings table, Rating CHECK | TEST-RATE-001 | [ ] |
| REQ-RATE-002 | Comment submission | Ratings.comment TEXT | TEST-COMM-001 | [ ] |
| REQ-RATE-003 | Rating-restaurant association | ForRestaurant table | TEST-REST-RATE-001 | [ ] |
| REQ-RATE-004 | Rating-cart association | Ratings.CartID UNIQUE | TEST-CART-RATE-001 | [ ] |

**Review Notes:**
- Verify one-to-one cart-rating relationship is properly documented
- Check rating validation (1-5 range) is enforced
- Ensure optional comment field is handled correctly

---

## 2. Design-to-Implementation Traceability (SDD → Code)

### 2.1 Controller Layer

| SDD Component | Implementation Class | Methods | Status |
|---------------|---------------------|---------|--------|
| Authentication Controller | AuthController.java | register(), login(), logout() | [ ] |
| Cart Controller | CartController.java | addToCart(), viewCart(), checkout() | [ ] |
| Search Controller | SearchController.java | search() | [ ] |
| Home Controller | HomeController.java | home() | [ ] |

**Review Notes:**
- Verify all controller methods match SDD specifications
- Check request/response mappings
- Ensure error handling is implemented

---

### 2.2 Service Layer

| SDD Component | Implementation Class | Methods | Status |
|---------------|---------------------|---------|--------|
| User Service | UserService.java | registerUser(), authenticateUser() | [ ] |
| Search Service | SearchService.java | searchRestaurants() | [ ] |

**Review Notes:**
- Verify business logic matches SDD design
- Check service layer error handling
- Ensure transaction boundaries are defined

---

### 2.3 Data Access Layer

| SDD Component | Implementation Class | Methods | Status |
|---------------|---------------------|---------|--------|
| Database Repository | DbTestRepo.java | Various query methods | [ ] |

**Review Notes:**
- Verify SQL queries match database schema
- Check prepared statement usage for security
- Ensure connection management is proper

---

### 2.4 Data Transfer Objects

| SDD Component | Implementation Class | Fields | Status |
|---------------|---------------------|--------|--------|
| Cart Item DTO | CartItem.java | menuItemId, quantity, etc. | [ ] |
| User Login Result DTO | UserLoginResult.java | username, userType, etc. | [ ] |

**Review Notes:**
- Verify DTO structure matches SDD design
- Check validation annotations (if any)
- Ensure proper serialization

---

## 3. Database Schema Traceability

### 3.1 Entity Relationships

| Entity | Related Entities | Relationship Type | Foreign Keys | Status |
|--------|-----------------|-------------------|--------------|--------|
| User | UserPhoneNumber | Many-to-Many | HasPhoneNum | [ ] |
| User | UserAddress | Many-to-Many | Lives | [ ] |
| User | Restaurant | Many-to-Many | Manages | [ ] |
| User | Cart | One-to-Many | Belongs | [ ] |
| User | Rating | One-to-Many | WrittenBy | [ ] |
| Restaurant | MenuItem | Many-to-Many | Has | [ ] |
| Restaurant | Rating | Many-to-Many | ForRestaurant | [ ] |
| Restaurant | Cart | Many-to-Many | Holds | [ ] |
| Cart | MenuItem | Many-to-Many | Contains | [ ] |
| Cart | Rating | One-to-One | Ratings.CartID | [ ] |
| MenuItem | Discount | Many-to-Many | Applied | [ ] |

**Review Notes:**
- Verify all relationships are documented in SDD
- Check cascade delete behaviors (CASCADE vs RESTRICT)
- Ensure referential integrity is maintained

---

### 3.2 Constraints and Validations

| Constraint | Table/Column | Type | Status |
|------------|--------------|------|--------|
| Rating Range | Ratings.Rating | CHECK (1-5) | [ ] |
| Quantity Positive | Contains.Quantity | CHECK (> 0) | [ ] |
| Username Unique | User.Username | UNIQUE | [ ] |
| Phone Unique | UserPhoneNumber.PhoneNumber | UNIQUE | [ ] |
| Cart-Rating Unique | Ratings.CartID | UNIQUE | [ ] |
| UserType Enum | User.UserType | ENUM('Customer','Manager') | [ ] |
| Cart Status Enum | Cart.Status | ENUM('preparing','sent','accepted') | [ ] |

**Review Notes:**
- Verify all constraints are documented in SDD
- Check if application layer enforces these constraints
- Ensure test cases cover constraint violations

---

## 4. Test Coverage Analysis

### 4.1 Requirement Coverage

| Requirement Category | Total Requirements | Test Cases | Coverage % | Status |
|---------------------|-------------------|------------|------------|--------|
| User Management | 4 | [Count] | [%] | [ ] |
| Restaurant Management | 4 | [Count] | [%] | [ ] |
| Menu and Ordering | 5 | [Count] | [%] | [ ] |
| Rating System | 4 | [Count] | [%] | [ ] |
| **Total** | **17** | **[Total]** | **[%]** | **[ ]** |

**Review Notes:**
- Calculate actual coverage percentages from STP
- Identify gaps in test coverage
- Prioritize missing test cases

---

### 4.2 Component Coverage

| Component | Unit Tests | Integration Tests | System Tests | Status |
|-----------|------------|------------------|--------------|--------|
| AuthController | [ ] | [ ] | [ ] | [ ] |
| CartController | [ ] | [ ] | [ ] | [ ] |
| SearchController | [ ] | [ ] | [ ] | [ ] |
| UserService | [ ] | [ ] | [ ] | [ ] |
| SearchService | [ ] | [ ] | [ ] | [ ] |
| DbTestRepo | [ ] | [ ] | [ ] | [ ] |

**Review Notes:**
- Verify each component has appropriate test levels
- Check test data setup (FOS-DML alignment)
- Ensure edge cases are covered

---

## 5. Gap Analysis

### 5.1 Missing Traceability Links

**Identified Gaps:**
1. [ ] Requirement without corresponding design element
2. [ ] Design element without test case
3. [ ] Test case without requirement reference
4. [ ] Database constraint without application validation

**Action Items:**
- Document each gap
- Assign priority (High/Medium/Low)
- Create follow-up tasks

---

### 5.2 Incomplete Traceability

**Areas Needing Clarification:**
1. [ ] Ambiguous requirement IDs
2. [ ] Unclear component relationships
3. [ ] Missing test case details
4. [ ] Incomplete design specifications

---

## 6. Traceability Verification Checklist

### 6.1 Forward Traceability (SRS → SDD → Code)
- [ ] All SRS requirements have corresponding SDD components
- [ ] All SDD components have code implementations
- [ ] Requirements are uniquely identified
- [ ] Component relationships are clear

### 6.2 Backward Traceability (Code → SDD → SRS)
- [ ] All code components trace back to SDD
- [ ] All SDD components trace back to SRS
- [ ] No orphaned code or design elements
- [ ] All features are requirement-driven

### 6.3 Test Traceability (STP → SRS/SDD)
- [ ] All test cases reference requirements
- [ ] All requirements have test cases
- [ ] Test cases cover design components
- [ ] Test data aligns with requirements

---

## 7. Recommendations

### 7.1 Immediate Actions
1. **Complete Traceability Matrix**: Fill in all status columns
2. **Identify Gaps**: Document missing links
3. **Update Documentation**: Add traceability references (without modifying original DOCX files)

### 7.2 Process Improvements
1. **Requirement ID Standard**: Ensure consistent ID format (REQ-XXX-###)
2. **Test Case ID Standard**: Ensure consistent ID format (TEST-XXX-###)
3. **Component Naming**: Align code component names with SDD

### 7.3 Tool Recommendations
1. **Traceability Tool**: Consider using tools for automated traceability
2. **Documentation Links**: Add hyperlinks between related sections
3. **Version Control**: Track traceability changes

---

## 8. Review Summary

**Traceability Status:**
- Requirements Coverage: [To be calculated]
- Design Coverage: [To be calculated]
- Test Coverage: [To be calculated]
- Overall Traceability: [To be assessed]

**Next Steps:**
1. Complete traceability matrix with actual document references
2. Identify and document gaps
3. Create action plan for improvements
4. Schedule follow-up review

---

**End of Traceability Review**
