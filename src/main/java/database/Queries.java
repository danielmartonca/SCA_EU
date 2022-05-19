package database;

import model.Course;
import model.Examination;
import model.Student;
import utilities.LoggingUtilities;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Queries {
    private static final String CONNECTION_STRING = "jdbc:sqlserver://localhost;encrypt=true;trustServerCertificate=true;database=StudentCardDb;";
    private static final String username = "sa";
    private static final String password = "LCy!@e^jr#G{<9<B";

    public static Course getCourseByName(String name) {
        String query = "SELECT * FROM Courses c WHERE c.Name=?";
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING, username, password); PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) return null;
            resultSet.next();
            return new Course(resultSet.getInt(1), UUID.fromString(resultSet.getString(2)), resultSet.getString(3));
        } catch (Exception e) {
            LoggingUtilities.printError("[SQL] Exception occurred while extracting course by name from db.");
        }
        return null;
    }

    public static List<Course> getAllCourses() {
        List<Course> courses = new LinkedList<>();
        String query = "SELECT * FROM Courses";
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING, username, password); Statement statement = con.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                Course course = new Course(resultSet.getInt(1), UUID.fromString(resultSet.getString(2)), resultSet.getString(3));
                courses.add(course);
            }
        } catch (Exception e) {
            System.err.println("[SQL] Exception occurred while getting cases from the database: " + e.getMessage());
        }
        return courses;
    }

    public static List<Examination> getStudentGradesAtCourse(Student student, Course course) {
        List<Examination> gradesList = new LinkedList<>();
        String query = "SELECT * FROM Grades WHERE student_id=? AND  course_id=?";
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING, username, password); PreparedStatement statement = con.prepareStatement(query)) {
            statement.setInt(1, student.getId());
            statement.setInt(2, course.getId());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Examination grade = new Examination(student, course, resultSet.getInt(1), new java.util.Date(resultSet.getDate(2).getTime()), resultSet.getBoolean(3));
                gradesList.add(grade);
            }
        } catch (Exception e) {
            LoggingUtilities.printError("[SQL] Exception occurred at getStudentGradesAtCourse.");
            LoggingUtilities.printError("[SQL] Exception is: " + e.getMessage());
        }
        return gradesList;
    }

    public static Student findStudentById(int studentId) {
        String query = "SELECT 1 FROM Students s WHERE s.id=?";
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING, username, password); PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) return null;
            Student student = new Student();
            student.setId(studentId);
            return student;
        } catch (Exception e) {
            LoggingUtilities.printError("[SQL] Exception occurred at findStudentById.");
        }
        return null;
    }

    public static void insertGrade(Examination grade) {
        String query = "INSERT INTO Grades (grade, date, hasPaidTax, student_id, course_id) VALUES (?,?,?,?,?)";
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING, username, password); PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, grade.getGrade());

            preparedStatement.setDate(2, new java.sql.Date(grade.getDate().getTime()));

            preparedStatement.setString(3, grade.isHasPaidTax() ? "true" : "false");

            int studentId = grade.getStudent().getId();
            preparedStatement.setInt(4, studentId);

            int courseId = grade.getCourse().getId();
            preparedStatement.setInt(5, courseId);
            preparedStatement.execute();
        } catch (Exception e) {
            LoggingUtilities.printError("[SQL] Exception occurred while inserting new grade in database.");
            LoggingUtilities.printError("[SQL] Exception is: " + e.getMessage());
        }
    }


    public static void setHasPaidTax(boolean hasPaidTax, Course course) {
        String query = "UPDATE Grades SET hasPaidTax=? WHERE course_id=?";
        try (Connection con = DriverManager.getConnection(CONNECTION_STRING, username, password); PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, hasPaidTax ? "true" : "false");
            preparedStatement.setInt(2, course.getId());
            preparedStatement.execute();
        } catch (Exception e) {
            LoggingUtilities.printError("[SQL] Exception occurred at setHasPaidTax.");
            LoggingUtilities.printError("[SQL] Exception is: " + e.getMessage());
        }
    }
}
