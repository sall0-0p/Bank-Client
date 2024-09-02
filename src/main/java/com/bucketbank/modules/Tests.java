package com.bucketbank.modules;

import java.util.logging.Logger;

import com.bucketbank.App;

public class Tests {
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();
    
    public Tests() {
        // Create User

        try {
            // User gelkin = new User("gelkin1991");

            // Account personalAccount = gelkin.getPersonalAccount();
            // personalAccount.setBalance(500);
            // Account additionalAccount = new Account("gelkin1991", true);
            // additionalAccount.setBalance(100);

            // User lomen = new User ("lomen");
            // Account lomenPersonalAccount = lomen.getPersonalAccount();
            // lomenPersonalAccount.setBalance(1);

            // additionalAccount.reinstate();
            // lomen.reinstate();

            // gelkin.deleteUser();
            logger.info("Tests completed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
