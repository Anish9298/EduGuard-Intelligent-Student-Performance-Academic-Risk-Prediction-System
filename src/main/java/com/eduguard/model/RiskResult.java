package com.eduguard.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "risk_results")
public class RiskResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    private double riskScore;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date evaluatedAt = new Date();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Date getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Date evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}