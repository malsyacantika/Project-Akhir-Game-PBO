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
    private ImageView bggame;
    @FXML
    private ImageView player;
    @FXML
    private Label scoreLabel;

    private List<ImageView> fallingObjects = new ArrayList<>();
    private Random random = new Random();
    private int score = 0;
    private boolean isFlying = false;
    private long flyEndTime = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set gambar latar
        ancorr.setOnKeyPressed(this::moveperi);
        bggame.setImage(new Image(getClass().getResource("/perilucu/pixel game/bggame.jpeg").toString()));
        ancorr.requestFocus();

        // Pastikan area permainan bisa menangkap event keyboard
        ancorr.setOnMouseClicked(event -> ancorr.requestFocus());

        // Jalankan loop permainan
        startGameLoop();
    }

@FXML
private void moveperi(KeyEvent event) {
    if (event.getCode() == KeyCode.LEFT) { // Jika tombol panah kiri ditekan
        if (player.getLayoutX() > 0) { // Cegah keluar dari layar di sisi kiri
            player.setLayoutX(player.getLayoutX() - 5); // Geser ke kiri
        }
    } else if (event.getCode() == KeyCode.RIGHT) { // Jika tombol panah kanan ditekan
        if (player.getLayoutX() + player.getFitWidth() < ancorr.getWidth()) { // Cegah keluar dari layar di sisi kanan
            player.setLayoutX(player.getLayoutX() + 5); // Geser ke kanan
        }
    } else if (isFlying && event.getCode() == KeyCode.UP) { // Terbang ke atas saat tombol panah atas ditekan
        if (player.getLayoutY() > 0) { // Cegah keluar dari layar di sisi atas
            player.setLayoutY(player.getLayoutY() - 5); // Geser ke atas
        }
    } else if (isFlying && event.getCode() == KeyCode.DOWN) { // Gerak ke bawah dalam mode terbang
        if (player.getLayoutY() + player.getFitHeight() < ancorr.getHeight()) { // Cegah keluar dari layar di sisi bawah
            player.setLayoutY(player.getLayoutY() + 5); // Geser ke bawah
        }
    }
}


    private void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastSpawnTime = 0;

            @Override
            public void handle(long now) {
                if (now - lastSpawnTime > 2_000_000_000) { // Setiap 2 detik
                    spawnFallingObject();
                    lastSpawnTime = now;
                }
                updateFallingObjects();
                checkFlyingState(now);
                checkGameOver();
            }
        };
        gameLoop.start();
    }

    private void spawnFallingObject() {
        ImageView fallingObject = new ImageView();
        int objectType = random.nextInt(3); // 0: makanan, 1: musuh, 2: diamond

        if (objectType == 0) {
            fallingObject.setImage(new Image(getClass().getResource("/perilucu/pixel game/makanan.png").toString()));
            fallingObject.setUserData("food");
        } else if (objectType == 1) {
            fallingObject.setImage(new Image(getClass().getResource("/perilucu/pixel game/musuh.png").toString()));
            fallingObject.setUserData("enemy");
        } else {
            fallingObject.setImage(new Image(getClass().getResource("/perilucu/pixel game/kekuatan.png").toString()));
            fallingObject.setUserData("diamond");
        }

        fallingObject.setFitWidth(37);
        fallingObject.setFitHeight(37);
        fallingObject.setLayoutX(random.nextDouble() * (ancorr.getWidth() - fallingObject.getFitWidth()));
        fallingObject.setLayoutY(0);

        fallingObjects.add(fallingObject);
        ancorr.getChildren().add(fallingObject);
    }

    private void updateFallingObjects() {
        List<ImageView> toRemove = new ArrayList<>();
        for (ImageView obj : fallingObjects) {
            obj.setLayoutY(obj.getLayoutY() + 2); // Objek jatuh ke bawah

            // Deteksi tabrakan dengan pemain
            if (obj.getBoundsInParent().intersects(player.getBoundsInParent())) {
                if ("food".equals(obj.getUserData())) {
                    score += 10; // Tambah skor
                } else if ("enemy".equals(obj.getUserData())) {
                    score -= 10; // Kurangi skor
                } else if ("diamond".equals(obj.getUserData())) {
                    startFlying();
                }

                scoreLabel.setText("Score: " + score);
                toRemove.add(obj);
                ancorr.getChildren().remove(obj);
            }

            // Hapus jika objek keluar layar
            if (obj.getLayoutY() > ancorr.getHeight()) {
                toRemove.add(obj);
                ancorr.getChildren().remove(obj);
            }
        }
        fallingObjects.removeAll(toRemove);
    }

    private void startFlying() {
        isFlying = true;
        flyEndTime = System.currentTimeMillis() + 10_000; // Terbang selama 10 detik
        player.setOpacity(0.7); // Efek transparan untuk mode terbang
    }

    private void checkFlyingState(long now) {
        if (isFlying && System.currentTimeMillis() > flyEndTime) {
            isFlying = false;
            player.setOpacity(1.0); // Kembali ke tampilan normal
        }
    }

    private void checkGameOver() {
        if (score <= -50) {
            scoreLabel.setText("Game Over!");
            player.setDisable(true); // Nonaktifkan gerakan
            fallingObjects.forEach(obj -> ancorr.getChildren().remove(obj)); // Hapus semua objek
            fallingObjects.clear();
        }
    }
}
