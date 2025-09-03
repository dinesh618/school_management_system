# School Management System - Entity Relationship Diagram

This document provides a comprehensive overview of the database schema and entity relationships for the School Management System.

## Database Schema Overview

The School Management System database consists of 8 main entities organized in a relational structure that supports comprehensive academic management functionality.

## Entities and Relationships

### 1. User (Base Entity)
**Table**: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| first_name | VARCHAR(50) | NOT NULL | User's first name |
| last_name | VARCHAR(50) | NOT NULL | User's last name |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User's email address |
| password | VARCHAR(255) | NOT NULL | Encrypted password |
| role | ENUM | NOT NULL | User role (STUDENT, TEACHER, ADMIN) |
| is_active | BOOLEAN | DEFAULT TRUE | Account status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Inheritance Strategy**: Single Table Inheritance with discriminator column `dtype`

### 2. Student (User Subclass)
**Table**: `users` (with dtype = 'Student')

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| student_id | VARCHAR(20) | UNIQUE, NOT NULL | Student identification number |
| year_level | INT | NULL | Current year level (1-4) |
| major | VARCHAR(100) | NULL | Student's major field of study |
| gpa | DECIMAL(3,2) | NULL | Grade Point Average |

**Relationships**:
- One-to-Many with `Enrollment` (student can enroll in multiple courses)
- One-to-Many with `Submission` (student can submit multiple assignments)
- One-to-Many with `Attendance` (student has multiple attendance records)

### 3. Teacher (User Subclass)
**Table**: `users` (with dtype = 'Teacher')

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| employee_id | VARCHAR(20) | UNIQUE, NOT NULL | Employee identification number |
| department | VARCHAR(100) | NULL | Teacher's department |
| specialization | VARCHAR(100) | NULL | Area of specialization |

**Relationships**:
- One-to-Many with `Course` (teacher can teach multiple courses)

### 4. Course
**Table**: `courses`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique course identifier |
| course_code | VARCHAR(10) | UNIQUE, NOT NULL | Course code (e.g., "CS101") |
| course_name | VARCHAR(200) | NOT NULL | Course name |
| description | TEXT | NULL | Course description |
| credits | INT | NOT NULL | Credit hours |
| semester | VARCHAR(20) | NOT NULL | Semester (Fall, Spring, Summer) |
| academic_year | VARCHAR(9) | NOT NULL | Academic year (e.g., "2023-2024") |
| schedule | VARCHAR(100) | NULL | Class schedule |
| room | VARCHAR(50) | NULL | Classroom location |
| max_students | INT | DEFAULT 30 | Maximum enrollment capacity |
| teacher_id | BIGINT | FOREIGN KEY | Reference to teaching teacher |
| is_active | BOOLEAN | DEFAULT TRUE | Course status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Relationships**:
- Many-to-One with `Teacher` (course belongs to one teacher)
- One-to-Many with `Assignment` (course can have multiple assignments)
- One-to-Many with `Enrollment` (course can have multiple student enrollments)
- One-to-Many with `Attendance` (course has multiple attendance records)

### 5. Assignment
**Table**: `assignments`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique assignment identifier |
| title | VARCHAR(200) | NOT NULL | Assignment title |
| description | TEXT | NULL | Assignment description |
| due_date | TIMESTAMP | NOT NULL | Due date and time |
| max_points | INT | NOT NULL | Maximum points possible |
| type | ENUM | NOT NULL | Assignment type (HOMEWORK, QUIZ, EXAM, PROJECT) |
| course_id | BIGINT | FOREIGN KEY | Reference to course |
| is_active | BOOLEAN | DEFAULT TRUE | Assignment status |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Relationships**:
- Many-to-One with `Course` (assignment belongs to one course)
- One-to-Many with `Submission` (assignment can have multiple submissions)

### 6. Enrollment
**Table**: `enrollments`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique enrollment identifier |
| student_id | BIGINT | FOREIGN KEY | Reference to student |
| course_id | BIGINT | FOREIGN KEY | Reference to course |
| enrollment_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Enrollment date |
| grade | VARCHAR(2) | NULL | Final course grade (A, B, C, D, F) |
| status | ENUM | NOT NULL | Enrollment status (ENROLLED, COMPLETED, DROPPED, WITHDRAWN) |

**Relationships**:
- Many-to-One with `Student` (enrollment belongs to one student)
- Many-to-One with `Course` (enrollment belongs to one course)

**Constraints**:
- Unique constraint on (student_id, course_id) to prevent duplicate enrollments

### 7. Submission
**Table**: `submissions`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique submission identifier |
| student_id | BIGINT | FOREIGN KEY | Reference to student |
| assignment_id | BIGINT | FOREIGN KEY | Reference to assignment |
| content | TEXT | NOT NULL | Submission content |
| submitted_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Submission timestamp |
| grade | VARCHAR(10) | NULL | Assigned grade |
| feedback | TEXT | NULL | Teacher's feedback |
| graded_at | TIMESTAMP | NULL | Grading timestamp |
| graded_by | VARCHAR(100) | NULL | Grader identifier |
| is_late | BOOLEAN | DEFAULT FALSE | Late submission flag |

**Relationships**:
- Many-to-One with `Student` (submission belongs to one student)
- Many-to-One with `Assignment` (submission belongs to one assignment)

**Constraints**:
- Unique constraint on (student_id, assignment_id) to prevent duplicate submissions

### 8. Attendance
**Table**: `attendance`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique attendance identifier |
| student_id | BIGINT | FOREIGN KEY | Reference to student |
| course_id | BIGINT | FOREIGN KEY | Reference to course |
| date | DATE | NOT NULL | Attendance date |
| status | ENUM | NOT NULL | Attendance status (PRESENT, ABSENT, LATE, EXCUSED) |
| remarks | VARCHAR(255) | NULL | Additional notes |
| marked_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | When attendance was marked |
| marked_by | VARCHAR(100) | NULL | Who marked the attendance |

**Relationships**:
- Many-to-One with `Student` (attendance record belongs to one student)
- Many-to-One with `Course` (attendance record belongs to one course)

**Constraints**:
- Unique constraint on (student_id, course_id, date) to prevent duplicate attendance records

## Entity Relationship Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      User       │    │    Student      │    │    Teacher      │
│                 │    │   (extends)     │    │   (extends)     │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ + id (PK)       │    │ + student_id    │    │ + employee_id   │
│ + first_name    │    │ + year_level    │    │ + department    │
│ + last_name     │    │ + major         │    │ + specialization│
│ + email         │    │ + gpa           │    │                 │
│ + password      │    │                 │    │                 │
│ + role          │    │                 │    │                 │
│ + is_active     │    │                 │    │                 │
│ + created_at    │    │                 │    │                 │
│ + updated_at    │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         │                       │                       │ 1
         │                       │                       │
         │                       │              ┌─────────────────┐
         │                       │              │     Course      │
         │                       │              ├─────────────────┤
         │                       │          ┌───│ + id (PK)       │
         │                       │          │   │ + course_code   │
         │                       │          │   │ + course_name   │
         │                       │          │   │ + description   │
         │                       │          │   │ + credits       │
         │                       │          │   │ + semester      │
         │                       │          │   │ + academic_year │
         │                       │          │   │ + schedule      │
         │                       │          │   │ + room          │
         │                       │          │   │ + max_students  │
         │                       │          │   │ + teacher_id(FK)│
         │                       │          │   │ + is_active     │
         │                       │          │   │ + created_at    │
         │                       │          │   │ + updated_at    │
         │                       │          │   └─────────────────┘
         │                       │          │            │ 1
         │                       │          │            │
         │                       │          │            │ *
         │                       │          │   ┌─────────────────┐
         │                       │          │   │   Assignment    │
         │                       │          │   ├─────────────────┤
         │                       │          └───│ + id (PK)       │
         │                       │              │ + title         │
         │                       │              │ + description   │
         │                       │              │ + due_date      │
         │                       │              │ + max_points    │
         │                       │              │ + type          │
         │                       │              │ + course_id(FK) │
         │                       │              │ + is_active     │
         │                       │              │ + created_at    │
         │                       │              │ + updated_at    │
         │                       │              └─────────────────┘
         │                       │                       │ 1
         │                       │                       │
         │                       │                       │ *
         │                       │              ┌─────────────────┐
         │                       │              │   Submission    │
         │                       │              ├─────────────────┤
         │                       └──────────────│ + id (PK)       │
         │                        *             │ + student_id(FK)│
         │                                      │ + assignment_id │
         │                                      │   (FK)          │
         │                                      │ + content       │
         │                                      │ + submitted_at  │
         │                                      │ + grade         │
         │                                      │ + feedback      │
         │                                      │ + graded_at     │
         │                                      │ + graded_by     │
         │                                      │ + is_late       │
         │                                      └─────────────────┘
         │
         │ *
┌─────────────────┐    *                  1    ┌─────────────────┐
│   Enrollment    │ ───────────────────────────│     Course      │
├─────────────────┤                            │   (reference)   │
│ + id (PK)       │                            │                 │
│ + student_id(FK)│                            │                 │
│ + course_id(FK) │                            │                 │
│ + enrollment_dt │                            │                 │
│ + grade         │                            │                 │
│ + status        │                            │                 │
└─────────────────┘                            └─────────────────┘
         │ *
         │
         │ 1
┌─────────────────┐    *                  1    ┌─────────────────┐
│   Attendance    │ ───────────────────────────│     Course      │
├─────────────────┤                            │   (reference)   │
│ + id (PK)       │                            │                 │
│ + student_id(FK)│                            │                 │
│ + course_id(FK) │                            │                 │
│ + date          │                            │                 │
│ + status        │                            │                 │
│ + remarks       │                            │                 │
│ + marked_at     │                            │                 │
│ + marked_by     │                            │                 │
└─────────────────┘                            └─────────────────┘
```

## Relationship Details

### Primary Relationships

1. **Teacher → Course** (1:M)
   - One teacher can teach multiple courses
   - Each course is taught by exactly one teacher
   - Foreign Key: `courses.teacher_id` → `users.id`

2. **Course → Assignment** (1:M)
   - One course can have multiple assignments
   - Each assignment belongs to exactly one course
   - Foreign Key: `assignments.course_id` → `courses.id`

3. **Student → Enrollment** (1:M)
   - One student can enroll in multiple courses
   - Each enrollment record belongs to exactly one student
   - Foreign Key: `enrollments.student_id` → `users.id`

4. **Course → Enrollment** (1:M)
   - One course can have multiple student enrollments
   - Each enrollment record belongs to exactly one course
   - Foreign Key: `enrollments.course_id` → `courses.id`

5. **Student → Submission** (1:M)
   - One student can submit multiple assignments
   - Each submission belongs to exactly one student
   - Foreign Key: `submissions.student_id` → `users.id`

6. **Assignment → Submission** (1:M)
   - One assignment can have multiple submissions
   - Each submission belongs to exactly one assignment
   - Foreign Key: `submissions.assignment_id` → `assignments.id`

7. **Student → Attendance** (1:M)
   - One student can have multiple attendance records
   - Each attendance record belongs to exactly one student
   - Foreign Key: `attendance.student_id` → `users.id`

8. **Course → Attendance** (1:M)
   - One course can have multiple attendance records
   - Each attendance record belongs to exactly one course
   - Foreign Key: `attendance.course_id` → `courses.id`

### Business Rules and Constraints

1. **Unique Enrollments**: A student cannot enroll in the same course multiple times
   - Enforced by unique constraint on (student_id, course_id)

2. **Unique Submissions**: A student cannot submit multiple submissions for the same assignment
   - Enforced by unique constraint on (student_id, assignment_id)

3. **Daily Attendance**: Only one attendance record per student per course per day
   - Enforced by unique constraint on (student_id, course_id, date)

4. **User Role Inheritance**: Students and Teachers inherit from User base class
   - Implemented using Single Table Inheritance with discriminator

5. **Course Capacity**: Enrollment is limited by course max_students capacity
   - Enforced at application level

6. **Grade Validation**: Grades must follow institution standards
   - Enforced at application level with validation

7. **Assignment Due Dates**: Submissions after due date are marked as late
   - Calculated and enforced at application level

## Database Indexes

### Primary Indexes
- All primary keys are automatically indexed
- Foreign key columns should be indexed for performance

### Recommended Additional Indexes

```sql
-- User table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Course table indexes
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_semester_year ON courses(semester, academic_year);
CREATE INDEX idx_courses_is_active ON courses(is_active);

-- Assignment table indexes
CREATE INDEX idx_assignments_course_id ON assignments(course_id);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);
CREATE INDEX idx_assignments_is_active ON assignments(is_active);

-- Enrollment table indexes
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);

-- Submission table indexes
CREATE INDEX idx_submissions_student_id ON submissions(student_id);
CREATE INDEX idx_submissions_assignment_id ON submissions(assignment_id);
CREATE INDEX idx_submissions_submitted_at ON submissions(submitted_at);

-- Attendance table indexes
CREATE INDEX idx_attendance_student_id ON attendance(student_id);
CREATE INDEX idx_attendance_course_id ON attendance(course_id);
CREATE INDEX idx_attendance_date ON attendance(date);
```

## Database Schema DDL

### Create Database
```sql
CREATE DATABASE school_management_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### Table Creation Order
Due to foreign key dependencies, tables should be created in this order:
1. `users` (base table for User, Student, Teacher)
2. `courses` (references users.id for teacher_id)
3. `assignments` (references courses.id)
4. `enrollments` (references users.id and courses.id)
5. `submissions` (references users.id and assignments.id)
6. `attendance` (references users.id and courses.id)

This ER diagram represents a comprehensive academic management system that supports the full lifecycle of educational activities from user management to course delivery and assessment.