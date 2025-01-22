package it.campione.roulette;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Castle Method: {
 * You bet 0.50 EUR on 0.
 * You bet 5 EUR on the "1st 12" sector.
 * You bet 5 EUR on the "2nd 12" sector.
 * You bet 0.50 EUR on the "3rd 12" sector straddling the two pairs at the
 * bottom "25 and 28" and "31 and 34" (0.50 EUR and 0.50 EUR respectively). In
 * total, therefore, 11.50 EUR is staked.
 * }
 * 
 * The "Opposite Color" method takes over in the event of a "loss" with the
 * "Castle" method, i.e. when the gain with the "Castle" method decreases the
 * total gain despite the victory. "Opposite Color" mode: {
 * You bet 8.50 EUR on the color opposite to that of the number that was drawn
 * just before (if a red number belonging to the third sector "3rd 12" had just
 * been drawn, you would now bet on the opposite color, i.e. the color black).
 * Both in case of victory and defeat, the "Castle" method is applied again and
 * so on.
 * }
 * 
 * For more details see the video https://www.youtube.com/watch?v=VPmbUqGtrOY
 */
public class RouletteGameApp extends Application {

    Roulette roulette;
    private WebView outputWebView;
    private TextArea statsTextArea;
    private ComboBox<Integer> numberOfSpinsComboBox;
    private ComboBox<Integer> sufficientCapitalComboBox;

    // Numbers for specific bets
    private static final int[] THIRD_12 = { 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36 };
    private static final int[] FIRST_12 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    private static final int[] SECOND_12 = { 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 };

    // Red and black numbers in roulette
    private static final int[] RED_NUMBERS = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
    private static final int[] BLACK_NUMBERS = { 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35 };

    // State variables
    private int lastLossNumber = -1; // Stores the last number that caused a leak
    private boolean isBackupStrategyActive = false; // Indicates whether the backup strategy is active

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Roulette Game - Castello Strategy");

        roulette = new Roulette();

        // WebView for output (supports HTML)
        outputWebView = new WebView();

        // TextArea for statistics
        statsTextArea = new TextArea();
        statsTextArea.setEditable(false);
        statsTextArea.setWrapText(true);

        // Apply animations on startup
        applyStartupAnimations(statsTextArea);

        // ComboBox for the number of launches
        numberOfSpinsComboBox = new ComboBox<>();
        for (int i = 1; i <= 5500; i++) {
            numberOfSpinsComboBox.getItems().add(i);
        }
        numberOfSpinsComboBox.getSelectionModel().select(99); // Set 100 as the default value

        // ComboBox for the minimum win capital
        sufficientCapitalComboBox = new ComboBox<>();
        sufficientCapitalComboBox.getItems().addAll(0, 25, 50, 60, 75, 90, 100, 150, 200);
        sufficientCapitalComboBox.getSelectionModel().selectFirst(); // Set 0 as the default value

        // Button to start simulation
        Button startButton = new Button("Avvia Simulazione");
        startButton.getStyleClass().add("button");
        startButton.setOnAction(e -> startSimulation());
        applyButtonEffects(startButton);

        // Layout
        VBox controlsBox = new VBox(10, new Label("Numero di lanci nella serie:"), numberOfSpinsComboBox,
                new Label("Capitale minimo di vittoria:"), sufficientCapitalComboBox, startButton);
        controlsBox.setPadding(new Insets(10));

        // Apply animation to ComboBoxes
        applyComboBoxAnimation(numberOfSpinsComboBox);
        applyComboBoxAnimation(sufficientCapitalComboBox);

        BorderPane root = new BorderPane();
        root.setCenter(outputWebView);
        root.setRight(controlsBox);
        root.setBottom(statsTextArea);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);

        // Closing event management
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            applyExitAnimations(primaryStage);
        });

        primaryStage.show();
    }

    private void applyStartupAnimations(TextArea textArea) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), textArea);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), textArea);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000), textArea);
        rotateTransition.setByAngle(360);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition,
                rotateTransition);
        parallelTransition.play();
    }

    private void applyExitAnimations(Stage primaryStage) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), primaryStage.getScene().getRoot());
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.5);
        scaleTransition.setToY(0.5);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000),
                primaryStage.getScene().getRoot());
        rotateTransition.setByAngle(360);

        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition,
                rotateTransition);
        parallelTransition.setOnFinished(e -> primaryStage.close());
        parallelTransition.play();
    }

    private void startSimulation() {
        addNeonEffect(statsTextArea);

        outputWebView.getEngine().loadContent("");
        statsTextArea.clear();

        int numberOfSpins = numberOfSpinsComboBox.getValue();
        int sufficientCapital = sufficientCapitalComboBox.getValue();
        double totalProfitLoss = 0;
        double maxProfit = Double.MIN_VALUE;
        StringBuilder output = new StringBuilder();
        StringBuilder stats = new StringBuilder();
        String maxProfitLine = "";
        int maxProfitIndex = -1;

        output.append(
                "<html><head><meta charset='UTF-8'></head><body style='font-family: Courier New; font-size: 12px;'>");

        // Variable to keep track of the current strategy
        String currentStrategy = "Castello";

        // Variable to store the color of the number drawn in the previous round
        String lastColor = "";

        for (int i = 0; i < numberOfSpins; i++) {
            int number = roulette.spin();
            double result = 0;
            String strategy = "";

            switch (currentStrategy) {
            case "Castello":
                result = calculateBetResult(number);
                strategy = "(Castello)";
                if (result < 0) {
                    currentStrategy = "Colore opposto"; // Passa a (Colore opposto) al primo fallimento
                    lastColor = getColor(number); // Memorizza il colore del numero estratto
                }
                break;

            case "Colore opposto":
                // Bet on the opposite color to lastColor
                String targetColor = lastColor.equals("Rosso") ? "Nero" : "Rosso";
                boolean isWin = getColor(number).equals(targetColor);

                if (isWin) {
                    result = 17; // Net Payout: €8.50 * 2 = €17
                } else {
                    result = -8.50; // Loss: -8.50€
                }

                strategy = "(Colore opposto)";
                if (result < 0) {
                    currentStrategy = "Castello"; // Return to (Castle) if he loses
                } else {
                    currentStrategy = "Castello"; // Return to (Castle) if he wins
                }
                break;
            }

            totalProfitLoss += result;

            if (totalProfitLoss > maxProfit) {
                maxProfit = totalProfitLoss;
                maxProfitIndex = i;
            }

            String color = getColor(number);
            String parity = getParity(number);
            String range = getRange(number);
            String situation = getSituation(result);
            String profitLoss = (result >= 0) ? "Guadagno: " + result + "€" : "Perdita: " + Math.abs(result) + "€";

            String line = getSymbol(result) + " " + number + " | Colore: " + color + " | Parità: " + parity
                    + " | Range: " + range + " | Situazione: " + situation + " | " + profitLoss + " | Totale: "
                    + totalProfitLoss + "€ " + strategy + "<br>";

            if (totalProfitLoss == maxProfit) {
                maxProfitLine = line;
            }

            if (totalProfitLoss < 0) {
                output.append("<span style='color:red;'>").append(line).append("</span>");
            } else if (sufficientCapital > 0 && totalProfitLoss >= sufficientCapital) {
                output.append("<span style='color:blue;'>").append(line).append("</span>");
            } else {
                output.append("<span style='color:black;'>").append(line).append("</span>");
            }
        }

        output.append("</body></html>");

        stats.append("Massimo guadagno raggiunto: ").append(maxProfit).append("€\n");
        stats.append("Posizione del massimo guadagno: ").append(maxProfitIndex + 1).append("\n");
        stats.append("Profitto/Perdita totale: ").append(totalProfitLoss).append("€\n");

        String highlightedLine = "<span style='background-color: #F0E68C; font-weight: bold; color: black;'>"
                + maxProfitLine + "</span>";
        String finalOutput = output.toString().replace(maxProfitLine, highlightedLine);

        // Carica il contenuto con codifica UTF-8
        outputWebView.getEngine().loadContent(finalOutput);
        statsTextArea.setText(stats.toString());

        removeNeonEffect(statsTextArea);
    }

    private double calculateBetResult(int number) {
        double totalWin = 0;

        if (number == 0) {
            // Win on 0 (payout 35:1)
            totalWin += 0.50 * 35; // 17.50€
            // Subtract losses from other bets
            totalWin -= 5; // Bet on "1st 12" (Loss)
            totalWin -= 5; // Bet on "2nd 12" (Loss)
            totalWin -= 0.50; // Bet on 25-28 and "3rd 12" (loss)
            totalWin -= 0.50; // Bet on 31-34 and "3rd 12" (loss)
        } else if (contains(FIRST_12, number)) {
            // Win on "1st 12" (payout 3:1)
            totalWin += 5 * 3; // 15€
            // Subtract only losing bets
            totalWin -= 0.50; // Bet on 0
            totalWin -= 0.50; // Bet on 25-28 and "3rd 12"
            totalWin -= 0.50; // Bet on 31-34 and "3rd 12"
            totalWin -= 5; // Bet on "2nd 12" (Loss)
        } else if (contains(SECOND_12, number)) {
            // Win on "2nd 12" (payout 3:1)
            totalWin += 5 * 3; // 15€
            // Subtract only losing bets
            totalWin -= 0.50; // Bet on 0
            totalWin -= 0.50; // Bet on 25-28 and "3rd 12"
            totalWin -= 0.50; // Bet on 31-34 and "3rd 12"
            totalWin -= 5; // Bet on "1st 12" (Loss)
        } else if (contains(THIRD_12, number)) {
            // Win on "3rd 12" (payout 2:1)
            totalWin += 0.50 * 2; // 1€
            // Subtract all other bets
            totalWin -= 0.50; // Bet on 0
            totalWin -= 5; // Bet on "1st 12"
            totalWin -= 5; // Bet on "2nd 12"
        }

        return totalWin; // Returns net gain without rounding
    }

    private String getSymbol(double result) {
        if (result > 0) {
            return "."; // Victory
        } else {
            return "X"; // Defeat
        }
    }

    private String getColor(int number) {
        if (number == 0) {
            return "Verde";
        } else if (contains(RED_NUMBERS, number)) {
            return "Rosso";
        } else if (contains(BLACK_NUMBERS, number)) {
            return "Nero";
        }
        return "N/A";
    }

    private String getParity(int number) {
        if (number == 0) {
            return "N/A";
        } else if (number % 2 == 0) {
            return "Pari";
        } else {
            return "Dispari";
        }
    }

    private String getRange(int number) {
        if (number == 0) {
            return "N/A";
        } else if (number >= 1 && number <= 18) {
            return "Basso";
        } else {
            return "Alto";
        }
    }

    private String getSituation(double result) {
        if (result > 0) {
            return "Vittoria";
        } else {
            return "Perdita";
        }
    }

    private boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }

    private void applyButtonEffects(Button button) {
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: #45a049; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 10, 0, 0, 5); -fx-cursor: hand;");
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
        });
        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 10 20; -fx-font-size: 14px;");
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
        button.setOnMousePressed(e -> {
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(100), button);
            rotateTransition.setByAngle(5);
            rotateTransition.setCycleCount(2);
            rotateTransition.setAutoReverse(true);
            rotateTransition.play();
        });
    }

    private void applyComboBoxAnimation(ComboBox<?> comboBox) {
        comboBox.setOnAction(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), comboBox);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);
            scaleTransition.play();
        });
    }

    private void addNeonEffect(TextArea textArea) {
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.TRANSPARENT);

        textArea.setEffect(innerShadow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.colorProperty(), Color.TRANSPARENT)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.colorProperty(), Color.BLUE)));
        timeline.play();
    }

    private void removeNeonEffect(TextArea textArea) {
        InnerShadow innerShadow = (InnerShadow) textArea.getEffect();

        if (innerShadow == null) {
            innerShadow = new InnerShadow();
            innerShadow.setColor(Color.BLUE);
            textArea.setEffect(innerShadow);
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(innerShadow.colorProperty(), Color.BLUE)),
                new KeyFrame(Duration.seconds(1), new KeyValue(innerShadow.colorProperty(), Color.TRANSPARENT)));
        timeline.setOnFinished(e -> {
            textArea.setEffect(null);
            textArea.setStyle("");
        });
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}