# Automated Exception Dashboard for Keyword-Driven Frameworks 📊

## Project Overview

The **Automated Exception Dashboard** is a Java-based tool designed to streamline debugging in test automation by converting large raw log files into a clean, interactive HTML dashboard. It parses log files from keyword-driven frameworks to provide a high-level summary and detailed drill-down of failed tests, enabling faster and more efficient troubleshooting.

---

## Key Features

- **Automated Log Parsing:** Processes the entire log file to extract individual test case results automatically.
- **Failure Detection:** Identifies failures using keywords such as "Exception," "Error," or "failed."
- **Interactive Dashboard:** Generates a static, shareable HTML dashboard viewable in any modern web browser.
- **Summary Statistics:** Displays total, passed, and failed test counts clearly.
- **Drill-Down Functionality:** Clicking on a failed test name opens a modal displaying full log details for focused debugging.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 11 or higher:** Make sure Java is installed and properly configured on your system.
- **Maven:** Used for project build management and dependencies.

### Project Structure

The project uses a standard Maven layout:

selenium-keyword-dashboard/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── dashboard/
        │               ├── SimpleDashboardGenerator.java
        │               └── (other classes, if any)
        └── resources/
            └── dashboardTemplate.html (or similar static files)



---

### How to Run
1. **Place Your Log File:**  
   Put your log file (named `log.txt` by default) at the path specified in `SimpleDashboardGenerator.java`:  
   `F:\QA_AUTOMATIONS_PROJECTS\ExceptionDashBoard\selenium-keyword-dashboard\log.txt`

2. **Build the Project:**  
   Open a terminal/command prompt in the project's root folder (`selenium-keyword-dashboard`) and run:
   
'''mvn clean install'''


3. **Generate the Dashboard:**  
After building, generate the dashboard by running:  

'''mvn exec:java -Dexec.mainClass=com.example.dashboard.SimpleDashboardGenerator'''


4. **View the Dashboard:**  
The tool creates `dashboard_demo.html` in the project root. Open it in any modern browser to see your interactive test report.

---

## Technical Details

- **Language:** Java  
- **Parsing:** Scans log files for test results using delimiters and keywords to detect errors.
- **HTML Generation:** Uses a template-based approach, injecting parsed data into static HTML enhanced with [Tailwind CSS](https://tailwindcss.com) and [Chart.js](https://www.chartjs.org) for styling and visuals.
- **User Interaction:** Modal windows for detailed log views; charts summarize pass/fail rates.

---

## Customization

To adjust for your environment or preferences, update these variables in `SimpleDashboardGenerator.java`:

- `LOG_FILE`: Path to your log file.  
- `DASHBOARD_FILE`: Name and location of the dashboard HTML output file.

---

## License

*(Add license details here if applicable.)*

---

## Contact

For questions, suggestions, or contributions, please open issues or pull requests in the project repository.

---


