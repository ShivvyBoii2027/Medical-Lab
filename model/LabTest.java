
/*
 *  Shivank Gudupally
 * 2026-01-22
 * Period 6
 * Section9
 */
package model;

public class LabTest {

    private static final String STATUS_PENDING = "PENDING";
    private int testId;
    private String testType;
    private String testIdFormatted;  // e.g., "BL001", "CBC002"
    private int urgency;  // 1-5 stars
    private int patientId;
    private int doctorId;
    private String dateCreated;
    private String testData;  // JSON or formatted test results
    private String status;  // PENDING, COMPLETED, CANCELLED

    public LabTest(int testId, String testType, int urgency, int patientId, int doctorId) {
        this.testId = testId;
        this.testType = testType;
        this.urgency = urgency;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.status = STATUS_PENDING;
    }

    // Full constructor - using setters for additional fields to reduce parameter count
    public LabTest(int testId, String testType, String testIdFormatted, int urgency, 
                   int patientId, int doctorId) {
        this.testId = testId;
        this.testType = testType;
        this.testIdFormatted = testIdFormatted;
        this.urgency = urgency;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.status = STATUS_PENDING;
    }

    // Backward compatibility constructor
    public LabTest(int testId, String testType, String priority) {
        this.testId = testId;
        this.testType = testType;
        if ("HIGH".equals(priority)) {
            this.urgency = 5;
        } else if ("MEDIUM".equals(priority)) {
            this.urgency = 3;
        } else {
            this.urgency = 1;
        }
        this.status = STATUS_PENDING;
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getTestIdFormatted() {
        return testIdFormatted;
    }

    public void setTestIdFormatted(String testIdFormatted) {
        this.testIdFormatted = testIdFormatted;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        if (urgency >= 1 && urgency <= 5) {
            this.urgency = urgency;
        }
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTestData() {
        return testData;
    }

    public void setTestData(String testData) {
        this.testData = testData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return testIdFormatted + " - " + testType + " (Urgency: " + urgency + "/5)";    }
}