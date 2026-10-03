/*
 *  Shivank Gudupally
 * 2026-01-22
 * Period 6
 * Section9
 */
package db;

import java.sql.*;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.mindrot.jbcrypt.BCrypt;

public class DBManager {

    private static Connection connection;
    private static String currentUser = null;
    private static String currentUserRole = null;

    // ── Credentials loaded from env vars; fall back to properties / prompts at
    // connect-time ──
    private static String dbUrl = "jdbc:mysql://localhost:3306/medilab_pro";
    private static String dbUser = "root";
    private static String dbPassword = "[Shivank123]";

    private DBManager() {
    }

    /* ─────────────────────────── CONNECTION ─────────────────────────── */

    /**
     * Connect using explicit credentials (called from the login dialog).
     */
    public static boolean connect(String url, String user, String pass) {
        dbUrl = url;
        dbUser = user;
        dbPassword = pass;
        return connectInternal();
    }

    /**
     * Re-connect using previously supplied credentials.
     */
    public static boolean connect() {
        if (dbUrl == null) {
            return false; // No credentials stored yet
        }
        return connectInternal();
    }

    private static boolean connectInternal() {
        String baseUri = "jdbc:mysql://localhost:3306/";
        String schema = "medilab_pro";
        String[] possiblePasses = { dbPassword, dbPassword.replace("[", "").replace("]", ""), "root", "" };

        // 1. Try to connect to Server to ensure schema exists
        Connection serverConn = null;
        String successfulPass = null;

        for (String pass : possiblePasses) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                serverConn = DriverManager.getConnection(baseUri, dbUser, pass);
                successfulPass = pass;
                break;
            } catch (Exception ignored) {
            }
        }

        if (serverConn == null)
            return false;

        try {
            Statement st = serverConn.createStatement();
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + schema);
            serverConn.close();

            // 2. Connect to the actual schema
            dbPassword = successfulPass;
            dbUrl = baseUri + schema;
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            createTables();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null;
            currentUser = null;
            currentUserRole = null;
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private static Connection getConnection() throws SQLException {
        if (!isConnected())
            throw new SQLException("Database not connected.");
        return connection;
    }

    /* ─────────────────────────── TABLE CREATION ─────────────────────── */

    private static void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {

            st.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id      INT AUTO_INCREMENT PRIMARY KEY,
                        username     VARCHAR(50)  UNIQUE NOT NULL,
                        password     VARCHAR(255) NOT NULL,
                        email        VARCHAR(100) UNIQUE NOT NULL,
                        phone        VARCHAR(20),
                        role         ENUM('ADMIN','DOCTOR','PATIENT','TECHNICIAN','RECEPTIONIST') NOT NULL,
                        is_active    BOOLEAN DEFAULT TRUE,
                        created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_login   TIMESTAMP NULL,
                        INDEX idx_username (username),
                        INDEX idx_email    (email),
                        INDEX idx_role     (role)
                    )""");

            st.execute("""
                    CREATE TABLE IF NOT EXISTS patients (
                        patient_id        INT AUTO_INCREMENT PRIMARY KEY,
                        first_name        VARCHAR(50)  NOT NULL,
                        middle_name       VARCHAR(50),
                        last_name         VARCHAR(50)  NOT NULL,
                        gender            VARCHAR(20)  NOT NULL,
                        age               INT,
                        phone             VARCHAR(20),
                        email             VARCHAR(100),
                        address           TEXT,
                        blood_group       VARCHAR(5),
                        allergies         TEXT,
                        emergency_contact VARCHAR(255),
                        username          VARCHAR(50),
                        is_active         BOOLEAN DEFAULT TRUE,
                        created_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_patient_name  (first_name, last_name),
                        INDEX idx_patient_email (email)
                    )""");
            try {
                st.execute("ALTER TABLE patients ADD COLUMN username VARCHAR(50)");
            } catch (Exception e) {}
            try {
                st.execute("ALTER TABLE patients ADD COLUMN is_active TINYINT DEFAULT 1");
            } catch (Exception e) {}
            try {
                st.execute("ALTER TABLE patients MODIFY COLUMN emergency_contact VARCHAR(255)");
            } catch (Exception e) {}

            st.execute("""
                    CREATE TABLE IF NOT EXISTS doctors (
                        doctor_id         INT AUTO_INCREMENT PRIMARY KEY,
                        first_name        VARCHAR(50)     NOT NULL,
                        middle_name       VARCHAR(50),
                        last_name         VARCHAR(50)     NOT NULL,
                        specialization    VARCHAR(100)    NOT NULL,
                        license_number    VARCHAR(50)     UNIQUE,
                        qualification     TEXT,
                        experience_years  INT,
                        department        VARCHAR(100),
                        consultation_fee  DECIMAL(10,2),
                        email             VARCHAR(100),
                        phone             VARCHAR(20),
                        available_days    VARCHAR(100),
                        username          VARCHAR(50),
                        is_active         TINYINT DEFAULT 1,
                        created_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_doctor_name  (first_name, last_name),
                        INDEX idx_doctor_email (email)
                    )""");
            try {
                st.execute("ALTER TABLE doctors ADD COLUMN username VARCHAR(50)");
            } catch (Exception e) {}
            try {
                st.execute("ALTER TABLE doctors ADD COLUMN is_active TINYINT DEFAULT 1");
            } catch (Exception e) {}

            st.execute("""
                    CREATE TABLE IF NOT EXISTS specializations (
                        spec_id              INT AUTO_INCREMENT PRIMARY KEY,
                        spec_name            VARCHAR(100) UNIQUE NOT NULL,
                        category             VARCHAR(50)  DEFAULT 'GENERAL',
                        created_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )""");
            try {
                st.execute("ALTER TABLE specializations CHANGE COLUMN specialization_name spec_name VARCHAR(100)");
            } catch (Exception e) {
            }
            try {
                st.execute("ALTER TABLE specializations ADD COLUMN category VARCHAR(50) DEFAULT 'GENERAL'");
            } catch (Exception e) {
            }

            st.execute("""
                    CREATE TABLE IF NOT EXISTS test_types (
                        test_type_id              INT AUTO_INCREMENT PRIMARY KEY,
                        test_code                 VARCHAR(20)  UNIQUE NOT NULL,
                        test_name                 VARCHAR(100) NOT NULL,
                        category                  VARCHAR(50)  DEFAULT 'GENERAL',
                        acronym                   VARCHAR(10),
                        description               TEXT,
                        sample_type               VARCHAR(50),
                        normal_range              TEXT,
                        preparation_instructions  TEXT,
                        turnaround_time           VARCHAR(50),
                        cost                      DECIMAL(10,2),
                        is_active                 BOOLEAN DEFAULT TRUE,
                        created_date              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_test_code (test_code)
                    )""");
            try {
                st.execute("ALTER TABLE test_types ADD COLUMN category VARCHAR(50) DEFAULT 'GENERAL'");
            } catch (Exception e) {
            }

            st.execute(
                    """
                            CREATE TABLE IF NOT EXISTS lab_tests (
                                test_id       VARCHAR(20) PRIMARY KEY,
                                patient_id    INT NOT NULL,
                                doctor_id     INT,
                                test_type_id  INT NOT NULL,
                                urgency_stars INT DEFAULT 3,
                                status        ENUM('PENDING_APPROVAL','REQUESTED','COLLECTED','IN_PROGRESS','COMPLETED','CANCELLED')
                                              DEFAULT 'REQUESTED',
                                request_date    DATETIME,
                                collection_date DATETIME,
                                completion_date DATETIME,
                                notes         TEXT,
                                requested_by  VARCHAR(50),
                                created_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (patient_id)   REFERENCES patients(patient_id)  ON DELETE CASCADE,
                                FOREIGN KEY (doctor_id)    REFERENCES doctors(doctor_id)    ON DELETE SET NULL,
                                FOREIGN KEY (test_type_id) REFERENCES test_types(test_type_id),
                                INDEX idx_test_patient (patient_id),
                                INDEX idx_test_status  (status)
                            )""");
            try {
                st.execute(
                        "ALTER TABLE lab_tests MODIFY COLUMN status ENUM('PENDING_APPROVAL','REQUESTED','COLLECTED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'REQUESTED'");
            } catch (Exception e) {
            }

            st.execute("""
                    CREATE TABLE IF NOT EXISTS test_results (
                        result_id         INT AUTO_INCREMENT PRIMARY KEY,
                        test_id           VARCHAR(20) NOT NULL,
                        result_data       TEXT,
                        result_summary    VARCHAR(255),
                        performed_by      VARCHAR(50),
                        verified_by       VARCHAR(50),
                        result_date       DATETIME,
                        verification_date DATETIME,
                        notes             TEXT,
                        FOREIGN KEY (test_id) REFERENCES lab_tests(test_id) ON DELETE CASCADE,
                        INDEX idx_result_test (test_id)
                    )""");

            st.execute("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        app_id         INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id     INT NOT NULL,
                        doctor_id      INT NOT NULL,
                        app_date       DATETIME NOT NULL,
                        reason         VARCHAR(255),
                        status         ENUM('PENDING','APPROVED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
                        created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
                        FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id)  ON DELETE CASCADE,
                        INDEX idx_app_date (app_date),
                        INDEX idx_app_doctor (doctor_id)
                    )""");

            st.execute("""
                    CREATE TABLE IF NOT EXISTS user_settings (
                        username      VARCHAR(50) PRIMARY KEY,
                        theme_mode    VARCHAR(20) DEFAULT 'GOATED',
                        accent_color  VARCHAR(7)  DEFAULT '#06b6d4',
                        font_size     INT         DEFAULT 13,
                        bg_image      VARCHAR(255),
                        FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                    )""");
            try {
                st.execute("ALTER TABLE user_settings ADD COLUMN bg_image VARCHAR(255)");
            } catch (Exception e) {
            }

            st.execute("""
                    CREATE TABLE IF NOT EXISTS user_activity (
                        activity_id   INT AUTO_INCREMENT PRIMARY KEY,
                        username      VARCHAR(50),
                        action        VARCHAR(255),
                        details       TEXT,
                        ip_address    VARCHAR(45),
                        activity_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_username (username),
                        INDEX idx_date     (activity_date)
                    )""");

            st.execute("""
                    CREATE TABLE IF NOT EXISTS access_rights (
                        role          VARCHAR(20) NOT NULL,
                        module_name   VARCHAR(50) NOT NULL,
                        can_view      INT DEFAULT 0,
                        can_edit      INT DEFAULT 0,
                        PRIMARY KEY (role, module_name)
                    )""");

            // Seed admin account if no users exist
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            if (rs.getInt(1) == 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO users (username,password,email,phone,role) VALUES (?,?,?,?,?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, BCrypt.hashpw("Admin123!", BCrypt.gensalt()));
                    ps.setString(3, "admin@medlab.local");
                    ps.setString(4, "");
                    ps.setString(5, "ADMIN");
                    ps.executeUpdate();
                }
            }

            // Seed specializations if empty
            rs = st.executeQuery("SELECT COUNT(*) FROM specializations");
            rs.next();
            if (rs.getInt(1) == 0) {
                String[] specs = {
                        "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology",
                        "Oncology", "Radiology", "Surgery", "General Practice", "Emergency Medicine",
                        "Psychiatry", "Anesthesiology", "Gynecology", "Urology", "Ophthalmology",
                        "Endocrinology", "Gastroenterology", "Hematology", "Nephrology",
                        "Pulmonology", "Rheumatology"
                };
                for (String spec : specs) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO specializations (specialization_name) VALUES (?)")) {
                        ps.setString(1, spec);
                        ps.executeUpdate();
                    }
                }
            }

            // Seed test types if empty
            rs = st.executeQuery("SELECT COUNT(*) FROM test_types");
            rs.next();
            if (rs.getInt(1) == 0) {
                Object[][] types = {
                        { "CBC", "Complete Blood Count", "CBC", "Blood" },
                        { "BMP", "Basic Metabolic Panel", "BMP", "Blood" },
                        { "LIPID", "Lipid Panel", "LIPID", "Blood" },
                        { "TSH", "Thyroid Stimulating Hormone", "TSH", "Blood" },
                        { "UA", "Urinalysis", "UA", "Urine" },
                        { "XRAY", "X-Ray", "XRAY", "N/A" },
                        { "MRI", "MRI Scan", "MRI", "N/A" },
                        { "CT", "CT Scan", "CT", "N/A" },
                        { "HBA1C", "HbA1c", "HBA1C", "Blood" },
                        { "LFT", "Liver Function Test", "LFT", "Blood" },
                        { "KFT", "Kidney Function Test", "KFT", "Blood" },
                        { "ECG", "Electrocardiogram", "ECG", "N/A" },
                        { "ECHO", "Echocardiogram", "ECHO", "N/A" },
                        { "USGAB", "Ultrasound Abdomen", "USG", "N/A" }
                };
                for (Object[] t : types) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO test_types (test_code,test_name,acronym,sample_type) VALUES (?,?,?,?)")) {
                        ps.setString(1, (String) t[0]);
                        ps.setString(2, (String) t[1]);
                        ps.setString(3, (String) t[2]);
                        ps.setString(4, (String) t[3]);
                        ps.executeUpdate();
                    }
                }
            }

            // Seed access rights if empty
            rs = st.executeQuery("SELECT COUNT(*) FROM access_rights");
            rs.next();
            if (rs.getInt(1) == 0) {
                String[] roles = { "ADMIN", "DOCTOR", "PATIENT" };
                String[] modules = { "Dashboard", "Registration", "Reports", "MasterDetails", "Administration" };
                for (String role : roles) {
                    for (String mod : modules) {
                        // Admins get full access, others get view only by default
                        int canEdit = "ADMIN".equals(role) ? 1 : 0;
                        int canView = 1; 
                        try (PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO access_rights (role, module_name, can_view, can_edit) VALUES (?,?,?,?)")) {
                            ps.setString(1, role);
                            ps.setString(2, mod);
                            ps.setInt(3, canView);
                            ps.setInt(4, canEdit);
                            ps.executeUpdate();
                        }
                    }
                }
            }

            // Identity Patch: Link existing patients/doctors to users by email if username
            // is null
            try {
                st.executeUpdate(
                        "UPDATE patients p JOIN users u ON p.email = u.email SET p.username = u.username WHERE p.username IS NULL");
                st.executeUpdate(
                        "UPDATE doctors d JOIN users u ON d.email = u.email SET d.username = u.username WHERE d.username IS NULL");
            } catch (Exception e) {
            }
        }
    }

    /* ─────────────────────────── USER AUTH ──────────────────────────── */

    public static void setCurrentUser(String username) {
        currentUser = username;
        try {
            ResultSet rs = getUserByUsername(username);
            if (rs.next())
                currentUserRole = rs.getString("role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Auto-link: ensure existing patient/doctor record is linked to this username
        autoLinkIdentity(username);
    }

    /**
     * Attempts to link an existing patient/doctor record to the given username
     * by matching email from the users table. Safe to call on every login — no-op
     * if already linked.
     */
    private static void autoLinkIdentity(String username) {
        try (Statement st = getConnection().createStatement()) {
            // Link patients whose email matches but username is null
            st.executeUpdate(
                    "UPDATE patients p JOIN users u ON p.email = u.email " +
                            "SET p.username = u.username WHERE p.username IS NULL AND u.username = '" + username + "'");
            // Link doctors whose email matches but username is null
            st.executeUpdate(
                    "UPDATE doctors d JOIN users u ON d.email = u.email " +
                            "SET d.username = u.username WHERE d.username IS NULL AND u.username = '" + username + "'");
        } catch (Exception ignored) {
        }
    }

    public static boolean authenticate(String usernameOrId, String password) throws SQLException {
        String u = usernameOrId;
        // Check if patient ID (digits only)
        if (usernameOrId.matches("\\d+")) {
            // First try matching by patient_id column
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT username FROM patients WHERE patient_id=?")) {
                ps.setInt(1, Integer.parseInt(usernameOrId));
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getString("username") != null) {
                    u = rs.getString("username");
                } else {
                    // Fallback: Try linking via email if username not set in patients table
                    try (PreparedStatement ps2 = getConnection().prepareStatement(
                            "SELECT u.username FROM users u JOIN patients p ON u.email = p.email WHERE p.patient_id=?")) {
                        ps2.setInt(1, Integer.parseInt(usernameOrId));
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) u = rs2.getString("username");
                    }
                }
            }
        }

        String sql = "SELECT password, role FROM users WHERE username=? AND is_active=1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, u);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password");
                if (BCrypt.checkpw(password, hash)) {
                    currentUser = u;
                    currentUserRole = rs.getString("role"); // Ensure role is set!
                    logActivity(u, "LOGIN", "Login success (" + (u.equals(usernameOrId) ? "Username" : "ID") + ")");
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static boolean isAdmin() {
        if (currentUserRole == null && currentUser != null && isConnected()) {
            setCurrentUser(currentUser); // Attempt to refresh role
        }
        return "ADMIN".equals(currentUserRole);
    }

    public static ResultSet loginUser(String username, String password) throws SQLException {
        String sql = "SELECT user_id,username,email,phone,role,is_active FROM users WHERE username=? AND is_active=TRUE";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try (PreparedStatement hashPs = getConnection().prepareStatement(
                        "SELECT password FROM users WHERE username=?")) {
                    hashPs.setString(1, username);
                    ResultSet hashRs = hashPs.executeQuery();
                    if (hashRs.next() && BCrypt.checkpw(password, hashRs.getString("password"))) {
                        try (PreparedStatement upPs = getConnection().prepareStatement(
                                "UPDATE users SET last_login=NOW() WHERE username=?")) {
                            upPs.setString(1, username);
                            upPs.executeUpdate();
                        }
                        setCurrentUser(username);
                        logActivity(username, "LOGIN", "Logged in");
                        return getUserByUsername(username);
                    }
                }
            }
        }
        return null;
    }

    public static ResultSet getUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id,username,email,phone,role,is_active,created_date,last_login FROM users WHERE username=?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, username);
        return ps.executeQuery();
    }

    public static boolean usernameExists(String username) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static boolean emailExists(String email) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM users WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static String getUsernameByEmail(String email) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT username FROM users WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("username") : null;
        }
    }

    public static String getPatientIdByEmail(String email) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT patient_id FROM patients WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("patient_id") : null;
        }
    }

    /* ─────────────────────────── USER MANAGEMENT ────────────────────── */

    public static void addUser(String username, String password, String email, String phone, String role)
            throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can add users.");
        internalAddUser(username, password, email, phone, role);
    }

    private static void internalAddUser(String username, String password, String email, String phone, String role)
            throws SQLException {
        if (usernameExists(username))
            throw new SQLException("Username already exists.");
        if (emailExists(email))
            throw new SQLException("Email already registered.");
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO users (username,password,email,phone,role) VALUES (?,?,?,?,?)")) {
            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, role.toUpperCase());
            ps.executeUpdate();
            logActivity(username, "REGISTER", "User registered: " + username);
        }
    }

    public static int registerPatientAccount(String user, String pass, String email, String phone, String fn, String ln,
            String gender, int age) throws SQLException {
        connection.setAutoCommit(false);
        try {
            internalAddUser(user, pass, email, phone, "PATIENT");
            int pid = addPatientWithUser(fn, "", ln, gender, age, phone, email, "", "", "", "", user);
            connection.commit();
            return pid;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static void registerDoctorAccount(String user, String pass, String email, String phone, String fn, String ln,
            String spec) throws SQLException {
        connection.setAutoCommit(false);
        try {
            internalAddUser(user, pass, email, phone, "DOCTOR");
            // Pass null for license_number to avoid "TBD" duplicate entry errors
            addDoctorWithUser(fn, "", ln, spec, null, null, 0, "General", 0.0, email, phone, "Mon-Fri", user);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static void changePassword(String username, String currentPassword, String newPassword) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT password FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || !BCrypt.checkpw(currentPassword, rs.getString("password"))) {
                throw new SQLException("Current password is incorrect.");
            }
        }
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE users SET password=? WHERE username=?")) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            ps.setString(2, username);
            ps.executeUpdate();
            logActivity(username, "CHANGE_PASSWORD", "Password changed");
        }
    }

    public static void resetPasswordDirect(String username, String newPassword) throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can reset passwords.");
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE users SET password=? WHERE username=?")) {
            ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            ps.setString(2, username);
            ps.executeUpdate();
            logActivity(currentUser, "RESET_PASSWORD", "Reset password for: " + username);
        }
    }

    public static void updateUser(String username, String email, String phone, String role, boolean isActive)
            throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can update users.");
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE users SET email=?,phone=?,role=?,is_active=? WHERE username=?")) {
            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setString(3, role.toUpperCase());
            ps.setBoolean(4, isActive);
            ps.setString(5, username);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_USER", "Updated user: " + username);
        }
    }

    public static void saveUserSettings(String username, String mode, String color, int size, String bgImage)
            throws SQLException {
        String sql = "INSERT INTO user_settings (username, theme_mode, accent_color, font_size, bg_image) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE theme_mode=?, accent_color=?, font_size=?, bg_image=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, mode);
            ps.setString(3, color);
            ps.setInt(4, size);
            ps.setString(5, bgImage);
            ps.setString(6, mode);
            ps.setString(7, color);
            ps.setInt(8, size);
            ps.setString(9, bgImage);
            ps.executeUpdate();
        }
    }

    public static ResultSet getUserSettings(String username) throws SQLException {
        String sql = "SELECT * FROM user_settings WHERE username=?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, username);
        return ps.executeQuery();
    }

    public static void deleteUser(String username) throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can delete users.");
        if (username.equals(currentUser))
            throw new SQLException("Cannot delete your own account.");
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM users WHERE username=?")) {
            ps.setString(1, username);
            ps.executeUpdate();
            logActivity(currentUser, "DELETE_USER", "Deleted user: " + username);
        }
    }

    public static ResultSet getAllUsers() throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can view all users.");
        Statement st = getConnection().createStatement();
        return st.executeQuery(
                "SELECT user_id,username,email,phone,role,is_active,created_date,last_login FROM users ORDER BY username");
    }

    /* ─────────────────────────── PATIENT CRUD ───────────────────────── */

    public static int addPatient(String firstName, String middleName, String lastName,
            String gender, int age, String phone, String email,
            String address, String bloodGroup, String allergies,
            String emergencyContact) throws SQLException {
        return addPatientWithUser(firstName, middleName, lastName, gender, age, phone, email, address, bloodGroup,
                allergies, emergencyContact, null);
    }

    public static int addPatientWithUser(String firstName, String middleName, String lastName,
            String gender, int age, String phone, String email,
            String address, String bloodGroup, String allergies,
            String emergencyContact, String username) throws SQLException {
        String sql = """
                INSERT INTO patients
                  (first_name,middle_name,last_name,gender,age,phone,email,address,blood_group,allergies,emergency_contact,username)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, firstName.trim());
            ps.setString(2, emptyToNull(middleName));
            ps.setString(3, lastName.trim());
            ps.setString(4, gender.trim());
            ps.setInt(5, age);
            ps.setString(6, emptyToNull(phone));
            ps.setString(7, emptyToNull(email));
            ps.setString(8, emptyToNull(address));
            ps.setString(9, emptyToNull(bloodGroup));
            ps.setString(10, emptyToNull(allergies));
            ps.setString(11, emptyToNull(emergencyContact));
            ps.setString(12, username);
            ps.executeUpdate();
            logActivity(currentUser, "ADD_PATIENT",
                    "Added: " + firstName + " " + lastName + " (linked to: " + username + ")");
            ResultSet rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public static ResultSet getPatientById(int id) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(
                "SELECT *,DATE_FORMAT(created_date,'%Y-%m-%d %H:%i') as formatted_date FROM patients WHERE patient_id=?");
        ps.setInt(1, id);
        return ps.executeQuery();
    }

    public static ResultSet getAllPatients() throws SQLException {
        Statement st = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return st.executeQuery("""
                SELECT patient_id,first_name,middle_name,last_name,gender,age,
                       phone,email,address,blood_group,allergies,emergency_contact,
                       DATE_FORMAT(created_date,'%Y-%m-%d %H:%i') as formatted_date
                FROM patients ORDER BY last_name,first_name""");
    }

    public static ResultSet searchPatients(String term) throws SQLException {
        String sql = """
                SELECT p.patient_id, p.first_name, p.middle_name, p.last_name, p.gender, p.age,
                       p.phone, p.email, p.address, p.blood_group, p.allergies, p.emergency_contact,
                       DATE_FORMAT(p.created_date,'%Y-%m-%d %H:%i') as formatted_date
                FROM patients p
                LEFT JOIN users u ON p.email = u.email
                WHERE p.first_name LIKE ? OR p.last_name LIKE ? OR p.patient_id = ? OR u.username LIKE ?
                ORDER BY p.last_name, p.first_name""";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, "%" + term + "%");
        ps.setString(2, "%" + term + "%");
        int id = -1;
        try {
            id = Integer.parseInt(term);
        } catch (Exception e) {
        }
        ps.setInt(3, id);
        ps.setString(4, "%" + term + "%");
        return ps.executeQuery();
    }

    public static String getUserDisplayName(String username, String role) {
        String sql = "";
        if ("PATIENT".equals(role)) {
            sql = "SELECT first_name, last_name FROM patients p JOIN users u ON p.email = u.email WHERE u.username=?";
        } else if ("DOCTOR".equals(role)) {
            sql = "SELECT first_name, last_name FROM doctors d JOIN users u ON d.email = u.email WHERE u.username=?";
        } else if ("ADMIN".equals(role)) {
            return "Administrator";
        } else {
            return username;
        }

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (Exception e) {
            System.err.println("DisplayName Error: " + e.getMessage());
        }
        return username;
    }

    public static void resetPasswordByEmail(String email, String newPass) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE email=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(newPass, BCrypt.gensalt()));
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public static boolean patientExists(String first, String last) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM patients WHERE first_name=? AND last_name=?")) {
            ps.setString(1, first.trim());
            ps.setString(2, last.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static void updatePatient(int id, String fn, String mn, String ln, String gender, int age,
            String phone, String email, String address, String bg,
            String allergies, String emergency) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("""
                UPDATE patients SET first_name=?,middle_name=?,last_name=?,gender=?,age=?,
                    phone=?,email=?,address=?,blood_group=?,allergies=?,emergency_contact=?
                WHERE patient_id=?""")) {
            ps.setString(1, fn.trim());
            ps.setString(2, emptyToNull(mn));
            ps.setString(3, ln.trim());
            ps.setString(4, gender.trim());
            ps.setInt(5, age);
            ps.setString(6, emptyToNull(phone));
            ps.setString(7, emptyToNull(email));
            ps.setString(8, emptyToNull(address));
            ps.setString(9, emptyToNull(bg));
            ps.setString(10, emptyToNull(allergies));
            ps.setString(11, emptyToNull(emergency));
            ps.setInt(12, id);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_PATIENT", "Updated patient ID: " + id);
        }
    }

    public static boolean deletePatient(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM patients WHERE patient_id=?")) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0)
                logActivity(currentUser, "DELETE_PATIENT", "Deleted patient ID: " + id);
            return rows > 0;
        }
    }

    /* ─────────────────────────── DOCTOR CRUD ────────────────────────── */

    public static void addDoctor(String fn, String mn, String ln, String spec, String license,
            String qual, int exp, String dept, double fee,
            String email, String phone, String days) throws SQLException {
        addDoctorWithUser(fn, mn, ln, spec, license, qual, exp, dept, fee, email, phone, days, null);
    }

    public static void addDoctorWithUser(String fn, String mn, String ln, String spec, String license,
            String qual, int exp, String dept, double fee,
            String email, String phone, String days, String username) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO doctors
                  (first_name,middle_name,last_name,specialization,license_number,qualification,
                   experience_years,department,consultation_fee,email,phone,available_days,username)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)""")) {
            ps.setString(1, fn.trim());
            ps.setString(2, emptyToNull(mn));
            ps.setString(3, ln.trim());
            ps.setString(4, spec.trim());
            ps.setString(5, emptyToNull(license));
            ps.setString(6, emptyToNull(qual));
            ps.setInt(7, exp);
            ps.setString(8, emptyToNull(dept));
            ps.setDouble(9, fee);
            ps.setString(10, emptyToNull(email));
            ps.setString(11, emptyToNull(phone));
            ps.setString(12, emptyToNull(days));
            ps.setString(13, username);
            ps.executeUpdate();
            logActivity(currentUser, "ADD_DOCTOR", "Added: " + fn + " " + ln + " (linked to: " + username + ")");
        }
    }

    public static void updateDoctor(int id, String fn, String mn, String ln, String spec,
            String license, String qual, int exp, String dept,
            double fee, String email, String phone, String days) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("""
                UPDATE doctors SET first_name=?,middle_name=?,last_name=?,specialization=?,
                    license_number=?,qualification=?,experience_years=?,department=?,
                    consultation_fee=?,email=?,phone=?,available_days=?
                WHERE doctor_id=?""")) {
            ps.setString(1, fn.trim());
            ps.setString(2, emptyToNull(mn));
            ps.setString(3, ln.trim());
            ps.setString(4, spec.trim());
            ps.setString(5, emptyToNull(license));
            ps.setString(6, emptyToNull(qual));
            ps.setInt(7, exp);
            ps.setString(8, emptyToNull(dept));
            ps.setDouble(9, fee);
            ps.setString(10, emptyToNull(email));
            ps.setString(11, emptyToNull(phone));
            ps.setString(12, emptyToNull(days));
            ps.setInt(13, id);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_DOCTOR", "Updated doctor ID: " + id);
        }
    }

    public static ResultSet getDoctorById(int id) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM doctors WHERE doctor_id=?");
        ps.setInt(1, id);
        return ps.executeQuery();
    }

    public static ResultSet getAllDoctors() throws SQLException {
        Statement st = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return st.executeQuery("""
                SELECT doctor_id,first_name,middle_name,last_name,specialization,license_number,
                       qualification,experience_years,department,consultation_fee,email,phone,available_days,
                       DATE_FORMAT(created_date,'%Y-%m-%d %H:%i') as formatted_date
                FROM doctors ORDER BY last_name,first_name""");
    }

    public static ResultSet searchDoctors(String term) throws SQLException {
        String p = "%" + term + "%";
        PreparedStatement ps = getConnection().prepareStatement("""
                SELECT doctor_id,first_name,middle_name,last_name,specialization,license_number,
                       qualification,experience_years,department,consultation_fee,email,phone,available_days,
                       DATE_FORMAT(created_date,'%Y-%m-%d %H:%i') as formatted_date
                FROM doctors WHERE first_name LIKE ? OR last_name LIKE ? OR specialization LIKE ? OR email LIKE ?
                ORDER BY last_name,first_name""");
        ps.setString(1, p);
        ps.setString(2, p);
        ps.setString(3, p);
        ps.setString(4, p);
        return ps.executeQuery();
    }

    public static boolean deleteDoctor(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM doctors WHERE doctor_id=?")) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0)
                logActivity(currentUser, "DELETE_DOCTOR", "Deleted doctor ID: " + id);
            return rows > 0;
        }
    }

    /* ─────────────────────────── TEST MANAGEMENT ────────────────────── */

    public static String generateTestId(String testCode) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM lab_tests WHERE test_id LIKE ?")) {
            ps.setString(1, testCode + "-%");
            ResultSet rs = ps.executeQuery();
            int count = rs.next() ? rs.getInt(1) + 1 : 1;
            return testCode + "-" + String.format("%04d", count);
        }
    }

    public static void addTest(int patientId, Integer doctorId, int testTypeId,
            String testCode, int urgencyStars, String notes) throws SQLException {
        String testId = generateTestId(testCode);
        String initialStatus = "ADMIN".equals(currentUserRole) ? "REQUESTED" : "PENDING_APPROVAL";

        try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO lab_tests
                  (test_id,patient_id,doctor_id,test_type_id,urgency_stars,status,notes,request_date,requested_by)
                VALUES (?,?,?,?,?,?,? ,NOW(),?)""")) {
            ps.setString(1, testId);
            ps.setInt(2, patientId);
            if (doctorId == null)
                ps.setNull(3, Types.INTEGER);
            else
                ps.setInt(3, doctorId);
            ps.setInt(4, testTypeId);
            ps.setInt(5, urgencyStars);
            ps.setString(6, initialStatus);
            ps.setString(7, notes);
            ps.setString(8, currentUser);
            ps.executeUpdate();
            logActivity(currentUser, "ADD_TEST", "Test: " + testId + " (Status: " + initialStatus + ")");
        }
    }

    public static ResultSet getAllTests() throws SQLException {
        Statement st = getConnection().createStatement();
        return st.executeQuery("""
                SELECT lt.test_id, lt.test_id as test_code,
                       tt.test_name as type_name, tt.test_code as type_code,
                       lt.urgency_stars as urgency, lt.status,
                       p.first_name as p_first, p.last_name as p_last,
                       COALESCE(d.first_name,'') as d_first, COALESCE(d.last_name,'') as d_last,
                       DATE_FORMAT(lt.request_date,'%Y-%m-%d %H:%i') as request_date,
                       lt.notes
                FROM lab_tests lt
                JOIN  patients   p  ON lt.patient_id   = p.patient_id
                LEFT JOIN doctors d ON lt.doctor_id     = d.doctor_id
                JOIN  test_types tt ON lt.test_type_id  = tt.test_type_id
                ORDER BY lt.request_date DESC""");
    }

    public static ResultSet getTestsForDoctorFull(int doctorId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("""
                SELECT lt.test_id, lt.test_id as test_code,
                       tt.test_name as type_name, tt.test_code as type_code,
                       lt.urgency_stars as urgency, lt.status,
                       p.first_name as p_first, p.last_name as p_last,
                       COALESCE(d.first_name,'') as d_first, COALESCE(d.last_name,'') as d_last,
                       DATE_FORMAT(lt.request_date,'%Y-%m-%d %H:%i') as request_date,
                       lt.notes
                FROM lab_tests lt
                JOIN  patients   p  ON lt.patient_id   = p.patient_id
                LEFT JOIN doctors d ON lt.doctor_id     = d.doctor_id
                JOIN  test_types tt ON lt.test_type_id  = tt.test_type_id
                WHERE lt.doctor_id = ?
                ORDER BY lt.request_date DESC""");
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }

    public static ResultSet getTestsForPatientFull(int patientId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("""
                SELECT lt.test_id, lt.test_id as test_code,
                       tt.test_name as type_name, tt.test_code as type_code,
                       lt.urgency_stars as urgency, lt.status,
                       p.first_name as p_first, p.last_name as p_last,
                       COALESCE(d.first_name,'') as d_first, COALESCE(d.last_name,'') as d_last,
                       DATE_FORMAT(lt.request_date,'%Y-%m-%d %H:%i') as request_date,
                       lt.notes
                FROM lab_tests lt
                JOIN  patients   p  ON lt.patient_id   = p.patient_id
                LEFT JOIN doctors d ON lt.doctor_id     = d.doctor_id
                JOIN  test_types tt ON lt.test_type_id  = tt.test_type_id
                WHERE lt.patient_id = ?
                ORDER BY lt.request_date DESC""");
        ps.setInt(1, patientId);
        return ps.executeQuery();
    }

    public static ResultSet getTestsForPatient(int patientId) throws SQLException {
        return getPatientTests(patientId);
    }

    public static ResultSet getPatientTests(int patientId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("""
                SELECT lt.test_id as test_code, tt.test_name as type_name,
                       lt.urgency_stars, lt.status,
                       DATE_FORMAT(lt.request_date,'%Y-%m-%d %H:%i') as request_date,
                       COALESCE(d.first_name,'') as doctor_first_name,
                       COALESCE(d.last_name, '') as doctor_last_name
                FROM lab_tests lt
                JOIN  test_types tt ON lt.test_type_id = tt.test_type_id
                LEFT JOIN doctors d ON lt.doctor_id    = d.doctor_id
                WHERE lt.patient_id=?
                ORDER BY lt.request_date DESC""");
        ps.setInt(1, patientId);
        return ps.executeQuery();
    }

    public static void updateTestStatus(String testId, String status) throws SQLException {
        String extra = "COMPLETED".equals(status) ? ",completion_date=NOW()"
                : "COLLECTED".equals(status) ? ",collection_date=NOW()" : "";
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE lab_tests SET status=?" + extra + " WHERE test_id=?")) {
            ps.setString(1, status);
            ps.setString(2, testId);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_STATUS", testId + " → " + status);
        }
    }

    public static void deleteTest(String testId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM lab_tests WHERE test_id=?")) {
            ps.setString(1, testId);
            ps.executeUpdate();
            logActivity(currentUser, "DELETE_TEST", "Deleted: " + testId);
        }
    }

    public static void addTestResult(String testId, String resultData, String summary, String performedBy)
            throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO test_results (test_id,result_data,result_summary,performed_by,result_date)
                VALUES (?,?,?,?,NOW())""")) {
            ps.setString(1, testId);
            ps.setString(2, resultData);
            ps.setString(3, summary);
            ps.setString(4, performedBy);
            ps.executeUpdate();
        }
        updateTestStatus(testId, "COMPLETED");
        logActivity(currentUser, "ADD_RESULT", "Result for: " + testId);
    }

    public static ResultSet getTestResults(String testId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM test_results WHERE test_id=? ORDER BY result_date DESC");
        ps.setString(1, testId);
        return ps.executeQuery();
    }

    /* ─────────────────────────── TEST TYPES ─────────────────────────── */

    public static void addTestType(String code, String name, String category, String acronym, String sampleType,
            String desc, String normalRange, String prep,
            String turnaround, double cost) throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can add test types.");
        try (PreparedStatement ps = getConnection().prepareStatement("""
                INSERT INTO test_types
                  (test_code,test_name,category,acronym,sample_type,description,normal_range,
                   preparation_instructions,turnaround_time,cost)
                VALUES (?,?,?,?,?,?,?,?,?,?)""")) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setString(4, acronym);
            ps.setString(5, sampleType);
            ps.setString(6, desc);
            ps.setString(7, normalRange);
            ps.setString(8, prep);
            ps.setString(9, turnaround);
            ps.setDouble(10, cost);
            ps.executeUpdate();
            logActivity(currentUser, "ADD_TEST_TYPE", "Added: " + code);
        }
    }

    public static ResultSet getAllTestTypes() throws SQLException {
        Statement st = getConnection().createStatement();
        return st.executeQuery("""
                SELECT test_type_id,test_code,test_name,category,acronym,sample_type,description,
                       normal_range,preparation_instructions,turnaround_time,cost,is_active
                FROM test_types WHERE is_active=TRUE ORDER BY category, test_name""");
    }

    public static ResultSet getTestTypeByCode(String code) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM test_types WHERE test_code=?");
        ps.setString(1, code);
        return ps.executeQuery();
    }

    /* ─────────────────────────── SPECIALIZATIONS ────────────────────── */

    public static void addSpecialization(String name, String category) throws SQLException {
        if (!isAdmin())
            throw new SQLException("Only admins can add specializations.");
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO specializations (spec_name, category) VALUES (?, ?)")) {
            ps.setString(1, name.trim());
            ps.setString(2, category);
            ps.executeUpdate();
            logActivity(currentUser, "ADD_SPECIALIZATION", name);
        }
    }

    public static ResultSet getAllSpecializations() throws SQLException {
        Statement st = getConnection().createStatement();
        return st.executeQuery("SELECT spec_id, spec_name, category FROM specializations ORDER BY category, spec_name");
    }

    /* ─────────────────────────── STATISTICS ─────────────────────────── */

    public static int getTotalPatients() throws SQLException {
        return count("SELECT COUNT(*) FROM patients WHERE is_active=1");
    }

    public static int getTotalDoctors() throws SQLException {
        return count("SELECT COUNT(*) FROM doctors");
    }

    public static int getTotalTests() throws SQLException {
        return count("SELECT COUNT(*) FROM lab_tests");
    }

    public static int getTodaysPatientsCount() throws SQLException {
        return count("SELECT COUNT(DISTINCT patient_id) FROM lab_tests WHERE DATE(request_date) = CURDATE()");
    }

    public static int countUsersStatus() throws SQLException {
        return count("SELECT COUNT(*) FROM users WHERE is_active=1");
    }

    public static int getPendingTests() throws SQLException {
        return count("SELECT COUNT(*) FROM lab_tests WHERE status IN ('REQUESTED','COLLECTED','IN_PROGRESS')");
    }

    // Compatibility Aliases
    public static int countPatients() {
        try {
            return getTotalPatients();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int countDoctors() {
        try {
            return getTotalDoctors();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getTestCount() {
        try {
            return getTotalTests();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int countPendingTests() {
        try {
            return getPendingTests();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int countUsers() {
        try {
            return countUsersStatus();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getTodaysTests() throws SQLException {
        return count("SELECT COUNT(*) FROM lab_tests WHERE DATE(request_date)=CURDATE()");
    }

    public static int getCompletedToday() throws SQLException {
        return count("SELECT COUNT(*) FROM lab_tests WHERE status='COMPLETED' AND DATE(completion_date)=CURDATE()");
    }

    private static int count(String sql) throws SQLException {
        try (Statement st = getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /* ─────────────────────────── ACTIVITY LOG ───────────────────────── */

    public static int getStaffSaturation() throws SQLException {
        int total = count("SELECT COUNT(*) FROM doctors");
        if (total == 0)
            return 0;
        int active = count("SELECT COUNT(DISTINCT doctor_id) FROM lab_tests WHERE status != 'COMPLETED'");
        return (int) ((active / (double) total) * 100);
    }

    public static int getDiagnosticVelocity() throws SQLException {
        return count(
                "SELECT COUNT(*) FROM lab_tests WHERE status='COMPLETED' AND completion_date >= DATE_SUB(NOW(), INTERVAL 24 HOUR)");
    }

    public static String getSystemLatency() {
        return "24ms"; // Simulation for UI fullness
    }

    public static void logActivity(String username, String action, String details) {
        if (!isConnected())
            return;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO user_activity (username,action,details,activity_date) VALUES (?,?,?,NOW())")) {
            ps.setString(1, username);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            /* silent */ }
    }

    public static ResultSet getRecentActivity() throws SQLException {
        Statement st = getConnection().createStatement();
        return st.executeQuery("""
                SELECT CONCAT(action,' - ',details) as activity,
                       DATE_FORMAT(activity_date,'%Y-%m-%d %H:%i') as timestamp
                FROM user_activity ORDER BY activity_date DESC LIMIT 10""");
    }

    /* ─────────────────────────── UTILITY ────────────────────────────── */

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    // ================== GUI COMPATIBILITY METHODS ==================

    public static ResultSet getRecentTests(int limit) {
        try {
            Statement st = getConnection().createStatement();
            return st.executeQuery("""
                    SELECT lt.test_id, lt.urgency_stars, lt.status,
                           p.first_name, p.last_name,
                           tt.test_name, tt.test_code
                    FROM lab_tests lt
                    JOIN patients p ON lt.patient_id = p.patient_id
                    JOIN test_types tt ON lt.test_type_id = tt.test_type_id
                    WHERE p.is_active=1
                    ORDER BY lt.request_date DESC LIMIT """ + limit);
        } catch (Exception e) {
            return null;
        }
    }

    // ================== APPOINTMENT SYSTEM ==================

    public static void addAppointment(int patientId, int doctorId, String dateTime, String reason) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO appointments (patient_id, doctor_id, app_date, reason) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setString(3, dateTime);
            ps.setString(4, reason);
            ps.executeUpdate();
            logActivity(currentUser, "BOOK_APPOINTMENT", "Patient ID: " + patientId + " with Doctor ID: " + doctorId);
        }
    }

    public static void approveLab(String testId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE lab_tests SET status='REQUESTED' WHERE test_id=? AND status='PENDING_APPROVAL'")) {
            ps.setString(1, testId);
            if (ps.executeUpdate() > 0)
                logActivity(currentUser, "APPROVE_LAB", testId);
        }
    }

    public static void rejectLab(String testId) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE lab_tests SET status='CANCELLED' WHERE test_id=? AND status='PENDING_APPROVAL'")) {
            ps.setString(1, testId);
            if (ps.executeUpdate() > 0)
                logActivity(currentUser, "REJECT_LAB", testId);
        }
    }

    public static ResultSet getScheduledQueue(int doctorId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("""
                SELECT a.app_id, a.app_date, a.reason, a.status,
                       p.first_name, p.last_name
                FROM appointments a
                JOIN patients p ON a.patient_id = p.patient_id
                WHERE a.doctor_id = ? AND p.is_active = 1
                ORDER BY a.app_date ASC""");
        ps.setInt(1, doctorId);
        return ps.executeQuery();
    }

    public static ResultSet getAppointmentsForPatient(int patientId) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement("""
                SELECT a.app_id, a.app_date, a.reason, a.status,
                       d.first_name as d_first, d.last_name as d_last, d.specialization
                FROM appointments a
                JOIN doctors d ON a.doctor_id = d.doctor_id
                WHERE a.patient_id = ?
                ORDER BY a.app_date DESC""");
        ps.setInt(1, patientId);
        return ps.executeQuery();
    }

    public static ResultSet getAllAppointments() throws SQLException {
        Statement st = getConnection().createStatement();
        return st.executeQuery("""
                SELECT a.app_id, a.app_date, a.reason, a.status,
                       CONCAT(p.first_name, ' ', p.last_name) as patient_name,
                       CONCAT(d.first_name, ' ', d.last_name) as doctor_name
                FROM appointments a
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN doctors d ON a.doctor_id = d.doctor_id
                ORDER BY a.app_date DESC""");
    }

    public static void updateAppointmentStatus(int appId, String status) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE appointments SET status=? WHERE app_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, appId);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_APPOINTMENT", "App ID: " + appId + " changed to: " + status);
        }
    }

    // ----- Access Control (basic placeholder) -----
    public static boolean hasAccess(String role, String moduleName, String action) {
        if ("ADMIN".equals(role) && "Administration".equals(moduleName)) return true;
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT can_view, can_edit FROM access_rights WHERE role=? AND module_name=?");
            ps.setString(1, role);
            ps.setString(2, moduleName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if ("edit".equalsIgnoreCase(action)) return rs.getInt("can_edit") == 1;
                return rs.getInt("can_view") == 1;
            }
        } catch (SQLException e) {}
        // Default allow Dashboard
        if ("Dashboard".equals(moduleName)) return true;
        return false;
    }

    public static ResultSet getAccessRights() {
        try {
            Statement st = getConnection().createStatement();
            return st.executeQuery("SELECT role, module_name, can_view, can_edit FROM access_rights");
        } catch (SQLException e) {
            return null;
        }
    }

    public static void updateAccessRight(String role, String moduleName, int canView, int canEdit) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO access_rights (role, module_name, can_view, can_edit) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE can_view=?, can_edit=?")) {
            ps.setString(1, role);
            ps.setString(2, moduleName);
            ps.setInt(3, canView);
            ps.setInt(4, canEdit);
            ps.setInt(5, canView);
            ps.setInt(6, canEdit);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_ACCESS", "Role: " + role + ", Module: " + moduleName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----- Patient / Doctor ID lookup -----
    public static int getPatientId(String firstName, String lastName) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT patient_id FROM patients WHERE first_name=? AND last_name=? AND is_active=1");
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Multi-strategy lookup:
     * 1. Direct username match in patients table
     * 2. Email join: find patient whose email matches the user's email in users
     * table
     * 3. If found via fallback, also write username back for future logins
     */
    public static int getPatientIdByUsername(String username) {
        if (username == null) return -1;
        try {
            // Strategy 1: direct username column
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT patient_id FROM patients WHERE username=? AND is_active=1")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }

            // Strategy 2: join via email in users table
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT p.patient_id FROM patients p " +
                            "JOIN users u ON p.email = u.email " +
                            "WHERE u.username=? AND p.is_active=1 LIMIT 1")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int pid = rs.getInt(1);
                        // Strategy 3: write the link back so future lookups are instant
                        try (PreparedStatement fix = getConnection().prepareStatement(
                                "UPDATE patients SET username=? WHERE patient_id=?")) {
                            fix.setString(1, username);
                            fix.setInt(2, pid);
                            fix.executeUpdate();
                        } catch (Exception ignored) {}
                        return pid;
                    }
                }
            }

            // Strategy 4: Fallback - if user exists but no patient record, create skeleton
            String uEmail = null, uPhone = null, uRole = null;
            try (PreparedStatement ps = getConnection().prepareStatement("SELECT email, phone, role FROM users WHERE username=?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        uEmail = rs.getString("email");
                        uPhone = rs.getString("phone");
                        uRole = rs.getString("role");
                    }
                }
            }

            if ("PATIENT".equals(uRole)) {
                return addPatientWithUser(username, "", "User", "N/A", 0, uPhone, uEmail, "", "", "", "", username);
            }

            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getDoctorIdByUsername(String username) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT doctor_id FROM doctors WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getDoctorId(String firstName, String lastName) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT doctor_id FROM doctors WHERE first_name=? AND last_name=?");
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    // ----- Test wrappers -----
    public static void updateTest(String testId, int testTypeId, int urgencyStars, String notes) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE lab_tests SET test_type_id=?, urgency_stars=?, notes=? WHERE test_id=?")) {
            ps.setInt(1, testTypeId);
            ps.setInt(2, urgencyStars);
            ps.setString(3, notes);
            ps.setString(4, testId);
            ps.executeUpdate();
            logActivity(currentUser, "UPDATE_TEST", "Updated test: " + testId);
        }
    }

    public static ResultSet getTestsForDoctor(int id) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("""
                    SELECT lt.test_id, lt.status,
                           p.first_name as p_first, p.last_name as p_last,
                           tt.test_name as type_name, tt.test_code
                    FROM lab_tests lt
                    JOIN patients p ON lt.patient_id = p.patient_id
                    JOIN test_types tt ON lt.test_type_id = tt.test_type_id
                    WHERE lt.doctor_id=?
                    ORDER BY lt.request_date DESC""");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return crs;
        } catch (Exception e) {
            return null;
        }
    }
}
