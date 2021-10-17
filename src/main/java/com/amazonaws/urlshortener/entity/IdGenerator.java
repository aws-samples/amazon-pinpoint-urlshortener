// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

/*
package com.amazonaws.urlshortener.entity;

import org.hibernate.HibernateException;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class IdGenerator implements IdentifierGenerator {
    //You can give any name to sequence be sure that you know how to use it.
    private final String DEFAULT_SEQUENCE_NAME = "hibernate_sequence";


    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Serializable result = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        
        try {
            connection = session.connection();
            statement = connection.createStatement();
            try {
                
              //  * uncomment below line if you are using mysql and the sequence DOES NOT EXIST.
               // * As we all know MySql does not support sequence, instead there is AUTO INCREMENT
               // * if you are using other databases change SQL according to that
              //  * e.g. Oracle: "SELECT "+sequenceName+".NEXTVAL FROM DUAL"
              //  * PostgreSQL: "SELECT  NEXTVAL('+sequenceName+"')  
              //  * 
                statement.executeUpdate("UPDATE " + DEFAULT_SEQUENCE_NAME + " SET next_val=LAST_INSERT_ID(next_val+1)");
                System.out.println("mayur inside id generator before nextval");
                resultSet = statement.executeQuery("SELECT next_val FROM  " + DEFAULT_SEQUENCE_NAME);
                System.out.println("mayur inside id generator after nextval");
            } catch (Exception e) {

                System.out.println("In catch, cause : Table is not available.");
            
               // statement.execute("CREATE table " + DEFAULT_SEQUENCE_NAME + " (next_val INT NOT NULL)");
                //statement.executeUpdate("INSERT INTO " + DEFAULT_SEQUENCE_NAME + " VALUES(0)");
            
                //statement.executeUpdate("UPDATE " + DEFAULT_SEQUENCE_NAME + " SET next_val=LAST_INSERT_ID(next_val+1)");
              //  resultSet = statement.executeQuery("SELECT next_val FROM  " + DEFAULT_SEQUENCE_NAME);
                e.printStackTrace();
            }
            if (resultSet!= null && resultSet.next()) {
                System.out.println("mayur inside id generator resultset enntry");
                int nextValue = resultSet.getInt(1);
                System.out.println("mayur inside id generator resultset value="+nextValue);
               
                result = nextValue;
                System.out.println("mayur inside id generator result value="+result);
                Random r = new Random( System.nanoTime() );
                int randnumber = (1 + r.nextInt(2)) * 10000 + r.nextInt(10000); //this is 5 digit
                result =  Integer.valueOf(String.valueOf(randnumber) + String.valueOf(nextValue));
                //String suffix = String.format("%04d", nextValue);
                //result = prefix.concat(suffix);
                System.out.println("Custom generated sequence is : " + result);
            }
        } catch (SQLException e) {
            System.out.println("inside sql exception");
            e.printStackTrace();
        }
        return result;
    }
}
*/