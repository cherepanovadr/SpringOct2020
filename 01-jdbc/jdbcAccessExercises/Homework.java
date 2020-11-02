package jdbcAccessExercises;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class Homework {
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    private static final String MINIONS_TABLE_NAME = "minions_db";
    private Connection connection;
    private BufferedReader reader;

    public Homework() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void setConnection(String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        connection = DriverManager
                .getConnection(CONNECTION_STRING + MINIONS_TABLE_NAME, properties);
    }

    public void getVillainsNamesEx2() throws SQLException {
        String query = "SELECT v.name, count(mv.minion_id) count\n" +
                "from villains v join minions_villains mv on v.id = mv.villain_id\n" +
                "group by v.id\n" +
                "having count > 15\n" +
                "order by count desc;";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            System.out.printf("%s %d%n", resultSet.getString(1),
                    resultSet.getInt(2));
        }
    }

    public void getMinionNamesEx3() throws IOException, SQLException {
        reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter villain id:");
        int idNumber = Integer.parseInt(reader.readLine());
        String villainName = getEntityNameById(idNumber, "villains");
        if (villainName == null) {
            System.out.printf("No villain with ID %d exists in the database.", idNumber);
        } else {
            System.out.printf("Villain: %s%n", villainName);
            String query = "Select m.name, m.age from minions m\n" +
                    "join minions_villains mv on m.id = mv.minion_id\n" +
                    "where mv.villain_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, idNumber);
            ResultSet resultSet = statement.executeQuery();
            int counter = 1;
            while (resultSet.next()) {
                System.out.printf("%d. %s %d%n",
                        counter++,
                        resultSet.getString("name"),
                        resultSet.getInt("age"));
            }
        }
    }

    private String getEntityNameById(int idNumber, String tableName) throws SQLException {
        String query = String.format("Select name from %s where id = ?", tableName);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, idNumber);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? resultSet.getString("name") : null;
    }

    public void addMinionEx4() throws IOException, SQLException {
        System.out.println("Enter minions info: name, age, town name:");
        String[] minionsInfo = reader.readLine().split("\\s+");
        String minionName = minionsInfo[0];
        int age = Integer.parseInt(minionsInfo[1]);
        String townName = minionsInfo[2];
        int townId = getEntityNameByName(townName, "towns");
        int minionId = getEntityNameByName(minionName, "minions");
        if (minionId < 0) {
            if (townId < 0) {
                insertEntityInTowns(townName);
                System.out.printf("Town %s was added to the database.%n", townName);
                townId = getEntityNameByName(townName, "towns");
            }
            insertEntityInMinions(minionName, age, townId);
            minionId = getEntityNameByName(minionName, "minions");
        }
        System.out.println("Enter villain name:");
        String villainName = reader.readLine();
        int villainDBSearch = getEntityNameByName(villainName, "villains");
        if (villainDBSearch < 0) {
            insertEntityInVillains(villainName);
            System.out.printf("Villain %s was added to the database.%n", villainName);
            villainDBSearch = getEntityNameByName(villainName, "villains");
        }
        insertEntityInMinionsVillains(minionId, villainDBSearch);
        System.out.printf("Successfully added %s to be minion of %s", minionName, villainName);

    }

    private void insertEntityInMinionsVillains(int minionId, int villainDBSearch) throws SQLException {
        String query = "INSERT INTO minions_villains (minion_id, villain_id) value (?,?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, minionId);
        statement.setInt(2, villainDBSearch);
        statement.execute();

    }

    private void insertEntityInMinions(String minionName, int age, int town_id) throws SQLException {
        String query = "INSERT INTO minions (`name`, age, town_id) value (?,?,?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, minionName);
        statement.setInt(2, age);
        statement.setInt(3, town_id);
        statement.execute();
    }

    private void insertEntityInVillains(String villainName) throws SQLException {
        String query = "INSERT INTO villains (name, evilness_factor) value (?,'evil')";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, villainName);
        statement.execute();
    }

    private void insertEntityInTowns(String townName) throws SQLException {
        String query = "INSERT INTO towns(name) value (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, townName);
        statement.execute();
    }

    private int getEntityNameByName(String entityName, String tableName) throws SQLException {
        String query = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, entityName);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public void changeTownNameCasingEx5() throws IOException, SQLException {

        String countryName = reader.readLine();
        String query = "Update towns\n" +
                "SET `name` = upper(`name`)\n" +
                "where country = ?";

        PreparedStatement statement1 = connection.prepareStatement(query);
        statement1.setString(1, countryName);
        statement1.execute();
        int townsAffected = statement1.executeUpdate();
        if (townsAffected > 0) {
            System.out.printf("%d town names were affected.%n",
                    townsAffected);
            getAffectedCountries(countryName);
        } else {
            System.out.println("No town names were affected.");
        }
    }

    private void getAffectedCountries(String countryName) throws SQLException {
        String queryCount = "SELECT `name` FROM towns\n" +
                "WHERE country = ?";
        PreparedStatement statement2 = connection.prepareStatement(queryCount);
        statement2.setString(1, countryName);
        ResultSet resultSet = statement2.executeQuery();
        ArrayList<String> towns = new ArrayList<>();
        while (resultSet.next()) {
            towns.add(resultSet.getString(1));
        }
        System.out.println("[" + String.join(", ", towns) + "]");
    }

    public void removeVillainEx6() throws SQLException, IOException {
        System.out.println("Enter villain id:");

        int id = Integer.parseInt(reader.readLine());
        String villainName = getEntityNameById(id, "villains");
        if (villainName == null) {
            System.out.println("No such villain was found");
        } else {
            int releasedMinions = deleteRelationWithMinions(id);
            System.out.printf("%s was deleted%n", villainName);
            System.out.printf("%d minions released", releasedMinions);
        }
    }

    private int deleteRelationWithMinions(int villainId) throws SQLException {
        String query = "DELETE FROM `minions_villains` WHERE `villain_id` = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, villainId);
        return statement.executeUpdate();
    }

    public void printAllMinionNamesEx7() throws SQLException {
        String query = "SELECT name from minions";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> listReversed = new ArrayList<>();
        while (resultSet.next()) {
            String nameToAdd = resultSet.getString(1);
            list.add(nameToAdd);
            listReversed.add(nameToAdd);
        }
        Collections.reverse(listReversed);
        for (int i = 0; i < list.size() / 2; i++) {
            System.out.println(list.get(i));
            System.out.println(listReversed.get(i));
        }


    }

    public void increaseMinionAgeEx8() throws IOException, SQLException {
        String[] tokens = reader.readLine().split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            int currentId = Integer.parseInt(tokens[i]);
            incrementAgeby1(currentId);
            printMinions();
        }
    }

    private void printMinions() throws SQLException {
        String query3 = "Select name, age from minions";
        PreparedStatement statement3 = connection.prepareStatement(query3);
        ResultSet resultSet = statement3.executeQuery();
        while (resultSet.next()) {
            System.out.printf("%s %d%n", resultSet.getString(1), resultSet.getInt(2));
        }
    }

    private void incrementAgeby1(int currentId) throws SQLException {
        String query = "Update  minions\n" +
                "Set age = age+1\n" +
                "where id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, currentId);
        statement.executeUpdate();
        String query2 = "Update  minions\n" +
                "Set `name` = lower(`name`)\n" +
                "where id = ?";
        PreparedStatement statement2 = connection.prepareStatement(query2);
        statement2.setInt(1, currentId);
        statement2.executeUpdate();

    }

    public void increaseAgeStoredProcedureEx8() throws IOException, SQLException {
        System.out.println("Enter minion ID: ");
        int minion_id = Integer.parseInt(reader.readLine());
        String minionName = getEntityNameById(minion_id, "minions");
        if (minionName != null) {
            String query = "CALL usp_get_older(?)";
            CallableStatement callableStatement = connection.prepareCall(query);
            callableStatement.setInt(1, minion_id);
            callableStatement.execute();
        }
        printMinions();
    }
}
