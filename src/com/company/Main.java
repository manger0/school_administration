package com.company;

import java.sql.*;
import java.util.Scanner;


// school administration example Mathias Angerer
public class Main {

    public static void main(String[] args) {

        Connection connection = null;
        boolean anotherLogin = true;
        while (anotherLogin) {
            try {
                String url = "jdbc:mysql://localhost:3306/school_administration?user=root";
                connection = DriverManager.getConnection(url);
                int[] login = login(connection);
                int role = login[0];
                int personId = login[1];
                anotherLogin = !(role == 0 && personId == 0);

                // using login role number to verify level of access
                switch (role) {

                    // login number 1 identifies student
                    case 1:
                        boolean staySignedInStudent = true;
                        while (staySignedInStudent) {
                            Scanner scanner = new Scanner(System.in);
                            System.out.println("type in (1) to print available courses");
                            System.out.println("type in (2) to sign up for course");
                            System.out.println("type in (3) to print signed in courses");
                            System.out.println("type in (0) to sign out ");
                            int action = scanner.nextInt();
                            switch (action) {
                                case 1:
                                    printAvailableCoursesStudent(connection);
                                    break;
                                case 2:
                                    signInCourseStudent(connection, personId);
                                    break;
                                case 3:
                                    printSignedUpCourses(connection, personId);
                                    break;
                                case 0:
                                    staySignedInStudent = false;
                                    break;
                            }
                        }
                        break;

                    // login number 2 identifies teacher
                    case 2:
                        boolean staySignedInTeacher = true;
                        while (staySignedInTeacher) {
                            Scanner scanner = new Scanner(System.in);
                            System.out.println("type in (1) to print your courses to teach");
                            System.out.println("type in (2) to print students of course");
                            System.out.println("type in (3) to set grade of student");
                            System.out.println("type in (0) to sign out ");
                            int action = scanner.nextInt();
                            switch (action) {
                                case 1:
                                    printCoursesTeacher(connection, personId);
                                    break;
                                case 2:
                                    printStudentsOfCourse(connection, personId);
                                    break;
                                case 3:
                                    setGradeOfStudent(connection, personId);
                                    break;
                                case 0:
                                    staySignedInTeacher = false;
                                    break;
                            }
                        }
                        break;

                    // login number 3 identifies administrator
                    case 3:
                        boolean staySignedInAdmin = true;
                        while (staySignedInAdmin) {
                            Scanner scanner = new Scanner(System.in);
                            System.out.println("type in (1) to print current courses");
                            System.out.println("type in (2) to add course");
                            System.out.println("type in (3) to add person");
                            System.out.println("type in (0) to sign out ");
                            int action = scanner.nextInt();
                            switch (action) {
                                case 1:
                                    printCoursesAdmin(connection);
                                    break;
                                case 2:
                                    addCourseAdmin(connection);
                                    break;
                                case 3:
                                    addPersonAdmin(connection);
                                    break;
                                case 0:
                                    staySignedInAdmin = false;
                                    break;
                            }
                        }
                        break;
                }
            } catch (SQLException e) {
                throw new Error("connection problem", e);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    public static void addPersonAdmin(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("persons first name:");
        String firstName = scanner.nextLine();
        System.out.println("persons last name:");
        String lastName = scanner.nextLine();
        System.out.println("persons user name");
        String userName = scanner.nextLine();
        System.out.println("persons password");
        String password = scanner.nextLine();
        System.out.println("persons role (1): student");
        System.out.println("persons role (2): teacher");
        System.out.println("persons role (3): administrator");
        int role = scanner.nextInt();
        final String SQL_INSERT = "INSERT INTO person (first_name, last_name, user_name, password, role)" +
                "Values (?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT)) {
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, userName);
            preparedStatement.setString(4, password);
            preparedStatement.setInt(5, role);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println("\n");
    }

    public static void setGradeOfStudent(Connection connection, int teacherId) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("type in (course id)");
        int courseId = scanner.nextInt();
        System.out.println("type in (student id)");
        int studentId = scanner.nextInt();
        if (isTeacherFromCourseAndStudent(connection, teacherId, courseId, studentId)) {
            System.out.println("type in (grade) of student: " + studentId + "   in course: " + courseId);
            int grade = scanner.nextInt();
            String update = "UPDATE student_list set grade = ? WHERE course_id = ? AND student_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(update)) {
                preparedStatement.setInt(1, grade);
                preparedStatement.setInt(2, courseId);
                preparedStatement.setInt(3, studentId);
                preparedStatement.executeUpdate();
                System.out.println("grade set to: " + grade + "  success!\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("setGradeOfStudent problem");
            }
        } else System.out.println("selected student or course wrong");
    }

    public static boolean isTeacherFromCourseAndStudent(Connection connection, int teacherId, int courseId, int studentId) {
        String query = "SELECT student_list.course_id, student_list.student_id, student_list.grade, courses.course_id, courses.teacher_id\n" +
                "From student_list\n" +
                "INNER JOIN courses ON courses.course_id = student_list.course_id\n" +
                "Where courses.teacher_id = '" + teacherId + "' AND student_list.student_id = '" + studentId +
                "' AND student_list.course_id = '" + courseId + "';";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("isTeacherFromCourseAndStudent problem");
        }
        return false;
    }

    public static void printStudentsOfCourse(Connection connection, int teacherId) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("type in (course id)");
        int courseId = scanner.nextInt();
        String query = "SELECT courses.course_id, courses.teacher_id, student_list.course_id, student_list.grade, " +
                " student_list.student_id AS student_id, person.first_name, person.last_name\n" +
                "From student_list\n" +
                "INNER JOIN courses ON student_list.course_id = courses.course_id\n" +
                "INNER JOIN person ON student_list.student_id = person.person_id\n" +
                "WHERE courses.teacher_id = '" + teacherId + "' AND student_list.course_id = '" + courseId + "' ;";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            System.out.println("students from course: " + courseId);
            while (resultSet.next()) {
                int studentId = resultSet.getInt("student_id");
                String studentFirstName = resultSet.getString("first_name");
                String studentLastName = resultSet.getString("last_name");
                int grade = resultSet.getInt("grade");
                if (grade != 0) {
                    System.out.println("student id: " + studentId + "   first name: " + studentFirstName +
                            "   last name: " + studentLastName + "   grade: " + grade);
                } else {
                    System.out.println("student id: " + studentId + "   first name: " + studentFirstName +
                            "   last name: " + studentLastName + "   grade: not set");
                }
            }
            System.out.println("\n");
        } catch (SQLException e) {
            System.out.println("printStudentsOfCourse problem");
        }
    }

    public static void printCoursesTeacher(Connection connection, int teacherId) {
        String query = "select * from courses WHERE `teacher_id` = " + teacherId;
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            System.out.println("courses to teach: ");
            while (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                String courseName = resultSet.getString("course_name");
                int maxAmountSeats = resultSet.getInt("max_amount_seats");
                System.out.println("course id: " + courseId + "   course name: " + courseName + "   max amount seats: " +
                        maxAmountSeats);
            }
        } catch (SQLException e) {
            System.out.println("printCoursesTeacher problem");
        }
        System.out.println("\n");
    }

    public static void printSignedUpCourses(Connection connection, int studentId) {
        String query = "SELECT student_list.course_id, student_list.student_id," +
                "student_list.grade, courses.course_name\n" +
                "FROM student_list\n" +
                "INNER JOIN courses ON student_list.course_id = courses.course_id\n" +
                "WHERE `student_id` = '" + studentId + "';";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            while (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                String courseName = resultSet.getString("course_name");
                int grade = resultSet.getInt("grade");
                if (grade != 0) {
                    System.out.println("course id: " + courseId + "  course name: " + courseName + "  grade: " + grade);
                } else {
                    System.out.println("course id: " + courseId + "  course name: " + courseName);
                }
            }
        } catch (SQLException e) {
            System.out.println("printSignedUpCourses problem");
        }
        System.out.println("\n");
    }

    public static void signInCourseStudent(Connection connection, int studentId) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("type in course number:");
        int courseId = scanner.nextInt();
        boolean seatsAvailable = isSeatAvailable(connection, courseId);
        boolean notAlreadySignedIn = isNotAlreadySignedUp(connection, courseId, studentId);
        if (seatsAvailable && notAlreadySignedIn) {
            final String SQL_INSERT = "INSERT INTO `student_list` (course_id, student_id)" +
                    "Values (?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT)) {
                preparedStatement.setInt(1, courseId);
                preparedStatement.setInt(2, studentId);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("signInCourseStudent problem");
            }
            System.out.println("sign up for course: " + courseId + " success!\n");
        }
    }

    public static boolean isNotAlreadySignedUp(Connection connection, int courseId, int studentId) {
        String query = "select * from student_list WHERE `course_id` = '" + courseId + "' AND" +
                " `student_id` = '" + studentId + "';";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            if (resultSet.next()) {
                System.out.println("you already signed up for course: " + courseId + "!\n");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("isAlreadyInCourse problem");
        }
        return true;
    }

    public static boolean isSeatAvailable(Connection connection, int courseId) {
        int seatsTaken = seatsTaken(connection, courseId);
        int maxAmountSeats = 0;
        String query = "select * from courses WHERE `course_id` = '" + courseId + "';";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            if (resultSet.next()) maxAmountSeats = resultSet.getInt("max_amount_seats");
        } catch (SQLException e) {
            System.out.println("isCourseFullyBooked problem");
        }
        return (maxAmountSeats > seatsTaken);
    }

    public static void printAvailableCoursesStudent(Connection connection) {
        String query = "select * from courses";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            System.out.println("available courses: ");
            while (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                String courseName = resultSet.getString("course_name");
                int seatsTaken = seatsTaken(connection, courseId);
                int maxAmountSeats = resultSet.getInt("max_amount_seats");
                int teacherId = resultSet.getInt("teacher_id");
                System.out.println("course id: " + courseId + "   course name: " + courseName + "   taken seats: " +
                        seatsTaken + "/" + maxAmountSeats + "   teacher id: " + teacherId);
            }
        } catch (SQLException e) {
            System.out.println("printAvailableCoursesStudent problem");
        }
        System.out.println("\n");
    }

    public static int seatsTaken(Connection connection, int courseId) {
        int seatsTaken = 0;
        String query = "select count(*) AS seats_taken from student_list WHERE `course_id` = '" + courseId + "';";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            if (resultSet.next()) seatsTaken = resultSet.getInt("seats_taken");
        } catch (SQLException e) {
            System.out.println("seatsTaken");
        }
        return seatsTaken;
    }

    public static void addCourseAdmin(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("type in (course name) to add new course or type in (end)");
        String action = scanner.nextLine();
        if (!action.equalsIgnoreCase("end")) {
            System.out.println("max amount of seats:");
            int maxAmountSeats = scanner.nextInt();
            int teacherId = chooseTeacherAdmin(connection);
            final String SQL_INSERT = "INSERT INTO courses (course_name, max_amount_seats, teacher_id)" +
                    "Values (?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT)) {
                preparedStatement.setString(1, action);
                preparedStatement.setInt(2, maxAmountSeats);
                preparedStatement.setInt(3, teacherId);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                System.out.println("addCourseAdmin problem");
            }
        }
    }

    public static int chooseTeacherAdmin(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        String query = "select * from person WHERE `role` = '2'";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            System.out.println("available teachers: ");
            while (resultSet.next()) {
                int id = resultSet.getInt("person_id");
                String name = resultSet.getString("first_name");
                System.out.println("teacher id: " + id + "   teacher name: " + name);
            }
        } catch (SQLException e) {
            System.out.println("chooseTeacherAdmin problem");
        }
        System.out.println("type in teacher id:");
        return scanner.nextInt();
    }

    public static void printCoursesAdmin(Connection connection) {
        String query = "select * from courses";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            System.out.println("current courses: ");
            while (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                String courseName = resultSet.getString("course_name");
                int maxAmountSeats = resultSet.getInt("max_amount_seats");
                int teacherId = resultSet.getInt("teacher_id");
                System.out.println("course id: " + courseId + "   course name: " + courseName + "   max amount seats: " +
                        maxAmountSeats + "   teacher id: " + teacherId);
            }
        } catch (SQLException e) {
            System.out.println("printCourses problem");
        }
        System.out.println("\n");
    }

    public static int[] login(Connection connection) {
        System.out.println("\n");
        int[] result = new int[2];
        Scanner scanner = new Scanner(System.in);
        System.out.println("insert (user name) or (end)");
        String userName = scanner.nextLine();
        if (userName.equalsIgnoreCase("end")) {
            return result;
        }
        System.out.println("insert (password)");
        String password = (scanner.nextLine());
        String queryLogin = "SELECT * from person";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(queryLogin);
            while (resultSet.next()) {
                if (userName.equalsIgnoreCase(resultSet.getString("user_name")) &&
                        password.equalsIgnoreCase(resultSet.getString("password"))) {
                    result[0] = resultSet.getInt("role");
                    result[1] = resultSet.getInt("person_id");
                }
            }
            if (result[0] != 0) {
                System.out.println("login as successful\n");
            } else System.out.println("(user name) or (password) wrong");
        } catch (SQLException e) {
            System.out.println("login problem");
        }
        return result;
    }
}
