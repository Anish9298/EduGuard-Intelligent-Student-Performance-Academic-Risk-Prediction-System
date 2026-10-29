package com.eduguard.ai;

import com.eduguard.model.RiskResult;
import com.eduguard.model.Student;

public class RiskPredictor {

    public RiskResult evaluate(Student student,
                               double attendancePct,
                               double avgMarksPct,
                               long failedSubjects) {

        double score = (100 - attendancePct) * 0.4
                + (100 - avgMarksPct) * 0.4
                + failedSubjects * 5;

        String level;
        if (score >= 40)      level = "HIGH";
        else if (score >= 20) level = "MEDIUM";
        else                  level = "LOW";

        StringBuilder reason = new StringBuilder();
        if (attendancePct < 75) reason.append("Low attendance. ");
        if (avgMarksPct < 50)   reason.append("Low average marks. ");
        if (failedSubjects > 0) reason.append(failedSubjects).append(" failed subject(s).");
        if (reason.length() == 0) reason.append("No risk factors detected.");

        RiskResult r = new RiskResult();
        r.setStudent(student);
        r.setRiskScore(Math.round(score * 100.0) / 100.0);
        r.setRiskLevel(level);
        r.setReason(reason.toString().trim());
        return r;
    }
}