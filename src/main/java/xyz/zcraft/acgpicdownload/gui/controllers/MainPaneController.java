package xyz.zcraft.acgpicdownload.gui.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.font.MFXFontIcon;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import xyz.zcraft.acgpicdownload.gui.GUI;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPaneController implements Initializable {
    public MFXButton maximizeBtn;
    GUI gui;
    double w = 800;
    double h = 500 - 30;
    double resizeX;
    double resizeY;
    double resizeW;
    double resizeH;
    @javafx.fxml.FXML
    private AnchorPane mainPane;
    @javafx.fxml.FXML
    private ImageView background;
    @javafx.fxml.FXML
    private Label titleLbl;
    @javafx.fxml.FXML
    private VBox initPane;
    @javafx.fxml.FXML
    private VBox stagePane;
    @javafx.fxml.FXML
    private ImageView blurImg;
    @javafx.fxml.FXML
    private MFXProgressBar initProgressBar;
    @javafx.fxml.FXML
    private MFXButton closeBtn;
    @javafx.fxml.FXML
    private MFXButton minimizeBtn;
    @javafx.fxml.FXML
    private HBox titlePane;
    private boolean transparent = false;
    private double origX;
    private double origY;
    private double origStageX;
    private double origStageY;
    private Image image;

    public AnchorPane getMainPane() {
        return mainPane;
    }

    public ImageView getBackground() {
        return background;
    }

    public void setBackground(InputStream stream) {
        image = new Image(stream);
        fitBackground();
    }

    private void fitBackground() {
        if(image == null) return;
        Rectangle2D vp;

        if (image.getWidth() / image.getHeight() > w / h) {
            background.setFitHeight(h);
            blurImg.setFitHeight(h);
            background.setFitWidth(0);
            blurImg.setFitWidth(0);
            vp = new Rectangle2D((image.getWidth() - (image.getHeight() / h * w)) / 2, 0, image.getWidth(), image.getHeight());
        } else {
            background.setFitWidth(w);
            blurImg.setFitWidth(w);
            background.setFitHeight(0);
            blurImg.setFitHeight(0);
            vp = new Rectangle2D(0, (image.getHeight() - image.getWidth() / w * h) / 2, image.getWidth(), image.getHeight());
        }

        background.setImage(image);
        background.setViewport(vp);
        WritableImage snapshot = background.snapshot(new SnapshotParameters(), null);
        blurImg.setImage(snapshot);
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent() {
        transparent = true;
        blurImg.setVisible(false);
        background.setVisible(false);
        titleLbl.setTextFill(Color.BLACK);
        closeBtn.setGraphic(new MFXFontIcon("mfx-x", Color.BLACK));
        minimizeBtn.setGraphic(new MFXFontIcon("mfx-minus", Color.BLACK));
    }

    public GUI getGui() {
        return gui;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }

    public void setProgress(double p) {
        initProgressBar.setProgress(p == 1 ? -1 : p);
    }

    public void initDone() {
        FadeTransition ft = new FadeTransition();
        ft.setNode(initPane);
        ft.setToValue(0);
        ft.setFromValue(1);
        ft.setDuration(Duration.millis(10));
        ft.setRate(0.1);
        ft.setOnFinished((e) -> initPane.setVisible(false));
        ft.play();

        ft.setNode(blurImg);
        ft.setOnFinished((e) -> blurImg.setVisible(false));
        ft.play();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initPane.setVisible(true);
        closeBtn.setText("");
        closeBtn.setGraphic(new MFXFontIcon("mfx-x", Color.WHITE));
        minimizeBtn.setText("");
        minimizeBtn.setGraphic(new MFXFontIcon("mfx-minus", Color.WHITE));
        maximizeBtn.setText("");
        maximizeBtn.setGraphic(new MFXFontIcon("mfx-expand", Color.WHITE));
    }

    @FXML
    private void mouseDragged(MouseEvent e) {
        gui.mainStage.setX(e.getScreenX() - origX + origStageX);
        gui.mainStage.setY(e.getScreenY() - origY + origStageY);
    }

    @FXML
    private void startMoving(MouseEvent e) {
        origStageX = gui.mainStage.getX();
        origStageY = gui.mainStage.getY();
        origX = e.getScreenX();
        origY = e.getScreenY();
    }

    @FXML
    private void minimizeBtnOnAction() {
        gui.mainStage.setIconified(true);
    }

    @FXML
    private void closeBtnOnAction() {
        System.exit(0);
    }

    public HBox getTitlePane() {
        return titlePane;
    }

    public void resizeStart(MouseEvent event) {
        resizeX = event.getScreenX();
        resizeY = event.getScreenY();
        resizeW = gui.mainStage.getWidth();
        resizeH = gui.mainStage.getHeight();
    }

    public void resize(MouseEvent event) {
        if(resizeW + event.getScreenX() - resizeX > 0 && resizeH + event.getScreenY() - resizeY > 0){
            gui.mainStage.setWidth(resizeW + event.getScreenX() - resizeX);
            gui.mainStage.setHeight(resizeH + event.getScreenY() - resizeY);
            this.w = resizeW + event.getScreenX() - resizeX;
            this.h = resizeH + event.getScreenY() - resizeY;
            fitBackground();
        }
    }

    public void maximizeBtnOnAction() {
        gui.mainStage.setMaximized(!gui.mainStage.isMaximized());
        this.w = gui.mainStage.getWidth();
        this.h = gui.mainStage.getHeight() - 30;
        fitBackground();
    }
}
