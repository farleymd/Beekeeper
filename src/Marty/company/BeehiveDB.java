package Marty.company;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Date;

/**
 * Created by marty.farley on 4/12/2015.
 */
public class BeehiveDB {
    private static String protocol = "jdbc:derby:";
    private static String dbName = "beehiveDB";

    private static final String USER = "username";
    private static final String PASS = "password";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Statement statement = null;
        Connection conn = null;
        ResultSet resultSet = null;

        PreparedStatement psInsert = null;
        LinkedList<Statement> allStatements = new LinkedList<Statement>();

        try {

            conn = DriverManager.getConnection(protocol + dbName + ";create=true", USER, PASS);
            statement = conn.createStatement();
            allStatements.add(statement);

            System.out.println("Beehive Database Program");

            //Create a table in the database. Stores today's date, and the min and max temperatures recorded.

            String createTableSQL = "CREATE TABLE Beehive (BeehiveID int NOT NULL GENERATED ALWAYS " +
                    "AS IDENTITY (START WITH 1, INCREMENT BY 1), Location varchar(60), HiveNum int" +
                    "Date date, Weight double)";
            String deleteTableSQL = "DROP TABLE temp";
            try {
                statement.executeUpdate(createTableSQL);
                System.out.println("Created table");
            } catch (SQLException sqle) {
                //Seems the table already exists. Delete it and recreate it
                try {
                    if (sqle.getSQLState().startsWith("X0")) {    //Error code for table already existing start with XO
                        System.out.println("Temp table appears to exist already, delete and recreate");
                        statement.executeUpdate(deleteTableSQL);
                        statement.executeUpdate(createTableSQL);
                    } else {
                        //Something else went wrong. If we can't create the table, no point attempting
                        //to run the rest of the code. Throw the exception again to be handled at the end of the program.
                        throw sqle;
                    }
                } catch (SQLException se){
                    se.printStackTrace();
                }
            }
        } catch (SQLException se){
            se.printStackTrace();
        }

        String prepStatInsert = "INSERT INTO Beehive(location, hivenum, date,weight) VALUES ( ?,?,?,?)";
        try{
            psInsert = conn.prepareStatement(prepStatInsert);
            allStatements.add(psInsert);

            psInsert.setString(1,"North");
            psInsert.setInt(2,1);
            psInsert.setDate(3,java.sql.Date.valueOf("2014-04-01"));
            psInsert.setDouble(4, 3.40);
            psInsert.executeUpdate();

            psInsert.setString(1,"South");
            psInsert.setInt(2,2);
            psInsert.setDate(3,java.sql.Date.valueOf("2014-04-01"));
            psInsert.setDouble(4,5.00);
            psInsert.executeUpdate();

            String fetchAllDataSQL = "SELECT * from Beehive";

            try {
                resultSet = statement.executeQuery(fetchAllDataSQL);
                while (resultSet.next()){
                    int beehiveID = resultSet.getInt("BeehiveID");
                    String locationString = resultSet.getString("Location");
                    int numString = resultSet.getInt("HiveNum");
                    Date dateString = resultSet.getDate("Date");
                    double weightString = resultSet.getDouble("Weight");

                    System.out.println("BeehiveID:" + beehiveID + " Location: " + locationString +
                            "Hive Number: " + numString +  " Date Collected: " + dateString +
                            " Weight of Honey: " + weightString);
                }
            } catch (SQLException se){
                se.printStackTrace();
            }

        } catch (SQLException se){
            se.printStackTrace();
        }

        System.out.println("Would you like to add a new record? Y/N");
        String userAddAnswer = scanner.nextLine();

        if (userAddAnswer.equalsIgnoreCase("Y")){
          addRecord(scanner,statement);
        }

        System.out.println("Would you like to update a record? Y/N");
        String userUpdateAnswer = scanner.nextLine();

        if (userUpdateAnswer.equalsIgnoreCase("Y")){
            updateRecord(scanner,statement,conn,resultSet);
        }


    }

    public static void addRecord(Scanner scanner, Statement statement){
        System.out.println("What was the location of the beehive?");
        String location = scanner.nextLine();
        System.out.println("How much honey (in lbs) was collected at " + location + "?");
        String weight = scanner.nextLine();
        double honeyWeight = Double.parseDouble(weight);
        System.out.println("On what date did you collect the honey?");
        String date = scanner.nextLine();
        Date dateCollected = new Date();

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            dateCollected = format.parse(date);
        }catch (java.text.ParseException pe){
            pe.printStackTrace();
        }

        String insertSQLStatement = "INSERT INTO Beehive VALUES('" + location
                + "', " + dateCollected + " " + honeyWeight + ")";
        System.out.println("Record successfully inserted.");
        try {
            statement.executeUpdate(insertSQLStatement);
        } catch(SQLException se) {
            se.printStackTrace();
        }
    }

    public static void updateRecord(Scanner scanner, Statement statement, Connection conn,
                                    ResultSet resultSet){
        boolean locationFound = true;

        while (locationFound == true){
            System.out.println("What location would you like to update?");
            String updateLocation = scanner.nextLine();

            if (updateLocation.equals("")){
                break;
            }

            try {
                String searchQuery = "select * from Beehive where location like ?";
                PreparedStatement recordSearch = conn.prepareStatement(searchQuery);
                recordSearch.setString(1,updateLocation);
                resultSet = recordSearch.executeQuery();

                if (resultSet.next()){
                    locationFound = true;
                    String location = resultSet.getString("Location");
                    System.out.print("What would you like to update, date or weight for location " + location + "?");
                    String userChoice = scanner.nextLine();

                    if (userChoice.equalsIgnoreCase("date")){
                        Date dateUpdate = new Date();
                        try {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            System.out.println("On what date was the honey collected?");
                            String userDate = scanner.nextLine();
                            dateUpdate = format.parse(userDate);
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }

                        String updateDateSQL = "UPDATE Beehive SET date = " + dateUpdate + " where location"
                                + " = '" + location + "'";
                        statement.executeUpdate(updateDateSQL);
                        System.out.println("Record successfully updated");
                        resultSet.close();
                        break;
                    } else if (userChoice.equalsIgnoreCase("weight")){
                        System.out.print("What is the new honey weight?");
                        String honeyWeight = scanner.nextLine();
                        double weightUpdate = Double.parseDouble(honeyWeight);

                        String updateWeightSQL = "UPDATE Beehive SET weight = " + weightUpdate + " where location"
                                + " = '" + location + "'";
                        statement.executeUpdate(updateWeightSQL);
                        System.out.println("Record successfully updated");
                        resultSet.close();
                        break;
                    }

                } else {
                    locationFound = false;
                }

                if (locationFound == false){
                    System.out.println("Sorry, that location could not be found in the database.");
                    resultSet.close();
                }
            } catch (SQLException se){
                se.printStackTrace();
            }

        }

        //Display total honey from all hives combined for any one year
        //Display total honey from one particular hive for all years
        //Display best year and total honey for one particular hive
        //Which hives produced the most honey last year? which hives produced less honey than previous years?
        //Display total honey in ranked over (date and honey)
        //which hive produces most honey overall, which produces least

        try {
            if (statement != null){
                statement.close();
            }
        } catch (SQLException se){
            se.printStackTrace();
        }

        String deletedbSQL = "DROP TABLE Beehive";
        try {
            statement.executeUpdate(deletedbSQL);
        } catch (SQLException se){
            se.printStackTrace();
        }

        try {
            if (conn != null){
                conn.close();
            }
        } catch (SQLException se){
            se.printStackTrace();
        }
    }


}
