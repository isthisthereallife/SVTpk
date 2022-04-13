package org.m.svtpk;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.services.EpisodeService;
import org.m.svtpk.utils.Arrow;
import org.m.svtpk.utils.Settings;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SvtpkApplication extends Application {
    EpisodeEntity currentEpisode = new EpisodeEntity();
    EpisodeService episodeService = new EpisodeService();
    Text infoText;
    Button dlBtn;
    ImageView episodeImageView;
    ImageView statusIcon;
    TextField addressTextField;
    Stage window;
    Settings settings;
    SimpleIntegerProperty loadingCounter;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.getIcons().add(new Image("file:src/main/resources/images/arrow_green.png"));
        window.setTitle("SVTpk");
        window.setScene(homeScene());
        window.show();
    }

    private Scene optionsScene() {
        GridPane grid = basicGrid();
        //HBox navbar = Navbar.getNavbar(window);
        HBox navbar = new HBox(10);
        navbar.prefHeight(160);
        navbar.setSpacing(100);

        // left part

        ImageView imgArrowLeft = Arrow.getImgViewArrowLeft();


        HBox goBackButtonBox = new HBox(10);
        Text goBackText = new Text("Gå Tillbaka");
        goBackText.setTextAlignment(TextAlignment.LEFT);
        goBackButtonBox.getChildren().add(imgArrowLeft);
        goBackButtonBox.getChildren().add(goBackText);
        goBackButtonBox.setOnMouseClicked(e -> {
            window.setScene(homeScene());
        });


        navbar.getChildren().add(goBackButtonBox);


        //right part
        Label resolutionLabel = new Label("Inställningar");
        resolutionLabel.setAlignment(Pos.CENTER_LEFT);
        navbar.getChildren().add(resolutionLabel);


        grid.add(navbar, 0, 8);
        return new Scene(grid, 640, 480);
    }

    private Scene optionsScene2(EpisodeEntity currentEpisode) {

        BorderPane borderPane = new BorderPane();
        MenuBar menuBar = new MenuBar();
        Menu filesMenu = new Menu("Fil");
        filesMenu.getItems().addAll(new MenuItem("Klickity Klick"), new SeparatorMenuItem(), new MenuItem("Exit"));
        Menu helpMenu = new Menu("Hjälp");
        helpMenu.getItems().addAll(new MenuItem("ffmpeg"), new MenuItem("SvtPlay"));
        menuBar.getMenus().addAll(filesMenu, helpMenu);
        ImageView imgArrowLeft = new ImageView(new Image("file:src/main/resources/images/arrow_green.png"));

        HBox navbar = new HBox(10);
        navbar.prefHeight(160);
        navbar.setSpacing(100);

        // left part

        imgArrowLeft.rotateProperty().setValue(90);
        imgArrowLeft.setPreserveRatio(true);
        imgArrowLeft.setFitWidth(20);

        HBox goBackButtonBox = new HBox(10);
        Text goBackText = new Text("Gå Tillbaka");
        goBackText.setTextAlignment(TextAlignment.LEFT);
        goBackButtonBox.getChildren().add(imgArrowLeft);
        goBackButtonBox.getChildren().add(goBackText);
        goBackButtonBox.setOnMouseClicked(e -> {
            window.setScene(homeScene());
        });


        navbar.getChildren().add(goBackButtonBox);
        VBox vbox = new VBox();
        vbox.getChildren().add(menuBar);
        vbox.getChildren().add(navbar);
        borderPane.setTop(vbox);
        borderPane.setCenter(new Text("CENTERN"));
        borderPane.setRight(new Text("Höger"));


        return new Scene(borderPane, 640, 480);

    }

    public Scene homeScene() {
        settings = Settings.load();
        GridPane grid = basicGrid();
        Label addressFieldLabel = new Label("Ange adress");
        addressFieldLabel.setAlignment(Pos.CENTER);

        addressTextField = addressTextField == null ? new TextField() : addressTextField;
        addressTextField.setPrefWidth(400);


        episodeImageView = currentEpisode.getImageURL() == null ? new ImageView() : new ImageView(new Image(currentEpisode.getImageURL()));
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);

        infoText = new Text(currentEpisode.hasID(currentEpisode) ? currentEpisode.toString() : "");
        infoText.prefHeight(160);
        infoText.setFill(Color.DARKGREEN);
        VBox vBoxInfoText = new VBox(addressFieldLabel, addressTextField, episodeImageView, infoText);

        //lägg till alternativen till den här,
        VBox vBoxSettings = new VBox();

        Text resText = new Text("Upplösning");
        String[] resolutionsList = {"1080p", "720p", "360p"};
        ChoiceBox<String> resolutionChoiceBox = new ChoiceBox<String>(FXCollections.observableArrayList(resolutionsList));
        resolutionChoiceBox.setValue("Upplösning");
        resolutionChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            settings.setResolution(newValue);
            settings.save();
        });
        HBox res = new HBox(resText, resolutionChoiceBox);

        Text subsText = new Text("Undertexter");
        String[] subsList = {"Inga undertexter", "Svenska"};
        ChoiceBox<String> subsChoiceBox = new ChoiceBox<String>(FXCollections.observableArrayList(subsList));
        subsChoiceBox.setValue(subsList[0]);
        subsChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            settings.setSubs(newValue);
            settings.save();
        });
        HBox sub = new HBox(subsText, subsChoiceBox);

        Text dirText = new Text("Spara till...");
        DirectoryChooser d = new DirectoryChooser();
        Button dirBtn = new Button("Välj");

        HBox hBoxCopy = new HBox(dirText, dirBtn);
        Text currentSavePath = settings.getPath() == null ? new Text() : new Text(settings.getPath());//settings.getPath()==null ? "" : settings.getPath());
        currentSavePath.textProperty().addListener((observableValue, s, newValue) -> {

        });

        dirBtn.setOnAction(e -> {
                    String path = String.valueOf(d.showDialog(window));
                    System.out.println("path:" + path);
                    if (!path.equals("null")) {
                        settings.setPath(path);
                        settings.save();
                        currentSavePath.setText(settings.getPath());
                    }
                }
        );
        VBox copy = new VBox(hBoxCopy, currentSavePath);
       /*
        path.valueProperty().addListener((observableValue, s, newValue) -> {
            settings.setResolution(newValue);
            settings.save();
        });
        */
        vBoxSettings.getChildren().add(res);
        vBoxSettings.getChildren().add(sub);
        vBoxSettings.getChildren().add(copy);


        TitledPane settings = new TitledPane("Inställningar för olika saker du kan ställa in", vBoxSettings);
        settings.setLayoutX(1);
        settings.setLayoutY(1);
        settings.prefWidth(200);

        Accordion accordion = new Accordion();
        accordion.getPanes().add(settings);
        VBox settingsBox = new VBox(accordion);
        settingsBox.setVisible(true);


        HBox hBoxEpisodeInfo = new HBox(vBoxInfoText, settingsBox);


        statusIcon = currentEpisode.hasID(currentEpisode) ? Arrow.getImgViewArrowDown("green") : Arrow.getImgViewArrowDown("grey");
        HBox statusIndicator = new HBox(statusIcon);
        statusIndicator.prefHeight(100);
        statusIndicator.setAlignment(Pos.BOTTOM_CENTER);
        statusIndicator.setDisable(!currentEpisode.hasID(currentEpisode));

        dlBtn = new Button("Kopiera");
        dlBtn.setDisable(!currentEpisode.hasID(currentEpisode));
        HBox hboxDlBtn = new HBox(10);
        hboxDlBtn.setAlignment(Pos.BOTTOM_CENTER);
        hboxDlBtn.getChildren().add(dlBtn);


        addressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
           /* search every time user types a letter in search box. not good now, maybe good later.
             currentEpisode = episodeService.findEpisode(addressTextField.getText());
           */
            if (addressTextField.getText().length() < 1) {
                currentEpisode = new EpisodeEntity();
                updateUI();
            }
        });
        addressTextField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                currentEpisode = episodeService.findEpisode(addressTextField.getText());
                updateUI();
            }
        });

        Button findEpisodeBtn = new Button("Hitta");
        findEpisodeBtn.setOnAction(e -> {
            currentEpisode = episodeService.findEpisode(addressTextField.getText());
            updateUI();
        });

        HBox search = new HBox(10);
        search.getChildren().add(addressTextField);
        search.getChildren().add(findEpisodeBtn);
        statusIndicator.setOnMouseClicked(e -> {
            if (currentEpisode.hasID(currentEpisode))
                dlBtn.fire();
        });
        dlBtn.setOnAction(e -> {
            //ändra statusIndicator
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            String status = episodeService.copyEpisodeToDisk(currentEpisode).toString();
            if ("200 OK".equals(status)) {
                System.out.println(status + " it goooood!");
                dlBtn.setText("Kopierat!");
                dlBtn.setDisable(true);
            } else {
                System.out.println(status);
                statusIcon.setImage(Arrow.getImgArrowDown("red"));
                infoText.setText(infoText.getText() + "\nNågot gick snett!");
            }
        });
        Button debugBtn = new Button("DEBUG");
        debugBtn.setAlignment(Pos.BOTTOM_CENTER);
        debugBtn.setOnAction(e -> {
            window.setScene(optionsScene2(currentEpisode));
        });


        grid.add(addressFieldLabel, 0, 1);
        grid.add(search, 0, 2);
        grid.add(hBoxEpisodeInfo, 0, 3);
        grid.add(statusIndicator, 0, 4);
        grid.add(hboxDlBtn, 0, 5);

        //grid.add(debugBtn, 0, 6);

        return new Scene(grid, 640, 480);
    }

    public Scene homeScene2() {
        BorderPane bp = new BorderPane();

        // INPUT FIELD AND HITTA BUTTON
        Label addressFieldLabel = new Label("Ange adress");
        addressFieldLabel.setAlignment(Pos.CENTER);
        Group addressFieldGroup = new Group(addressFieldLabel);
        addressFieldGroup.prefHeight(300);
        addressFieldGroup.prefWidth(500);
        addressFieldGroup.setAutoSizeChildren(true);


        addressTextField = addressTextField == null ? new TextField() : addressTextField;
        addressTextField.setPrefWidth(400);
        addressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (addressTextField.getText().length() < 1) {
                currentEpisode = new EpisodeEntity();
                updateUI();
            }
        });


        addressTextField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                currentEpisode = episodeService.findEpisode(addressTextField.getText());
                updateUI();
            }
        });
        addressFieldGroup.getChildren().add(addressTextField);


        Button findEpisodeBtn = new Button("Hitta");
        findEpisodeBtn.setOnAction(e -> {
            currentEpisode = episodeService.findEpisode(addressTextField.getText());
            updateUI();
        });
        addressFieldGroup.getChildren().add(findEpisodeBtn);


        episodeImageView = currentEpisode.getImageURL() == null ? new ImageView() : new ImageView(new Image(currentEpisode.getImageURL()));
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);

        infoText = new Text(currentEpisode.hasID(currentEpisode) ? currentEpisode.toString() : "");
        infoText.prefHeight(160);
        infoText.setFill(Color.DARKGREEN);

        VBox vBoxInfoText = new VBox(episodeImageView, infoText);
        vBoxInfoText.prefHeight(400);

        Group infoTextGroup = new Group(vBoxInfoText);
        infoTextGroup.prefWidth(500);
        infoTextGroup.prefHeight(400);
        infoTextGroup.setAutoSizeChildren(true);

        dlBtn = new Button("Privatkopiera");
        dlBtn.setAlignment(Pos.CENTER);
        dlBtn.setDisable(!currentEpisode.hasID(currentEpisode));
        HBox hboxDlBtn = new HBox(dlBtn);
        hboxDlBtn.setAlignment(Pos.CENTER);
        Group dlGroup = new Group(hboxDlBtn);
        dlGroup.prefHeight(200);
        dlGroup.prefWidth(500);
        dlGroup.setAutoSizeChildren(true);


        HBox search = new HBox(addressTextField, findEpisodeBtn);
        addressFieldGroup.getChildren().add(search);

        dlBtn.setOnAction(e -> {
            infoText.setText(episodeService.copyEpisodeToDisk(currentEpisode).toString());
        });
        Button debugBtn = new Button("DEBUG");
        debugBtn.setOnAction(e -> {
            window.setScene(optionsScene2(currentEpisode));
        });
        //grid.add(debugBtn, 0, 6);
        bp.setTop(addressFieldGroup);
        bp.setCenter(infoTextGroup);
        bp.setBottom(dlGroup);

        return new Scene(bp, 640, 480);
    }

    private void updateUI() {

        if (!currentEpisode.getSvtId().equals("")) {
            infoText.setVisible(true);
            infoText.setFill(Color.DARKGREEN);
            infoText.setText(currentEpisode.toString());
            episodeImageView.setImage(new Image(currentEpisode.getImageURL()));
            statusIcon.setImage(Arrow.getImgArrowDown("green"));
            statusIcon.setDisable(false);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(false);
            System.out.println("hej ui");
        } else if (addressTextField.getText().length() > 0) {
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(true);
            infoText.setFill(Color.FIREBRICK);
            infoText.setText("Tyvärr, hittar inte det avsnittet.");
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setDisable(true);
        } else {
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(false);
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setDisable(true);
        }
    }

    private GridPane basicGrid() {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setPrefSize(500, 400);
        grid.setMinSize(500, 400);
        grid.setPrefWidth(600);
        //grid.setGridLinesVisible(true);
        Text title = new Text("SVTpk");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTextAlignment(TextAlignment.LEFT);
        grid.add(title, 0, 0, 2, 1);
        return grid;
    }

    public static void main(String[] args) {
        launch();
    }

}
