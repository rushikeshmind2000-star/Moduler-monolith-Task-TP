package com.tech.emailvalidation.offerletter.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "offer_letters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @Column(name = "employee_name", nullable = false, length = 150)
    private String employeeName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "package_amount", nullable = false, length = 50)
    private String packageAmount;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "joining_date", length = 50)
    private String joiningDate;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "email_sent")
    private Boolean emailSent;

    @Column(name = "email_status", length = 500)
    private String emailStatus;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "pdf_data", columnDefinition = "LONGBLOB")
    private byte[] pdfData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
