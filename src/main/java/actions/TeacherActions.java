package actions;

import applet.Applet;
import database.Queries;
import model.Course;
import model.Grade;
import model.Student;
import utilities.UserType;
import utilities.LoggingUtilities;
import utilities.TextColor;

import java.util.List;
import java.util.Scanner;

public class TeacherActions {
    static Scanner scanner = new Scanner(System.in);

    private static List<Course> availableCourses;
    private static Course course;
    private static Student student;
    private static List<Grade> studentGradesAtCourse;
    private static double newGrade;

    private static void printAvailableCourses() {
        availableCourses = Queries.getAllCourses();
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Available courses: ");
        for (var courseName : availableCourses)
            System.out.print(LoggingUtilities.colorString(courseName.getName(), TextColor.BLUE) + ' ');
        System.out.println('\n');
    }

    private static void insertGradeInDatabase(double gradeValue) {
        Grade grade = new Grade(student, course, gradeValue);
        Queries.insertGrade(grade);
        studentGradesAtCourse.add(grade);
        if (gradeValue == 11.0) {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Setting hasPaidTax to '" + false + "' for course '" + course.getName() + "' in database.");
            Queries.setHasPaidTax(false, course);
        }
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Inserted new grade '" + gradeValue + "' for course '" + course.getName() + "' in database.");
    }

    public static void chooseCourse() {
        printAvailableCourses();

        boolean notOk = true;
        String input;
        do {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Choose one course: ");
            input = scanner.nextLine().trim();
            for (var course : availableCourses)
                if (course.getName().equals(input)) {
                    notOk = false;
                    break;
                }
            if (notOk)
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Please choose a correct course from the list.\n");
        } while (notOk);

        course = Queries.getCourseByName(input);
        if (course == null) {
            LoggingUtilities.printError("COULDN'T FIND COURSE BY NAME IN DB.");
            System.exit(-1);
        }
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Course chosen is " + course + "\n");
    }

    public static void inputGrade() {
        String input;
        boolean isOk;
        do {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Insert student grade: ");
            input = scanner.nextLine().trim();
            try {
                newGrade = Double.parseDouble(input);
                if (newGrade < 0 || newGrade > 10)
                    throw new NumberFormatException();
                isOk = true;
            } catch (NumberFormatException e) {
                isOk = false;
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Please insert a correct grade.\n");
            }
        } while (!isOk);

        int studentId = StudentActions.studentId;
        Applet.sendGrade(course, newGrade);

        student = Queries.findStudentById(studentId);
        if (student == null) {
            LoggingUtilities.printError("Student wasn't found in the database.");
            System.exit(-1);
        }

        LoggingUtilities.printUserMessage(UserType.TEACHER, "Sending to applet grade '" + newGrade + "' for course " + course + '\n');
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Applet returned studentId: '" + studentId + "'\n");
    }

    public static void insertStudentGradesAtCourse() {
        //get grades
        studentGradesAtCourse = Queries.getStudentGradesAtCourse(student, course);

        //if student has less than two grades
        if (studentGradesAtCourse.size() < 2) {
            insertGradeInDatabase(newGrade);
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has less than two grades.");
            return;
        }

        //if user has two grades < 5
        boolean hasPassed = true;
        int counter = 0;
        for (var grade : studentGradesAtCourse) {
            if (grade.getGrade() < 5.0)
                counter++;
            if (counter >= 2) {
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has more than two grades less than 5.");
                hasPassed = false;
                break;
            }
        }

        if (hasPassed) {
            LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has less than two grades less than 5.");
            insertGradeInDatabase(newGrade);
            return;
        }

        //if the student has paid the tax, stop
        for (var grade : studentGradesAtCourse)
            if (grade.isHasPaidTax()) {
                LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has paid the tax.");
                insertGradeInDatabase(newGrade);
                return;
            }


        //if the student has NOT paid the tax
        LoggingUtilities.printUserMessage(UserType.TEACHER, "Student has not payed the tax. Inserting error code 11 as grade in database and sending it to applet.");
        Applet.sendErrorCodeForTaxNotPaid(course, newGrade);
        insertGradeInDatabase(11.0);
    }
}
