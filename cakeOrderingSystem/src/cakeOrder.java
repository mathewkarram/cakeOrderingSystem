/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author mathe
 */
public class cakeOrder extends Application {
    private TextField nameText;
    private TextField numText;
    private ComboBox<String> cakeFlavour;
    private RadioButton small;
    private RadioButton medium;
    private RadioButton large;
    private CheckBox yes;
    private CheckBox no;
    private double cost =0;
    private String size;
    private TextField nameSearch;
    private TextArea searchResults;
    
    @Override
    public void start(Stage stage) throws SQLException {
       stage.setTitle("Cake Ordering System");
       Label title = new Label("Cake Order");
       Label customerName = new Label(" Name");
       Label customerName2 = new Label("Enter Name");
       Label customerNum = new Label("Phone Number");
       Label cakeType = new Label("Enter Cake Type");
       Label cakeSize = new Label("Enter Cake Size");
       Label freeDelivery = new Label("Free delivery: ");
       Label searchPreviousOrders = new Label("Search your previous orders:");
       Button search = new Button("Search");
       search.setOnAction(e -> selectFromDatabase());
       Button quit = new Button("Quit");
       quit.setOnAction(e -> Platform.exit());
       Button save = new Button("Save");
       save.setOnAction(e-> saveToDatabase());
       
       
        numText = new TextField();
        nameText = new TextField();
        nameSearch = new TextField();
        searchResults = new TextArea();
        cakeFlavour = new ComboBox<>();
        cakeFlavour.getItems().addAll("Apple", "Carrot", "Cheesecake", "Chocolate", "Coffee", "Opera", "Tiramisu");
        small = new RadioButton("Small");
        small.setOnAction( e -> {
            cost += 5;
            size = "Small";
        });
        medium = new RadioButton("Medium");
        medium.setOnAction( e -> {
            cost += 10;
            size = "Medium";
        });
        large = new RadioButton("Large");
        large.setOnAction( e -> {
            cost += 15;
            size = "Large";
        });
        yes = new CheckBox("Yes");
        no = new CheckBox("No");
        no.setOnAction(e -> {
            cost += 2.5;
        });
       GridPane grid = new GridPane();
       grid.setVgap(10);
       grid.setHgap(10);
       grid.setPadding(new Insets(10));
       
       grid.add(title, 0, 0);
       grid.add(customerName, 0, 1);
       grid.add(nameText, 1, 1);
       grid.add(customerNum, 0, 2);
       grid.add(numText, 1, 2);
       grid.add(cakeType, 0, 3);
       grid.add(cakeFlavour, 1, 3);
       grid.add(cakeSize, 0, 4);
       grid.add(small, 1, 4);
       grid.add(medium, 2, 4);
       grid.add(large, 3, 4);
       grid.add(freeDelivery, 0, 5);
       grid.add(yes, 1, 5);
       grid.add(no, 2, 5);
       grid.add(quit,0,6);
       grid.add(save, 1, 6);
       grid.add(searchPreviousOrders, 0, 7);
       grid.add(customerName2, 0, 8);
       grid.add(nameSearch, 1, 8);
       grid.add(search, 0, 9);
       grid.add(searchResults, 0, 10);
       
       
       
       Scene scene = new Scene(grid, 350, 350);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public void saveToDatabase() {
        try ( Connection connection = DatabaseConnector.getConnection()) {
            String insertCustomerSql = "INSERT INTO customers (customer_name, phone_number) VALUES (?, ?)";
            String insertOrderSql = "INSERT INTO orders (customer_id, cake_type, cake_size, free_delivery) VALUES (?, ?, ?, ?)";
            try {

                connection.setAutoCommit(false);

                try ( PreparedStatement customerStatement = connection.prepareStatement(insertCustomerSql, Statement.RETURN_GENERATED_KEYS)) {
                    customerStatement.setString(1, nameText.getText());
                    customerStatement.setString(2, numText.getText());
                    int affectedRows = customerStatement.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Creating customer failed, no rows affected.");
                    }

                    try ( ResultSet generatedKeys = customerStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int customerId = generatedKeys.getInt(1);

                            try ( PreparedStatement orderStatement = connection.prepareStatement(insertOrderSql)) {
                                orderStatement.setInt(1, customerId);
                                orderStatement.setString(2, cakeFlavour.getValue());
                                orderStatement.setString(3, size);
                                orderStatement.setString(4, yes.isSelected() ? "Yes" : "No");

                                orderStatement.executeUpdate();
                            }
                        } else {
                            throw new SQLException("Creating customer failed, no ID obtained.");
                        }
                    }
                }

                connection.commit();
            } catch (SQLException ex) {
                Logger.getLogger(cakeOrder.class.getName()).log(Level.SEVERE, null, ex);

                try {
                    if (connection != null) {
                        connection.rollback();
                    }
                } catch (SQLException rollbackEx) {
                    Logger.getLogger(cakeOrder.class.getName()).log(Level.SEVERE, null, rollbackEx);
                }
            } finally {

                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(cakeOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void selectFromDatabase() {
        try {
            Connection connection = DatabaseConnector.getConnection();
            String sql = "SELECT c.customer_name, c.phone_number, o.cake_type, o.cake_size "
                    + "FROM customers c JOIN orders o ON c.customer_id = o.customer_id "
                    + "WHERE c.customer_name = '" + nameSearch.getText() + "'";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            String text = "";
            while (resultSet.next()) {
                text += resultSet.getString("customer_name")
                        + " with phone number " + resultSet.getString("phone_Number")
                        + " ordered a " + resultSet.getString("cake_size") + " "
                        + resultSet.getString("cake_type") + " cake \n";
            }
            searchResults.setText(text);
        } catch (SQLException ex) {
            Logger.getLogger(cakeOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
