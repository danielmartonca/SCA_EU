package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Data
public class Examination {
    private Student student;
    private Course course;
    private Double grade;
    private Date date;
    private boolean hasPaidTax;

    public Examination(Student student, Course course, Double grade) {
        this.student = student;
        this.course = course;
        this.grade = grade;
        this.date = new Date();
        this.hasPaidTax = false;
    }
}
