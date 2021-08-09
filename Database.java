package banking;

import org.sqlite.SQLiteDataSource;

import java.nio.file.Path;
import java.sql.*;
import java.util.Scanner;

public class Database {

    static Path path;
    static int id;
    static SQLiteDataSource dataSource = new SQLiteDataSource();

    public static void createDatabase() {
        String url = "jdbc:sqlite:" + path.toString();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                    "id INTEGER," +
                    "number TEXT," +
                    "pin TEXT," +
                    "balance INTEGER DEFAULT 0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        id = updateId();
    }

    private static int updateId() {
        int id = 0;
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement();
             ResultSet cardsResult = statement.executeQuery("SELECT MAX (id) FROM card")) {
            id = cardsResult.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ++id;
    }

    public static void addAccount(Account account) {
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            statement.executeUpdate(String.format("INSERT INTO card VALUES " +
                    "(%d, '%s', '%s', %d)", id, account.getCardNumber(), account.getPin(), (int) account.getBalance()));
            id++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        showDatabase();
    }

    public static void showDatabase() {
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement();
             ResultSet cardsResult = statement.executeQuery("SELECT * FROM card")) {
            while (cardsResult.next()) {
                int id = cardsResult.getInt("id");
                String number = cardsResult.getString("number");
                String pin = cardsResult.getString("pin");
                int balance = cardsResult.getInt("balance");

                System.out.printf("Id: %d%n", id);
                System.out.printf("\tNumber: %s%n", number);
                System.out.printf("\tpin: %s%n", pin);
                System.out.printf("\tbalance: %d%n", balance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setPath(Path path) {
        Database.path = path;
    }

    public static void logIn() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your card number: ");
        String cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();

        String selectAccount = "SELECT pin FROM card WHERE number = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement selectAcc = con.prepareStatement(selectAccount)) {

            selectAcc.setString(1, cardNumber);
            ResultSet result = selectAcc.executeQuery();
            if (result.getString(1).equals(pin)) {
                System.out.println("You have successfully logged in!\n");
                con.close();
                Account.menu(cardNumber);
            } else {
                System.out.println("Wrong PIN!\n");
            }
        } catch (SQLException e) {
            System.out.println("Wrong card number!\n");
            e.printStackTrace();
        }
    }


    public static int getBalance(String cardNumber) {
        String selectAccount = "SELECT balance FROM card WHERE number = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement selectAcc = con.prepareStatement(selectAccount)) {

            selectAcc.setString(1, cardNumber);
            ResultSet resultSet = selectAcc.executeQuery();

            return resultSet.getInt(1);

        } catch (SQLException e) {
            System.out.println("Something went wrong getting the balance");
            e.printStackTrace();
        }
        return 666;
    }

    public static void addIncome(String cardNumber, int income) {

        String addIncome = "UPDATE card SET balance = balance + ? WHERE number = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement addInc = con.prepareStatement(addIncome)) {

            addInc.setInt(1, income);
            addInc.setString(2, cardNumber);
            addInc.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Something went wrong adding money");
            e.printStackTrace();
        }
    }

    public static void subtractIncome(String cardNumber, int income) {

        String addIncome = "UPDATE card SET balance = balance - ? WHERE number = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement addInc = con.prepareStatement(addIncome)) {

            addInc.setInt(1, income);
            addInc.setString(2, cardNumber);
            addInc.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Something went wrong subtracting money");
            e.printStackTrace();
        }
    }

    public static void closeAccount(String cardNumber) {
        String delAcc = "DELETE FROM card WHERE number = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement deleteAcc = con.prepareStatement(delAcc)) {

            deleteAcc.setString(1, cardNumber);
            deleteAcc.executeUpdate();
            System.out.println("\nThe account has been closed!\n");
            Main.menu();

        } catch (SQLException e) {
            System.out.println("Something went wrong deleting the account");
            e.printStackTrace();
        }
    }

    public static void transferMoney(String cardNumber) {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        Scanner scanner = new Scanner(System.in);
        String recAcc = scanner.nextLine();
        if (!Account.generateChecksum(recAcc.substring(0, recAcc.length() - 1)).equals(recAcc.substring(recAcc.length() - 1))) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
        } else {
            if (cardNumber.equals(recAcc)) {
                System.out.println("You can't transfer money to the same account!");
                return;
            }
            if (checkIfAccExists(recAcc)) {
                System.out.println("Enter how much money you want to transfer:");
                int moneyToTransfer = scanner.nextInt();
                if (getBalance(cardNumber) < moneyToTransfer) {
                    System.out.println("Not enough money!");
                } else {

                    subtractIncome(cardNumber, moneyToTransfer);
                    addIncome(recAcc, moneyToTransfer);
                    System.out.println("Success!");

                }
            } else {
                System.out.println("Such a card does not exist.");
            }
        }
    }

    private static boolean checkIfAccExists(String recAcc) {
        String selectAccount = "SELECT * FROM card WHERE number = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement selectAcc = con.prepareStatement(selectAccount)) {

            selectAcc.setString(1, recAcc);
            ResultSet resultSet = selectAcc.executeQuery();

            String number = resultSet.getString("number");
            if (number.equals(recAcc)) {
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Something went wrong checking if account exists");
            e.printStackTrace();
        }
        return false;
    }
}
