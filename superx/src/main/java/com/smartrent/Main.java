package com.smartrent;

import com.smartrent.Controller.dataservice;
import com.smartrent.View.LandingPage;
//import com.smartrent.View.Tenant.TenantDashboard;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        
        dataservice.initializefirebase();

         Application.launch(LandingPage.class,args);
    }
}
