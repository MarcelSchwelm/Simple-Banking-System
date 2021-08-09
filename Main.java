package banking;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Path path = Paths.get(Objects.requireNonNull(getFileName(args)));
        Database.setPath(path);
        Database.createDatabase();
        menu();
    }

    private static String getFileName(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-fileName".equals(args[i])) {
                if (args[i + 1] != null) {
                    return args[i + 1];
                } else {
                    throw new IllegalArgumentException("Please specify a filename; for example: -fileName db.s3db");
                }
            }
        }
        return null;
    }

    public static void menu() {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    Database.logIn();
                    break;
                case 0:
                    exit();
                    break;
                default:
                    System.out.println("Could not match the input: " + choice);
                    break;
            }
        }
    }

    public static void createAccount() {
        Account account = new Account();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(account.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(account.getPin());
        Database.addAccount(account);
    }

    public static void exit() {
        System.out.println("\nBye!\n");
        System.exit(0);
    }
}