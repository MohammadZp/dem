package org.example.transactionPrivacy;

import org.example.TransactionManager;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TransactionManager transactionManager = new TransactionManager();
        System.out.println("Enter transaction data:");
        String data = scanner.nextLine();
        transactionManager.saveTransaction(data);
        System.out.println("Saved. Now loading...");
        String loaded = transactionManager.loadTransaction();
        System.out.println("Loaded transaction: " + loaded);
    }
}
