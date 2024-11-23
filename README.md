# AutoScore

### Automatic grading tool for subjects using structured API at FPT University

---

## Project Information

- **Project Name**: AutoScore  
- **Topic**: Automatic grading tool for subjects using structured API at FPT University.
- **Team Name**: CodeEval Crew

---

## Team Members

- **SE160607** - Võ Thanh Tuyền  
- **SE160611** - Thiều Phan Văn Minh  
- **SE160599** - Võ Trọng Vương  
- **SE160585** - Hà Nhật Trường

---

## Supervisor

- **Nguyễn Văn Chiến**

---

## Overview

AutoScore is designed to streamline the grading process for programming assignments in courses that involve API development. This tool integrates a **Java Spring Boot** backend with a **Flutter** frontend to provide a seamless, scalable, and user-friendly grading solution.

---

## Features

- **Java Spring Boot Backend**: Efficiently handles API requests, grading logic, and database interactions.  
- **Flutter Frontend**: Provides a clean, responsive UI for both instructors and students.  
- **Customizable Grading Criteria**: Teachers can define grading rules based on performance and code quality.  
- **API Integration**: Supports RESTful APIs for automated grading and data retrieval.

---

## Installation

### Backend (Java Spring Boot)

1. Clone the backend repository:
    ```bash
    git clone https://github.com/SEP490-AutoScore/autoscore-backend.git
    ```

2. Navigate to the backend directory and build the project:
    ```bash
    cd AutoScore-backend
    ./mvnw clean install
    ```

3. Configure the database and API settings in `application.properties`.

4. Run the Spring Boot server:
    ```bash
    ./mvnw spring-boot:run
    ```

### Frontend (Flutter)

1. Clone the frontend repository:
    ```bash
    git clone https://github.com/SEP490-AutoScore/autoscore-frontend.git
    ```

2. Navigate to the frontend directory:
    ```bash
    cd AutoScore-frontend
    ```

3. Install Flutter dependencies:
    ```bash
    flutter pub get
    ```

4. Run the Flutter app:
    ```bash
    flutter run
    ```

---

## Usage

- **For Instructors**:  
    - Access the web-based interface via the Flutter frontend.  
    - Configure grading rules and review student submissions through the integrated dashboard.
  
- **For Students**:  
    - Submit your project via the mobile or web app and receive feedback and grades immediately after submission.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgements

- Special thanks to **Nguyễn Văn Chiến**, our project supervisor, for his guidance and support.

---

