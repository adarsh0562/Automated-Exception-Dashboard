package com.example.dashboard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SimpleDashboardGenerator {
    private static final String LOG_FILE = "F:\\QA_AUTOMATIONS_PROJECTS\\ExceptionDashBoard\\selenium-keyword-dashboard\\log.txt";
    private static final String DASHBOARD_FILE = "dashboard_demo.html";

    public static void main(String[] args) {
        DashboardStats stats = parseLogFileForStats(LOG_FILE);
        generateDashboardHtml(stats, DASHBOARD_FILE);
        System.out.println("Dashboard generated: " + DASHBOARD_FILE);
    }

    static class TestResult {
        String keywordName;
        String errorMessage;
        String fullLogSection;

        TestResult(String keywordName, String errorMessage, String fullLogSection) {
            this.keywordName = keywordName;
            this.errorMessage = errorMessage;
            this.fullLogSection = fullLogSection;
        }
    }

    static class DashboardStats {
        int totalKeywords;
        int passedKeywords;
        int failedKeywords;
        List<TestResult> failedResults;

        DashboardStats(int total, int passed, int failed, List<TestResult> failedResults) {
            this.totalKeywords = total;
            this.passedKeywords = passed;
            this.failedKeywords = failed;
            this.failedResults = failedResults;
        }
    }

    private static DashboardStats parseLogFileForStats(String filePath) {
        int totalKeywords = 0;
        int passedKeywords = 0;
        int failedKeywords = 0;
        List<TestResult> failedResults = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            String keywordName = null;
            StringBuilder section = new StringBuilder();
            boolean inSection = false;
            boolean hasError = false;
            String errorMsg = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("****************************") && line.endsWith("****************************")) {
                    if (inSection && keywordName != null) {
                        totalKeywords++;
                        if (hasError) {
                            failedKeywords++;
                            failedResults.add(new TestResult(keywordName, errorMsg != null ? errorMsg : "Unknown Error", section.toString()));
                        } else {
                            passedKeywords++;
                        }
                    }
                    // Start new section
                    keywordName = line.replace("*", "").trim();
                    section = new StringBuilder();
                    inSection = true;
                    hasError = false;
                    errorMsg = null;
                }
                if (inSection) {
                    section.append(line).append("\n");
                    if (!hasError) {
                        if (line.contains("verification failed")) {
                            hasError = true;
                            errorMsg = line.trim();
                        } else if (line.contains("Error Type:")) {
                            hasError = true;
                            errorMsg = line.replace("Error Type:", "").trim();
                        } else if (line.contains("Exception")) {
                            hasError = true;
                            errorMsg = line.trim();
                        }
                    }
                }
            }
            // Last section
            if (inSection && keywordName != null) {
                totalKeywords++;
                if (hasError) {
                    failedKeywords++;
                    failedResults.add(new TestResult(keywordName, errorMsg != null ? errorMsg : "Unknown Error", section.toString()));
                } else {
                    passedKeywords++;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
        }
        return new DashboardStats(totalKeywords, passedKeywords, failedKeywords, failedResults);
    }

    private static void generateDashboardHtml(DashboardStats stats, String outFile) {
        String template = getDashboardTemplate();
        int totalKeywords = stats.totalKeywords;
        int passedKeywords = stats.passedKeywords;
        int failedKeywords = stats.failedKeywords;
        List<TestResult> failedResults = stats.failedResults;

        // Build table rows for failed tests only
        StringBuilder tableRows = new StringBuilder();
        int sn = 1;
        for (TestResult r : failedResults) {
            tableRows.append("<tr>")
                .append("<td class='px-4 py-2'>").append(sn++).append("</td>")
                .append("<td class='px-4 py-2 font-medium text-blue-700'>").append(escape(r.keywordName)).append("</td>")
                .append("<td class='px-4 py-2 text-red-600'>").append(escape(r.errorMessage)).append("</td>")
                .append("<td class='px-4 py-2'>")
                .append("<button class='bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 transition pop' onclick=\"showModal(`")
                .append(escapeForJs(r.keywordName)).append("`, `")
                .append(escapeForJs(r.fullLogSection)).append("`)\">View</button>")
                .append("</td></tr>");
        }

        String html = template
                .replace("{{TOTAL_TESTS}}", String.valueOf(totalKeywords))
                .replace("{{PASSED_TESTS}}", String.valueOf(passedKeywords))
                .replace("{{FAILED_TESTS}}", String.valueOf(failedKeywords))
                .replace("{{CHART_DATA}}", "[" + passedKeywords + "," + failedKeywords + "]")
                .replace("<!-- TABLE_ROWS_PLACEHOLDER -->", tableRows.toString());

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
            writer.write(html);
        } catch (IOException e) {
            System.err.println("Error writing dashboard: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String escapeForJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("$", "\\$");
    }

    private static String getDashboardTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Test Failure Dashboard</title>\n" +
                "    <script src=\"https://cdn.tailwindcss.com\"></script>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
                "    <style>\n" +
                "       .fade-in {animation: fadeIn 1s;}\n" +
                "       @keyframes fadeIn {from {opacity: 0;} to {opacity: 1;}}\n" +
                "       .pop {animation: popAnim 0.4s;}\n" +
                "       @keyframes popAnim {0% {transform: scale(0.8); opacity: 0;} 100% {transform: scale(1); opacity: 1;}}\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body class=\"min-h-screen bg-gradient-to-br from-blue-100 via-purple-100 to-pink-100 fade-in\">\n" +
                "<div class=\"flex justify-end mb-4\">\n" +
                "    <button id=\"themeToggleBtn\" aria-label=\"Toggle dark mode\" \n" +
                "            class=\"bg-dark-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 px-3 py-1 rounded shadow hover:bg-gray-300 dark:hover:bg-gray-600 transition\">\n" +
                "        Light Mode\n" +
                "    </button>\n" +
                "</div>\n"+

                "    <div class=\"max-w-6xl mx-auto py-8\">\n" +
                "        <!-- LOGO -->\n" +
                "        <div class=\"flex justify-center mb-6\">\n" +
                "            <img src=\"https://upload.wikimedia.org/wikipedia/commons/a/a7/React-icon.svg\" alt=\"Logo\" class=\"h-20 w-20 rounded-full shadow-lg border-2 border-white bg-white p-1\">\n" +
                "        </div>\n" +
                "        <h1 class=\"text-4xl font-bold text-center text-purple-700 mb-8\">Test Failure Dashboard</h1>\n" +
                "        <!-- Summary Cards -->\n" +
                "        <div class=\"grid grid-cols-1 md:grid-cols-3 gap-6 mb-8\">\n" +
                "            <div class=\"bg-gradient-to-tr from-cyan-200 via-blue-400 to-blue-600 rounded-lg shadow-xl p-4 flex flex-col items-center transform hover:scale-105 transition duration-200 fade-in\">\n" +
                "                <div class=\"text-3xl\">🧮</div>\n" +
                "                <div class=\"text-2xl font-bold text-white\" id=\"totalTests\">{{TOTAL_TESTS}}</div>\n" +
                "                <div class=\"text-white font-semibold\">Total Tests</div>\n" +
                "            </div>\n" +
                "            <div class=\"bg-gradient-to-tr from-green-200 via-green-500 to-emerald-500 rounded-lg shadow-xl p-6 flex flex-col items-center transform hover:scale-105 transition duration-200 fade-in\">\n" +
                "                <div class=\"text-3xl\">✔️</div>\n" +
                "                <div class=\"text-2xl font-bold text-white\" id=\"passedTests\">{{PASSED_TESTS}}</div>\n" +
                "                <div class=\"text-white font-semibold\">Passed</div>\n" +
                "            </div>\n" +
                "            <div class=\"bg-gradient-to-tr from-pink-200 via-red-400 to-red-500 rounded-lg shadow-xl p-6 flex flex-col items-center transform hover:scale-105 transition duration-200 fade-in\">\n" +
                "                <div class=\"text-3xl\">❌</div>\n" +
                "                <div class=\"text-2xl font-bold text-white\" id=\"failedTests\">{{FAILED_TESTS}}</div>\n" +
                "                <div class=\"text-white font-semibold\">Failed</div>\n" +
                "            </div>\n" +
                "        </div>\n" +

                "        <!-- Chart -->\n" +
                "        <div class=\"bg-white rounded-lg shadow p-6 mb-8 flex justify-center\">\n" +
                "            <canvas id=\"failureChart\" width=\"150\" height=\"150\"></canvas>\n" +
                "        </div>\n" +
                "        <!-- Table -->\n" +
                "        <div class=\"bg-white rounded-lg shadow p-6 fade-in\">\n" +
                "            <div class=\"flex justify-between items-center border-b px-6 py-4 bg-purple-600 rounded-t-lg\">\n" +
                "                <h2 class=\"text-xl font-bold text-white\"> Failure Analysis</h2>\n" +
                "            </div>\n" +
                "            <table class=\"min-w-full divide-y divide-gray-200\">\n" +
                "                <thead class=\"bg-blue-100\">\n" +
                "                    <tr>\n" +
                "                        <th class=\"px-4 py-2 text-left text-xs font-semibold text-blue-700\">Sn</th>\n" +
                "                        <th class=\"px-4 py-2 text-left text-xs font-semibold text-blue-700\">Keyword Name</th>\n" +
                "                        <th class=\"px-4 py-2 text-left text-xs font-semibold text-blue-700\">Error Summary</th>\n" +
                "                        <th class=\"px-4 py-2 text-left text-xs font-semibold text-blue-700\">Details</th>\n" +
                "                    </tr>\n" +
                "                </thead>\n" +
                "                <tbody class=\"bg-white divide-y divide-gray-200\">\n" +
                "                    <!-- TABLE_ROWS_PLACEHOLDER -->\n" +
                "                </tbody>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <!-- Modal -->\n" +
                "    <div id=\"logModal\" class=\"fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40 hidden fade-in px-4\">\n" +
                "        <div class=\"bg-white dark:bg-gray-800 rounded-lg shadow-lg w-full max-w-3xl overflow-y-auto min-h-[40vh] max-h-[100vh] pop flex flex-col\">\n" +
                "            <div class=\"flex justify-between items-start border-b px-6 py-4 bg-purple-600 rounded-t-lg\">\n" +
                "                <h2 class=\"text-xl font-bold text-white leading-tight\" id=\"modalKeywordName\">Keyword Name</h2>\n" +
                "                <button onclick=\"closeModal()\" class=\"text-white text-2xl font-bold ml-4 flex-shrink-0\" aria-label=\"Close modal\">&times;</button>\n" +
                "            </div>\n" +
                "            <div class=\"px-6 py-4 overflow-y-auto\">\n" +
                "                <h3 class=\"font-semibold mb-2 text-purple-700\">Failure Logs</h3>\n" +
                "                <pre id=\"modalLogContent\" class=\"bg-gray-100 p-4 rounded text-sm whitespace-pre-wrap max-h-[60vh] overflow-y-auto\"></pre>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +

                "    <script>\n" +
                "        window.onload = function() {\n" +
                "            const ctx = document.getElementById('failureChart').getContext('2d');\n" +
                "            new Chart(ctx, {\n" +
                "                type: 'doughnut',\n" +
                "                data: {\n" +
                "                    labels: ['Passed', 'Failed'],\n" +
                "                    datasets: [{\n" +
                "                        data: {{CHART_DATA}},\n" +
                "                        backgroundColor: ['#22c55e', '#ef4444'],\n" +
                "                        borderWidth: 2,\n" +
                "                        borderColor: '#fff'\n" +
                "                    }]\n" +
                "                },\n" +
                "                options: {\n" +
                "                    responsive: false,\n" +
                "                    plugins: {\n" +
                "                        legend: { position: 'bottom' }\n" +
                "                    },\n" +
                "                    cutout: \"70%\"\n" +
                "                }\n" +
                "            });\n" +
                "        };\n" +
                "        function showModal(keyword, log) {\n" +
                "            document.getElementById('modalKeywordName').innerText = keyword;\n" +
                "            document.getElementById('modalLogContent').innerText = log.replace(/\\\\n/g, '\\n');\n" +
                "            document.getElementById('logModal').classList.remove('hidden');\n" +
                "        }\n" +
                "        function closeModal() {\n" +
                "            document.getElementById('logModal').classList.add('hidden');\n" +
                "        }\n" +
                "        window.onclick = function(event) {\n" +
                "            var modal = document.getElementById('logModal');\n" +
                "            if (event.target === modal) {\n" +
                "                closeModal();\n" +
                "            }\n" +
                "        }\n" +
                "document.addEventListener('DOMContentLoaded', function () {\n" +
                "    const themeToggleBtn = document.getElementById('themeToggleBtn');\n" +
                "    const root = document.documentElement;\n" +
                "    // Load saved theme or default to light\n" +
                "    const savedTheme = localStorage.getItem('theme');\n" +
                "    if (savedTheme === 'dark') {\n" +
                "        root.classList.add('dark');\n" +
                "        themeToggleBtn.textContent = 'Dark Mode';\n" +
                "    } else {\n" +
                "        root.classList.remove('dark');\n" +
                "        themeToggleBtn.textContent = 'Light Mode';\n" +
                "    }\n" +
                "    themeToggleBtn.onclick = function () {\n" +
                "        if (root.classList.contains('dark')) {\n" +
                "            root.classList.remove('dark');\n" +
                "            localStorage.setItem('theme', 'light');\n" +
                "            themeToggleBtn.textContent = 'Light Mode';\n" +
                "        } else {\n" +
                "            root.classList.add('dark');\n" +
                "            localStorage.setItem('theme', 'dark');\n" +
                "            themeToggleBtn.textContent = 'Dark Mode';\n" +
                "        }\n" +
                "    };\n" +
                "});\n" +

                "    </script>\n" +
                "</body>\n" +
                "</html>\n";
    }
}
