package com.sx.backend.dto;

import java.util.List;

public class AnalysisReportDTO {
    private String courseId;
    private String courseName;
    private int totalStudents;
    private double classAverage;
    private double classAverageRate;
    private List<StudentPerformance> performers;

    public static class StudentPerformance {
        private String studentNumber;
        private String studentName;
        private double gradeRate;
        private int rank;

        public StudentPerformance(String studentId, String studentNumber, double gradeRate, int rank) {
            this.studentNumber = studentId;
            this.studentName = studentNumber;
            this.gradeRate = gradeRate;
            this.rank = rank;
        }

        public StudentPerformance() {
        }

        public String getStudentNumber() {
            return studentNumber;
        }

        public void setStudentNumber(String studentNumber) {
            this.studentNumber = studentNumber;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public double getGradeRate() {
            return gradeRate;
        }

        public void setGradeRate(double gradeRate) {
            this.gradeRate = gradeRate;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    public AnalysisReportDTO(String courseId, String courseName, int totalStudents, double classAverage, List<StudentPerformance> performers) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.totalStudents = totalStudents;
        this.classAverage = classAverage;
        this.performers = performers;
    }

    public AnalysisReportDTO() {
    }

    public double getClassAverageRate() {
        return classAverageRate;
    }

    public void setClassAverageRate(double classAverageRate) {
        this.classAverageRate = classAverageRate;
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

    public List<StudentPerformance> getPerformers() {
        return performers;
    }

    public void setPerformers(List<StudentPerformance> performers) {
        this.performers = performers;
    }
}
