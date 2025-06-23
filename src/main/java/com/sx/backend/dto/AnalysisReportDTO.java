package com.sx.backend.dto;

import java.util.List;

public class AnalysisReportDTO {
    private String courseId;
    private String courseName;
    private int totalStudents;
    private double classAverage;
    private List<StudentPerformance> topPerformers;
    private List<StudentPerformance> needImprovement;

    public static class StudentPerformance {
        private String studentId;
        private String studentName;
        private double averageGrade;
        private double completionRate;
        private int rank;

        public StudentPerformance(String studentId, String studentName, double averageGrade, double completionRate, int rank) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.averageGrade = averageGrade;
            this.completionRate = completionRate;
            this.rank = rank;
        }

        public StudentPerformance() {
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public double getAverageGrade() {
            return averageGrade;
        }

        public void setAverageGrade(double averageGrade) {
            this.averageGrade = averageGrade;
        }

        public double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    public AnalysisReportDTO(String courseId, String courseName, int totalStudents, double classAverage, List<StudentPerformance> topPerformers, List<StudentPerformance> needImprovement) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.totalStudents = totalStudents;
        this.classAverage = classAverage;
        this.topPerformers = topPerformers;
        this.needImprovement = needImprovement;
    }

    public AnalysisReportDTO() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public double getClassAverage() {
        return classAverage;
    }

    public void setClassAverage(double classAverage) {
        this.classAverage = classAverage;
    }

    public List<StudentPerformance> getTopPerformers() {
        return topPerformers;
    }

    public void setTopPerformers(List<StudentPerformance> topPerformers) {
        this.topPerformers = topPerformers;
    }

    public List<StudentPerformance> getNeedImprovement() {
        return needImprovement;
    }

    public void setNeedImprovement(List<StudentPerformance> needImprovement) {
        this.needImprovement = needImprovement;
    }
}
