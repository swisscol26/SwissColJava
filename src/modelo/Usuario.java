package modelo;

import java.sql.Timestamp;

/**
 * Representa un usuario registrado en Swisscol.
 */
public class Usuario {

    private int userId;
    private String userName;
    private String email;
    private String passwordHash;
    private String userRole;
    private Timestamp createdAt;

    public Usuario() {
    }

    public Usuario(
            int userId,
            String userName,
            String email,
            String passwordHash,
            String userRole,
            Timestamp createdAt) {

        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}