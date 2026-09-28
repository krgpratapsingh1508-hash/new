package com.college.db;

import java.util.List;
import java.util.Map;

/** Python के DEFAULT_COLUMNS, PERMANENT_TWIN_MAPPINGS वगैरह का Java रूप. */
public final class Columns {
    private Columns() {}
    public static final List<String> DEFAULT = List.of(
        "Admission Year", "Admission Session", "Eligibility Name", "Admission Application Number",
        "Admission Date", "Unique ID", "Roll No.", "Application Enrollment No.",
        "Enrollment No.", "Student Name", "Father Name", "Mother Name", "Date of Birth",
        "Category", "Subject Code", "Subject", "Duration", "Mobile Number", "Email ID", "Address", "Status",
        "Current Year", "Application Number", "Student Abc Id", "Gender", "Admission Category", "Degree",
        "Branch", "Minor Subjects", "Vocational Subjects", "MDC Subjects", "PW/Ap/CE Subjects",
        "Admssion & Enrollment Fees", "Scholarship Name", "Payment Date", "Target Panel Visibility",
        "CCE Marks Obtained", "CCE Attendance Status", "Promotion Status", "Marks Obtained", "Result Status",
        "Exam Remarks", "Document Submit Status");
    /** ⚠️ Python में NAME_CASE_COLUMNS का मान फ़ाइल में अलग जगह है; ये तीन मानकर चला हूँ. */
    public static final List<String> NAME_CASE = List.of("Student Name", "Father Name", "Mother Name");
    public static final Map<String, String> TWINS = Map.of(
        "Application Number", "Admission Application Number",
        "Payment Date", "Admission Date");
}
