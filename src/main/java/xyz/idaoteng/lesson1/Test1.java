package xyz.idaoteng.lesson1;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Test1 extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建数据模型
        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("John", 25),
                new Person("Jane", 30),
                new Person("Doe", 35)
        );

        // 创建表格视图
        TableView<Person> table = new TableView<>();
        table.setItems(data);

        // 创建列
        TableColumn<Person, String> nameCol = new TableColumn<>("Name");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Person, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setMinWidth(100);
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

        // 将列添加到表格中
        table.getColumns().addAll(nameCol, ageCol);

        // 创建场景并添加表格
        VBox vbox = new VBox(table);
        Scene scene = new Scene(vbox, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 数据模型类
    public static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
