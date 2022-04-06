package org.m.svtpk;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.services.EpisodeService;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SvtpkApplication extends Application {
    EpisodeEntity currentEpisode = new EpisodeEntity();
    EpisodeService episodeService = new EpisodeService();
    Text infoText;
    Button dlBtn;
    ImageView episodeImageView;
    TextField addressTextField;

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image("file:src/main/resources/images/arrow.png"));
        stage.setTitle("SVTpk");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text title = new Text("SVTpk");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTextAlignment(TextAlignment.CENTER);
        grid.add(title, 0, 0, 2, 1);

        Label addressFieldLabel = new Label("Ange adress");
        addressFieldLabel.setAlignment(Pos.CENTER);
        grid.add(addressFieldLabel, 0, 2);

        addressTextField = new TextField();
        addressTextField.setPrefWidth(400);


        Image episodeImage = null;
        episodeImageView = new ImageView(episodeImage);
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);
        grid.add(episodeImageView,0,4);

        infoText = new Text();
        HBox hBoxInfoText = new HBox(10);
        infoText.prefHeight(160);
        hBoxInfoText.getChildren().add(infoText);

        grid.add(hBoxInfoText, 0, 8);

        dlBtn = new Button("Kopiera");
        dlBtn.setDisable(true);
        HBox hboxDlBtn = new HBox(10);
        hboxDlBtn.setAlignment(Pos.BOTTOM_CENTER);
        hboxDlBtn.getChildren().add(dlBtn);
        grid.add(hboxDlBtn, 0, 10);



        addressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentEpisode = episodeService.findEpisode(addressTextField.getText());
            updateUI();
        });


        Button findEpisodeBtn = new Button("Hitta");
        findEpisodeBtn.setOnAction(e->{
            currentEpisode = episodeService.findEpisode(addressTextField.getText());
            updateUI();
        });

        HBox search = new HBox(10);
        search.getChildren().add(addressTextField);
        search.getChildren().add(findEpisodeBtn);
        grid.add(search, 0, 3);

        dlBtn.setOnAction(e -> {
            episodeService.copyEpisodeToDisk(currentEpisode);
        });
        Scene scene = new Scene(grid, 640, 480);
        stage.setScene(scene);


        stage.show();
    }

    private void updateUI(){
        if (!currentEpisode.getSvtId().equals("")) {
            infoText.setVisible(true);
            infoText.setFill(Color.DARKGREEN);
            infoText.setText(currentEpisode.toString());
            episodeImageView.setImage(new Image(currentEpisode.getImageURL()));

            dlBtn.setDisable(false);
        } else if (addressTextField.getText().length() > 0) {
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(true);
            infoText.setFill(Color.FIREBRICK);
            infoText.setText("Tyvärr, hittar inte det avsnittet.");
            episodeImageView.setImage(null);
            dlBtn.setDisable(true);
        } else {
            infoText.setVisible(false);
            episodeImageView.setImage(null);
            dlBtn.setDisable(true);
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
