package perilucu;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class FXMLController implements Initializable {

    @FXML
    private AnchorPane ancorr;
    @FXML
    private ImageView player;
    @FXML
    private Label scoreLabel;
    @FXML
    private ImageView bggame;

    private final List<GameObject> fallingObjects = new ArrayList<>();
    private final Random random = new Random();
    private boolean isFlying = false;
    private boolean isFrozen = false;
    private long flyEndTime = 0;
    private long freezeEndTime = 0;
    private ImageView gameOverImage;
    private boolean gameOver = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ancorr.setOnKeyPressed(this::movePlayer);
        ancorr.setOnMouseClicked(event -> ancorr.requestFocus());
        ancorr.requestFocus();

        try {
             bggame.setImage(new Image(getClass().getResource("/perilucu/pixel game/bggame.jpeg").toString()));
        } catch (Exception e) {
             System.err.println("Gagal memuat gambar latar belakang: " + e.getMessage());
             e.printStackTrace();
}

        gameOverImage = new ImageView(new Image(getClass().getResource("/perilucu/pixel game/popupgameover.png").toString()));
        gameOverImage.setFitWidth(200); 
        gameOverImage.setFitHeight(250); 
        gameOverImage.setLayoutX((ancorr.getWidth() - 200) / 2 + 115);
        gameOverImage.setLayoutY(50);
        gameOverImage.setVisible(false);
        ancorr.getChildren().add(gameOverImage);


        GameState.resetScore();
        updateScore();
        startGameLoop();
    }

    private void movePlayer(KeyEvent event) {
        if (isFrozen || gameOver) return;

        if (event.getCode() == KeyCode.LEFT) {
            player.setLayoutX(Math.max(player.getLayoutX() - 5, 0));
        } else if (event.getCode() == KeyCode.RIGHT) {
            player.setLayoutX(Math.min(player.getLayoutX() + 5, ancorr.getWidth() - player.getFitWidth()));
        } else if (isFlying) {
            if (event.getCode() == KeyCode.UP) {
                player.setLayoutY(Math.max(player.getLayoutY() - 5, 0));
            } else if (event.getCode() == KeyCode.DOWN) {
                player.setLayoutY(Math.min(player.getLayoutY() + 5, ancorr.getHeight() - player.getFitHeight()));
            }
        }
    }

    private void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastSpawnTime = 0;

            @Override
            public void handle(long now) {
                if (gameOver) {
                    stop();
                    return;
                }

                if (now - lastSpawnTime > 2_000_000_000) {
                    spawnFallingObject();
                    lastSpawnTime = now;
                }
                updateFallingObjects();
                updateStates();
            }
        };
        gameLoop.start();
    }

    private void spawnFallingObject() {
        GameObject obj;
        switch (random.nextInt(4)) {
            case 0:
                obj = new Food();
                break;
            case 1:
                obj = new Enemy();
                break;
            case 2:
                obj = new Diamond();
                break;
            default:
                obj = new Freeze();
                break;
}


        obj.setLayoutX(random.nextDouble() * (ancorr.getWidth() - obj.getFitWidth()));
        obj.setLayoutY(0);
        fallingObjects.add(obj);
        ancorr.getChildren().add(obj);
    }

    private void updateFallingObjects() {
        List<GameObject> toRemove = new ArrayList<>();
        for (GameObject obj : fallingObjects) {
            obj.setLayoutY(obj.getLayoutY() + 2);

            if (obj.getBoundsInParent().intersects(player.getBoundsInParent())) {
                obj.applyEffect(this);
                toRemove.add(obj);
            }

            if (obj.getLayoutY() > ancorr.getHeight()) {
                toRemove.add(obj);
            }
        }
        fallingObjects.removeAll(toRemove);
        ancorr.getChildren().removeAll(toRemove);
    }

    private void updateStates() {
        if (isFlying && System.currentTimeMillis() > flyEndTime) {
            isFlying = false;
            player.setOpacity(1.0);
        }

        if (isFrozen && System.currentTimeMillis() > freezeEndTime) {
            isFrozen = false;
            player.setOpacity(1.0);
        }
    }

    private void checkGameOver() {
        if (GameState.getScore() < -10) {
            gameOver = false;
            player.setDisable(false);
            gameOverImage.setVisible(false);
            GameState.resetScore();
            updateScore();
        }
    }

    public void startFlying() {
        isFlying = true;
        flyEndTime = System.currentTimeMillis() + 10_000;
        player.setOpacity(0.7);
    }

    public void startFreezing() {
        isFrozen = true;
        freezeEndTime = System.currentTimeMillis() + 5_000;
        player.setOpacity(0.5);
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + GameState.getScore());
    }

    @FXML
    private void moveperi(KeyEvent event) {
    }

    private static class GameState {
        private static int score = 0;

        static void resetScore() {
            score = 0;
        }

        static int getScore() {
            return score;
        }

        static void increaseScore(int value) {
            score += value;
        }
    }

    private abstract static class GameObject extends ImageView {
        GameObject(String imagePath) {
            super(new Image(FXMLController.class.getResource(imagePath).toString()));
            setFitWidth(30);
            setFitHeight(30);
        }

        abstract void applyEffect(FXMLController controller);
    }

    private static class Food extends GameObject {
        Food() {
            super("/perilucu/pixel game/makanan.png");
        }

        @Override
        void applyEffect(FXMLController controller) {
            GameState.increaseScore(10);
            controller.updateScore();
        }
    }

    private static class Enemy extends GameObject {
        Enemy() {
            super("/perilucu/pixel game/musuh.png");
        }

        @Override
        void applyEffect(FXMLController controller) {
            GameState.increaseScore(-10);
            controller.updateScore();
            controller.gameOver = true;
            controller.player.setDisable(true);
            controller.gameOverImage.setVisible(true);
        }
    }

    private static class Diamond extends GameObject {
        Diamond() {
            super("/perilucu/pixel game/kekuatan.png");
        }

        @Override
        void applyEffect(FXMLController controller) {
            controller.startFlying();
        }
    }

    private static class Freeze extends GameObject {
        Freeze() {
            super("/perilucu/pixel game/ngefreez.png");
        }

        @Override
        void applyEffect(FXMLController controller) {
            controller.startFreezing();
        }
    }
}
