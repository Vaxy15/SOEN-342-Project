package com.soen342;

import com.soen342.console.Console;
import com.soen342.persistence.DBManager;

/**
 * Entry point for the Personal Task Management System.S
 * SOEN 342 - Winter 2026
 * 
 * RUN:
 * mvn clean package
 * java -jar target/TaskManagerApp-1.0-SNAPSHOT.jar
 * 
 */
public class Main {
    public static void main(String[] args) {
        try{
            DBManager.init();
        } catch (Exception e){
            System.out.println("Failed to initialize database.");
            e.printStackTrace();
            System.exit(1);
        }
        new Console().start();
        try{
            DBManager.close();
        } catch (Exception e){
            System.out.println("Failed to close database.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
