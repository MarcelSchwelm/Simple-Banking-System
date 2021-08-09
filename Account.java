package banking;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Account {
    private long cardNumber;
    private int pin;
    private double balance;

    public Account() {
        this.cardNumber = generateCardNumber();
        this.pin = generatePin();
        this.balance = 0.;
    }

    private int generatePin() {
        return ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private long generateCardNumber() {
        String bin = "400000";
        String acc = String.valueOf(generateAccNr());
        String binAndAcc = bin + acc;
        String checksum = generateChecksum(binAndAcc);

        return Long.parseLong(bin + acc + checksum);
    }

    public static String generateChecksum(String binAndAcc) {
        int sum = 0;
        for (int i = 1; i <= 15; i++) {
            String s = binAndAcc.substring(i - 1, i);
            int digit = Integer.parseInt(s);
            if (i % 2 != 0) {
                digit *= 2;
            }
            if (digit > 9) {
                digit -= 9;
            }
            sum += digit;
        }

        for (int i = 0; i <= 9; i++) {
            if ((sum + i) % 10 == 0) {
                return String.valueOf(i);
            }
        }
        return String.valueOf(666);
    }

    private int generateAccNr() {
        return ThreadLocalRandom.current().nextInt(100000000, 999999999);
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public int getPin() {
        return pin;
    }

    public double getBalance() {
        return balance;
    }

    public static void menu(String cardNumber) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("\nBalance: " + Database.getBalance(cardNumber) + "\n");
                    break;
                case 2:
                    System.out.println("Enter income:");
                    int income = scanner.nextInt();
                    Database.addIncome(cardNumber, income);
                    System.out.println("\nIncome was added!\n");
                    break;
                case 3:
                    Database.transferMoney(cardNumber);
                    break;
                case 4:
                    Database.closeAccount(cardNumber);
                    break;
                case 5:
                    System.out.println("\nYou have successfully logged out!\n");
                    Main.menu();
                    break;
                case 0:
                    Main.exit();
                    break;
                default:
                    System.out.println("\nCould not match the input: " + choice + "\n");
                    break;
            }
        }
    }
}
