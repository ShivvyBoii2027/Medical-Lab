/*
 *  Shivank Gudupally
 * 2026-01-22
 * Period 6
 * Section9
 */
package model;

public class Doctor {

    private int doctorId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String specialization;
    private String idCardNumber;
    private String licenseNumber;
    private String photoPath;

    public Doctor(int doctorId, String firstName, String lastName,
            String specialization, String idCardNumber, String licenseNumber) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.middleName = "";
        this.lastName = lastName;
        this.specialization = specialization;
        this.idCardNumber = idCardNumber;
        this.licenseNumber = licenseNumber;
        this.photoPath = "";
    }

    // Getters and setters
    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getFullName() {
        if (middleName != null && !middleName.isEmpty()) {
            return firstName + " " + middleName + " " + lastName;
        } else {
            return firstName + " " + lastName;
        }
    }

    public String getFormattedInfo() {
        return getFullName() + " - " + specialization;
    }

    @Override
    public String toString() {
        return getFormattedInfo();
    }
}
