package org.m.svtpk;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SvtpkApplication extends Application {

    @Override
    public void start(Stage mainStage){
        mainStage.setTitle("SVTpk");
        Scene scene = new Scene(new GridPane(),640,480);
        mainStage.setScene(scene);
        mainStage.show();
    }
    public static void main(String[] args) {
        launch();
    }

}
