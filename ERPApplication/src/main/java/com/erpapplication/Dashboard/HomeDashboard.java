package com.erpapplication.Dashboard;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.prefs.Preferences;

public class HomeDashboard extends Application {
    public static final URL SPLASH_IMAGE = HomeDashboard.class.getClassLoader().getResource("com/erpapplication/images/dm_LOGO1.jpg");
    private static final int SPLASH_WIDTH = 480;
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    public static Stage mainStage;

    private static final String WINDOW_POSITION_X = "Window_Position_X";
    private static final String WINDOW_POSITION_Y = "Window_Position_Y";
    private static final String WINDOW_WIDTH = "Window_Width";
    private static final String WINDOW_HEIGHT = "Window_Height";
    private static final double DEFAULT_X = 10;
    private static final double DEFAULT_Y = 10;
    private static final double DEFAULT_WIDTH = 1300;
    private static final double DEFAULT_HEIGHT = 700;
    private static final String NODE_NAME = "ViewSwitcher";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        ImageView splash = new ImageView(new Image(String.valueOf(SPLASH_IMAGE)));
        splash.setStyle("-fx-background-color: #fff");
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        loadProgress.setStyle("-fx-accent: #00b7ff");
        progressText = new Label();
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-padding: 5;" + "-fx-background-color: #fff;");
    }

    @Override
    public void start(Stage stage) {
        final Task<ObservableList<String>> friendTask = new Task<>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> foundComp = FXCollections.observableArrayList();
                ObservableList<String> availableComp =
                        FXCollections.observableArrayList("Core", "Database", "AADE", "Components");
                progressText.setStyle("-fx-font-size: 1.1em;");
                Thread.sleep(200);
                for (int i = 0; i < availableComp.size(); i++) {
                    updateProgress(i + 1, availableComp.size());
                    String Comp = availableComp.get(i);
                    foundComp.add(Comp);

                    double prog = 0.25*(i+1);
                    updateMessage("Loading " + Comp + "...\t\t" + prog*100 + " %");
                    Random rand = new Random();
                    Thread.sleep(rand.nextInt(400));
                }
                return foundComp;
            }
        };

        showSplash(stage, friendTask, () -> {
            try {
                showMainStage();
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Error initializing application");
                a.setContentText(e.getMessage());
                e.printStackTrace();
                a.showAndWait();
            }});
        new Thread(friendTask).start();
    }

    private void showMainStage() throws IOException {
        Stage mainStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("com/erpapplication/Home.fxml"));
        Parent content = loader.load();

        mainStage.setTitle("Diagnosis Multisystems ERP");
        mainStage.getIcons().addAll(new Image(String.valueOf(SPLASH_IMAGE)));
        mainStage.setMinHeight(DEFAULT_HEIGHT);
        mainStage.setMinWidth(DEFAULT_WIDTH);
        mainStage.setResizable(true);
        mainStage.setMaximized(true);

        Preferences pref = Preferences.userRoot().node(NODE_NAME);
        double x = pref.getDouble(WINDOW_POSITION_X, DEFAULT_X);
        double y = pref.getDouble(WINDOW_POSITION_Y, DEFAULT_Y);
        double width = pref.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH);
        double height = pref.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT);
        mainStage.setX(x);
        mainStage.setY(y);
        mainStage.setWidth(width);
        mainStage.setHeight(height);

        mainStage.setOnCloseRequest((final WindowEvent event) -> {
            Preferences preferences = Preferences.userRoot().node(NODE_NAME);
            preferences.putDouble(WINDOW_POSITION_X, mainStage.getX());
            preferences.putDouble(WINDOW_POSITION_Y, mainStage.getY());
            preferences.putDouble(WINDOW_WIDTH, mainStage.getWidth());
            preferences.putDouble(WINDOW_HEIGHT, mainStage.getHeight());
        });

        mainStage.setScene(new Scene(content));
        mainStage.show();
    }

    private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
        initStage.getIcons().addAll(new Image(String.valueOf(SPLASH_IMAGE)));
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.0), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.close());
                fadeSplash.play();

                initCompletionHandler.complete();
            }});

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        initStage.setScene(splashScene);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public interface InitCompletionHandler {
        void complete();
    }
}