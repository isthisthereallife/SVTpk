package org.m.svtpk;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import jdk.jfr.EventType;
import org.m.svtpk.entity.*;
import org.m.svtpk.services.EpisodeService;
import org.m.svtpk.utils.Arrow;
import org.m.svtpk.utils.EpisodeCopier;
import org.m.svtpk.utils.QueueHandler;
import org.m.svtpk.utils.Settings;

import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.DARKGREEN;

public class SvtpkApplication extends Application {
    static ArrayList<SeasonEntity> seasons = new ArrayList<>();
    static EpisodeEntity currentEpisode = new EpisodeEntity();
    EpisodeService episodeService = new EpisodeService();
    Text infoText;
    static Button dlBtn;
    ImageView episodeImageView;
    ImageView statusIcon;
    TextField addressTextField = new TextField("");
    Stage window;
    Settings settings;
    HBox episodeHBox;
    HBox mainContentBox;
    VBox settingsBox;
    VBox queueVBox;
    ChoiceBox<String> resolutionChoiceBox;
    ChoiceBox<String> languageChoiceBox;
    ChoiceBox<String> subsChoiceBox;
    TreeView<String> tree = new TreeView<>();
    CheckBoxTreeItem<String> treeBase;

    static SimpleDoubleProperty loadingCounter;
    static Text loaded;
    static ProgressBar progressBar;
    VBox progress;
    static ArrayList<QueueEntity> entityArrayList = new ArrayList<>();
    public static ObservableList<QueueEntity> queue = FXCollections.observableArrayList(entityArrayList);
    EpisodeCopier episodeCopier;
    Thread downThread = new Thread(episodeCopier);
    org.m.svtpk.utils.QueueHandler QueueHandler = new QueueHandler();
    Clipboard clip;


    @Override
    public void start(Stage stage) {
        window = stage;
        window.getIcons().add(Arrow.getImgArrowDown("green"));
        window.setTitle("SVTpk");
        window.setScene(homeScene());
        window.show();
    }

    public Scene homeScene() {
        downThread.start();
        settings = Settings.load();

        if (settings.isAdvancedUser()) {
            System.out.println("System property: " + System.getProperty("user.dir"));
            System.out.println("Operating System: " + System.getProperty("os.name"));
            System.out.println("Java runtime version: " + System.getProperty("java.runtime.version"));
        }
        GridPane grid = basicGrid();
        Label addressFieldLabel = new Label("Ange adress till avsnitt");
        addressFieldLabel.setAlignment(Pos.CENTER);

        //paste from clipboard if clipboard text exists and seems relevant
        clip = Clipboard.getSystemClipboard();
        addressTextField =
                Objects.equals(addressTextField.getText(), "") ?
                        clip.getString() == null ?
                                new TextField()
                                : clip.getString().contains("svt") ?
                                new TextField(Clipboard.getSystemClipboard().getString())
                                :
                                new TextField()
                        : addressTextField;
        addressTextField.setPrefWidth(400);


        // QUEUE

        ListView<QueueEntity> queueListView = new ListView<>();
        //QueueEntity qE = new QueueEntity(currentEpisode);
        queueListView.setVisible(false);
        queueListView.setItems(queue);
        queueListView.setPrefWidth(200);
        queueListView.setPrefHeight(600);
        //queueListView.setContextMenu(getContextMenu());

        queueVBox = new VBox(queueListView);
        episodeImageView = currentEpisode.getImageURL() == null ? new ImageView() : new ImageView(new Image(String.valueOf(currentEpisode.getImageURL())));
        episodeImageView.setPreserveRatio(true);
        episodeImageView.setFitWidth(200);

        infoText = new Text(currentEpisode.hasID(currentEpisode) ? currentEpisode.toString() : "");
        infoText.prefHeight(160);
        infoText.setFill(DARKGREEN);
        VBox vBoxInfoText = new VBox(addressFieldLabel, addressTextField, episodeImageView, infoText);

        //lägg till alternativen till den här,
        VBox vBoxSettings = new VBox();
        vBoxSettings.prefWidth(100);

        Text resText = new Text("Upplösning");
        ArrayList<String> resolutionsList = new ArrayList<>();
        resolutionChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(resolutionsList));
        resolutionChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                settings.setResolution(newValue);
                settings.save();
            }
        });
        HBox res = new HBox(resText, resolutionChoiceBox);
        res.setSpacing(30);
        res.setAlignment(Pos.BASELINE_LEFT);
        res.setPrefWidth(200);

        Text subsText = new Text("Undertexter");
        ArrayList<String> subs = new ArrayList<>();
        subsChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(subs));
        subsChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null && !newValue.trim().equals("")) {
                settings.setSubs(newValue);
                settings.save();
            }
        });
        HBox sub = new HBox(subsText, subsChoiceBox);
        sub.setSpacing(29);

        Text languageText = new Text("Språk");
        HashMap<String, AudioReferencesEntity> availableAudio = currentEpisode.getAvailableAudio();
        ArrayList<String> audioList = new ArrayList<>();
        availableAudio.forEach((k, v) -> {
            audioList.add(v.getLabel());
        });
        languageChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(audioList));
        languageChoiceBox.setItems(FXCollections.observableArrayList(audioList));
        languageChoiceBox.valueProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                settings.setAudio(newValue);
                settings.save();
            }
        });
        HBox lang = new HBox(languageText, languageChoiceBox);
        lang.setSpacing(62);

        Text dirText = new Text("Spara till...");
        DirectoryChooser d = new DirectoryChooser();
        Button dirBtn = new Button("Välj");

        HBox hBoxCopy = new HBox(dirText, dirBtn);
        Text currentSavePath = settings.getPath() == null ? new Text() : new Text(settings.getPath());//settings.getPath()==null ? "" : settings.getPath());

        currentSavePath.setOnMouseEntered(e -> {
            currentSavePath.setFill(Paint.valueOf(String.valueOf(DARKGREEN)));
        });
        currentSavePath.setOnMouseExited(e -> {
            currentSavePath.setFill(Paint.valueOf(String.valueOf(BLACK)));
        });
        currentSavePath.setWrappingWidth(150);
        currentSavePath.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                settings.setPath(newValue);
                settings.save();
            }
        });
        currentSavePath.setOnMouseClicked(e -> {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + currentSavePath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        hBoxCopy.setSpacing(38);

        dirBtn.setOnAction(e -> {
                    String path = String.valueOf(d.showDialog(window));
                    if (!path.equals("null")) {
                        settings.setPath(path);
                    } else {
                        settings.setPath();
                    }
                    settings.save();
                    currentSavePath.setText(settings.getPath());
                }
        );
        VBox copy = new VBox(hBoxCopy, currentSavePath);

        vBoxSettings.getChildren().add(res);
        vBoxSettings.getChildren().add(lang);
        vBoxSettings.getChildren().add(sub);
        vBoxSettings.getChildren().add(copy);


        TitledPane settingsPane = new TitledPane("Inställningar", vBoxSettings);
        settingsPane.setLayoutX(1);
        settingsPane.setLayoutY(1);
        settingsPane.maxWidth(50);


        Accordion accordion = new Accordion();
        accordion.getPanes().add(settingsPane);
        settingsBox = new VBox(accordion);

        episodeHBox = new HBox(vBoxInfoText, settingsBox);
        mainContentBox = mainContentBox != null ? mainContentBox : new HBox(episodeHBox);

        episodeHBox.getChildren().add(tree);

        episodeHBox.setVisible(currentEpisode.hasID(currentEpisode));

        statusIcon = currentEpisode.hasID(currentEpisode) ? Arrow.getImgViewArrowDown("green") : Arrow.getImgViewArrowDown("grey");
        HBox statusIndicator = new HBox(statusIcon);
        statusIndicator.prefHeight(100);
        statusIndicator.setAlignment(Pos.BOTTOM_CENTER);
        statusIndicator.setDisable(!currentEpisode.hasID(currentEpisode));
        dlBtn = new Button("Kopiera");

        loadingCounter = loadingCounter != null ? loadingCounter : new SimpleDoubleProperty();
        loadingCounter.addListener(((observableValue, number, newValue) -> {
            loadingCounter.set((Double) newValue);
            /*
            dlBtn.setDisable(true);
            if (loadingCounter.getValue() >= 0) {
                loaded.setFill(Color.DARKGREY);
                dlBtn.setText("Kopierar...");
            }
            if (loadingCounter.getValue() == 100) {
                loaded.setFill(DARKGREEN);
                dlBtn.setText("Kopierat!");
            }
             */
        }));

        progress = progress != null ? progress : new VBox();
        //progress.prefWidth(200);
        loaded = loaded != null ? loaded : new Text();
        loaded.setText("");
        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        progress.getChildren().add(progressBar);
        progress.getChildren().add(loaded);
        progress.setAlignment(Pos.BOTTOM_CENTER);


        dlBtn.setDisable(!currentEpisode.hasID(currentEpisode));
        HBox hboxDlBtn = new HBox(10);
        hboxDlBtn.setAlignment(Pos.BOTTOM_CENTER);
        hboxDlBtn.getChildren().add(dlBtn);


        addressTextField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                currentEpisode = episodeService.findEpisode(addressTextField.getText());
                if (currentEpisode.getSvtId() != null) {
                    seasons = episodeService.getSeasonsFromEpisode(currentEpisode);
                }
                updateUI();
            }
        });
        String findEpisodeBtnText = clip.getString() == null ?
                "Hitta" : clip.getString().contains("svt") && addressTextField.getText().length() > 0 ? "Klistra in och Hitta" : "Hitta";
        Button findEpisodeBtn = new Button(findEpisodeBtnText);
        findEpisodeBtn.setOnAction(e -> {
            if (addressTextField.getText().equals("") && clip.getString() != null && clip.getString().contains("svt")) {
                addressTextField.setText(clip.getString());
                currentEpisode = episodeService.findEpisode(clip.getString());
            } else {
                currentEpisode = episodeService.findEpisode(addressTextField.getText());
            }
            if (currentEpisode.getSvtId() != null) {
                seasons = episodeService.getSeasonsFromEpisode(currentEpisode);
            }


            updateUI();
        });


        addressTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (addressTextField.getText().length() < 1 && clip.getString() != null && clip.getString().contains("svt")) {
                currentEpisode = new EpisodeEntity();
                findEpisodeBtn.setText("Klistra in och Hitta");
                updateUI();
            } else {
                findEpisodeBtn.setText("Hitta");
            }

        });


        HBox search = new HBox(10);
        search.getChildren().add(addressTextField);
        search.getChildren().add(findEpisodeBtn);

        // Button "Kopiera"
        dlBtn.setOnAction(e -> {
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            dlBtn.setText("Kopierar...");
            dlBtn.setDisable(true);

            //kolla i seasons vilka som är WANTED

            for (SeasonEntity seasonInSeasons : seasons) {
                for (EpisodeEntity episodeInSeason : seasonInSeasons.getItems()) {
                    if (episodeInSeason.getProgressState() != null && episodeInSeason.getProgressState().equals(ProgressStates.WANTED)) {
                        boolean alreadyInQueue = false;
                        // kolla om avsnittet redan finns i queue, isf så skit i att göra ett nytt
                        for (QueueEntity episodeFromQueue : queue) {
                            if (episodeFromQueue.getEpisode().getSvtId().equals(episodeInSeason.getSvtId())) {
                                alreadyInQueue = true;
                            }
                        }
                        // det är den HÄR jag vill ha
                        //gör en riktig hämtning av avsnitten. här? kanske inte behövs. innan den ska laddas ner.
                        if (!alreadyInQueue) {
                            EpisodeEntity realEntity = episodeService.findEpisode(episodeInSeason.getSplashURL().toString());
                            realEntity.setProgressState(ProgressStates.QUEUED);
                            QueueEntity queueEntity = new QueueEntity(realEntity);
                            queueEntity.setContextMenu(queueEntity.createContextMenu());
                            queueEntity.setText(queueEntity.toString());
                            queueEntity.setWrapText(false);
                            queueEntity.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, null, null)));
                            queueEntity.setPrefWidth(135);
                            queueEntity.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
                            queueEntity.setPadding(Insets.EMPTY);
                            queue.add(queueEntity);
                        }

                    }
                }

            }
            queueListView.setVisible(true);
            File f = new File("src/main/resources/style.css");
            try {
                queueListView.getStyleClass().add(f.toURI().toURL().toString());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }

            queueListView.setItems(queue);
            updateUI();
        });

        Button debugBtn = new Button("DEBUG");
        debugBtn.setAlignment(Pos.BOTTOM_CENTER);
        debugBtn.setOnAction(e -> {
        });


        HBox episodeTree = new HBox();
        //grid.setGridLinesVisible(true);
        ColumnConstraints cC = new ColumnConstraints();
        cC.setPercentWidth(100);
        grid.getColumnConstraints().add(cC);
        grid.add(addressFieldLabel, 0, 1);
        grid.add(search, 0, 2, 6, 1);
        mainContentBox.getChildren().add(queueVBox);
        grid.add(mainContentBox, 0, 4);
        grid.add(statusIndicator, 0, 4);
        grid.add(progress, 0, 6);
        grid.add(episodeTree, 0, 5, 2, 6);
        grid.add(hboxDlBtn, 0, 7);

        //grid.add(debugBtn, 0, 7);
        return new Scene(grid, 800, 600);
    }

    public static void updateLoadingBar(QueueEntity qE, double progress) {
        qE.getEpisode().setProgressDouble(progress);
        qE.setText(qE.toString());
        if ((int) progress == 1 && qE.getEpisode().getSvtId().toString().equals(currentEpisode.getSvtId().toString())) {
            dlBtn.setText("Kopierat!");

        }
        //progressBar.setVisible(true);
        //progressBar.setProgress(loaderCounter);
        //loaded.setText(Math.round(loaderCounter * 100) + "%");

        // loadingCounter.set(loaderCounter * 100);
    }


    private void updateUI() {
        episodeHBox.setVisible(currentEpisode.hasID(currentEpisode));
        progressBar.setVisible(false);
        tree.setDisable(true);
        tree.setVisible(false);
        if (queue.size() > 0) queueVBox.setDisable(false);
        if (currentEpisode.isLive()) {
            // if requested episode is a live-stream
            loaded.setVisible(true);
            loaded.setFill(Color.FIREBRICK);
            loaded.setText("\n\nDu kan tyvärr inte kopiera en live-sändning.");
            episodeImageView.setVisible(false);
            settingsBox.setVisible(false);
            currentEpisode = new EpisodeEntity();
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(true);
        } else if (!currentEpisode.getSvtId().equals("") && !currentEpisode.getSvtId().equalsIgnoreCase("upcoming")) {
            //if there is a SvtId
            setVideoRes();
            setAudioLanguage();
            setSubs();
            infoText.setVisible(true);
            infoText.setFill(DARKGREEN);

            settingsBox.setVisible(true);
            if (currentEpisode.getImageURL() != null) {
                // if there is an image URL
                episodeImageView.setImage(new Image(String.valueOf(currentEpisode.getImageURL())));
                episodeImageView.setVisible(true);
            }
            statusIcon.setImage(Arrow.getImgArrowDown("green"));
            statusIcon.setDisable(false);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(false);
            System.out.println("(/#¤()/#¤#¤&#)¤&)#)/)))");

            //tree = new TreeView();
            tree.setDisable(false);
            tree.setVisible(true);
            treeBase = new CheckBoxTreeItem<>(currentEpisode.getProgramTitle());

            tree.setEditable(true);

            tree.getSelectionModel().selectedItemProperty()
                    .addListener((observableValue, oldItem, newItem) -> {
                                System.out.println("observableValue: " + observableValue);
                                System.out.println("old item: " + oldItem);
                                System.out.println("new item: " + newItem);
                            }
                    );
            // season node
            for (SeasonEntity season : seasons) {
                if (season.getType().equals(SeasonTypes.season)
                        || season.getType().equals(SeasonTypes.accessibility)
                        || season.getType().equals(SeasonTypes.productionPeriod)
                        || season.getType().equals(SeasonTypes.clip)
                        || season.getType().equals(SeasonTypes.unknown)) {
                    CheckBoxTreeItem<String> seasonNode = new CheckBoxTreeItem<>(season.getName());

                    // Dessa lådor behöver inget veta. Jag gör en lyssnare här istället.

                    treeBase.getChildren().add(seasonNode);
                    tree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
                    for (EpisodeEntity episode : season.getItems()) {
                        ContextMenu contextMenu = createSeasonItemContextMenu(episode);


                        Node arrowImageNode = new ImageView();
                        boolean inQueue = false;
                        // om items i seasons återfinns i queue
                        for (QueueEntity qE : queue) {
                            if (qE.getEpisode().getSvtId().equals(episode.getSvtId())) {
                                episode.setProgressState(qE.getEpisode().getProgressState());
                                arrowImageNode = Arrow.getImgViewArrowDown("green", 15);
                                inQueue = true;

                            }
                        }

                        CheckBoxTreeItem<String> episodeLeaf = new CheckBoxTreeItem<String>(episode.getEpisodeTitle(), arrowImageNode);
                        //ändra deras status
                        if (inQueue) {
                            episodeLeaf.setIndeterminate(true);
                        }

                        if (episode.getSvtId().equals(currentEpisode.getSvtId())) {
                            
                            currentEpisode.setDescription(episode.getDescription());//

                            infoText.setText(currentEpisode.toString());
                            infoText.setWrappingWidth(200);

                            episode.setProgressState(ProgressStates.WANTED);
                            episodeLeaf.setSelected(true);
                            treeBase.setExpanded(true);
                            treeBase.setIndeterminate(true);
                            seasonNode.setExpanded(true);
                            seasonNode.setIndeterminate(true);
                            //episodeLeaf.setExpanded(true);
                        }
                        seasonNode.getChildren().add(episodeLeaf);

                        seasonNode.addEventHandler(
                                CheckBoxTreeItem.<String>checkBoxSelectionChangedEvent(),
                                (CheckBoxTreeItem.TreeModificationEvent<String> e) -> {
                                    if (episodeLeaf.isSelected()) {
                                        episode.setProgressState(ProgressStates.WANTED);
                                    } else if (!episodeLeaf.isSelected()) {
                                        episode.setProgressState(ProgressStates.IGNORED);
                                    }
                                    //else if( episodeLeaf.isIndeterminate()){
                                    //    System.out.println("????????????\nIndeterminate???? \n"+episode.toString());
                                    //}
                                }
                        );
                    }
                }
            }

            tree.setRoot(treeBase);
            tree.setShowRoot(true);
            ;
            episodeHBox.setDisable(false);
            episodeHBox.setVisible(true);


        } else if (addressTextField.getText().length() > 0) {
            //if text supplied but no episode found
            currentEpisode = new EpisodeEntity();
            loaded.setVisible(true);
            loaded.setFill(Color.FIREBRICK);
            loaded.setText("Tyvärr, hittar inte det avsnittet.");
            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(true);
        } else {
            // no text in text field, clear UI
            loaded.setText("");
            currentEpisode = new EpisodeEntity();
            infoText.setVisible(false);

            episodeImageView.setImage(null);
            statusIcon.setImage(Arrow.getImgArrowDown("grey"));
            statusIcon.setDisable(true);
            dlBtn.setText("Kopiera");
            dlBtn.setDisable(true);
        }
    }

    private ContextMenu createSeasonItemContextMenu(EpisodeEntity episode) {
        System.out.println("Creating new Context menu for episode: " + episode.getEpisodeTitle());
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem miDetails = new MenuItem("Visa info");

        ArrayList<MenuItem> list = new ArrayList<>();
        list.add(miDetails);
        contextMenu.getItems().addAll(list);
        miDetails.setOnAction((mi) -> {
            currentEpisode = episode;
            updateUI();
        });
        return contextMenu;
    }

    private void setVideoRes() {
        String selectedRes = "";
        ArrayList<String> res = new ArrayList<>();
        for (Map.Entry<String, VideoReferencesEntity> entry : currentEpisode.getAvailableResolutions().entrySet()) {
            res.add(entry.getKey());
            if (settings.getResolution().equals(entry.getKey())) {
                selectedRes = entry.getKey();
                resolutionChoiceBox.setValue(selectedRes);
            }
        }
        if (resolutionChoiceBox.getValue() == null) {
            selectedRes = res.get(0);
        }
        resolutionChoiceBox.setItems(FXCollections.observableArrayList(res));
        resolutionChoiceBox.setValue(selectedRes);
    }

    private void setAudioLanguage() {
        String selectedAudio = "";
        ArrayList<String> lang = new ArrayList<>();
        for (Map.Entry<String, AudioReferencesEntity> entry : currentEpisode.getAvailableAudio().entrySet()) {
            lang.add(entry.getKey());
            if (settings.getAudio().equals(entry.getKey())) {
                selectedAudio = entry.getKey();
                languageChoiceBox.setValue(selectedAudio);
            }
        }
        if (languageChoiceBox.getValue() == null) {
            selectedAudio = lang.get(0);
        }
        languageChoiceBox.setItems(FXCollections.observableArrayList(lang));
        languageChoiceBox.setValue(selectedAudio);
    }

    private void setSubs() {
        String selectedSubs = "Inga undertexter";
        ArrayList<String> subs = new ArrayList<>();
        subs.add(selectedSubs);
        for (Map.Entry<String, SubtitleReferencesEntity> entry : currentEpisode.getAvailableSubs().entrySet()) {
            subs.add(entry.getKey());
            if (settings.getSubs().equals(entry.getKey())) {
                selectedSubs = entry.getKey();
                subsChoiceBox.setValue(selectedSubs);
            }
        }
        if (subsChoiceBox.getValue() == null) {
            selectedSubs = subs.get(0);
        }
        subsChoiceBox.setItems(FXCollections.observableArrayList(subs));
        subsChoiceBox.setValue(selectedSubs);
    }

    private GridPane basicGrid() {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        //grid.setPrefSize(500, 400);
        //grid.setMinSize(500, 400);
        //grid.setPrefWidth(600);
        //grid.setGridLinesVisible(true);
        Text title = new Text("SVTpk");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTextAlignment(TextAlignment.CENTER);
        grid.add(title, 0, 0, 2, 1);
        return grid;
    }

    public static void main(String[] args) {
        launch();
    }

}
