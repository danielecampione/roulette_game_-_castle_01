package it.campione.roulette;

import java.util.Random;

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

public class RouletteGameApp extends Application {

    private WebView outputWebView;
    private TextArea statsTextArea;
    private ComboBox<Integer> numberOfSpinsComboBox;
    private ComboBox<Integer> sufficientCapitalComboBox;
    private Random random;

    // Numeri per le scommesse specifiche
    private static final int[] THIRD_12 = { 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36 };
    private static final int[] FIRST_12 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    private static final int[] SECOND_12 = { 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 };

    // Numeri rossi e neri nella roulette
    private static final int[] RED_NUMBERS = { 1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36 };
    private static final int[] BLACK_NUMBERS = { 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35 };

    // Variabili di stato
    private int lastLossNumber = -1; // Memorizza l'ultimo numero che ha causato una perdita
    private boolean isBackupStrategyActive = false; // Indica se la strategia di backup è attiva

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Roulette Game - Castello Strategy");

        random = new Random();

        // WebView per l'output (supporta HTML)
        outputWebView = new WebView();

        // TextArea per le statistiche
        statsTextArea = new TextArea();
        statsTextArea.setEditable(false);
        statsTextArea.setWrapText(true);

        // Applica le animazioni all'avvio
        applyStartupAnimations(statsTextArea);

        // ComboBox per il numero di lanci
        numberOfSpinsComboBox = new ComboBox<>();
        for (int i = 1; i <= 5500; i++) {
            numberOfSpinsComboBox.getItems().add(i);
        }
        numberOfSpinsComboBox.getSelectionModel().select(99); // Imposta 100 come valore predefinito

        // ComboBox per il capitale minimo di vittoria
        sufficientCapitalComboBox = new ComboBox<>();
        sufficientCapitalComboBox.getItems().addAll(0, 25, 50, 60, 75, 90, 100, 150, 200);
        sufficientCapitalComboBox.getSelectionModel().selectFirst(); // Imposta 0 come valore predefinito

        // Pulsante per avviare la simulazione
        Button startButton = new Button("Avvia Simulazione");
        startButton.getStyleClass().add("button");
        startButton.setOnAction(e -> startSimulation());
        applyButtonEffects(startButton);

        // Layout
        VBox controlsBox = new VBox(10, new Label("Numero di lanci nella serie:"), numberOfSpinsComboBox,
                new Label("Capitale minimo di vittoria:"), sufficientCapitalComboBox, startButton);
        controlsBox.setPadding(new Insets(10));

        // Applica l'animazione alle ComboBox
        applyComboBoxAnimation(numberOfSpinsComboBox);
        applyComboBoxAnimation(sufficientCapitalComboBox);

        BorderPane root = new BorderPane();
        root.setCenter(outputWebView);
        root.setRight(controlsBox);
        root.setBottom(statsTextArea);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);

        // Gestione dell'evento di chiusura
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

        output.append("<html><body style='font-family: Courier New; font-size: 12px;'>");

        // Variabile per tenere traccia della strategia attuale
        String currentStrategy = "Castello";

        // Variabile per memorizzare il colore del numero estratto nel turno precedente
        String lastColor = "";

        for (int i = 0; i < numberOfSpins; i++) {
            int number = spinRoulette();
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
                // Scommetti sul colore opposto a lastColor
                String targetColor = lastColor.equals("Rosso") ? "Nero" : "Rosso";
                boolean isWin = getColor(number).equals(targetColor);

                if (isWin) {
                    result = 17; // Vincita netta: 8.50€ * 2 = 17€
                } else {
                    result = -8.50; // Perdita: -8.50€
                }

                strategy = "(Colore opposto)";
                if (result < 0) {
                    currentStrategy = "Castello"; // Ritorna a (Castello) se perde
                } else {
                    currentStrategy = "Castello"; // Ritorna a (Castello) se vince
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

        outputWebView.getEngine().loadContent(finalOutput);
        statsTextArea.setText(stats.toString());

        removeNeonEffect(statsTextArea);
    }

    private int spinRoulette() {
        return random.nextInt(37);
    }

    private double calculateBetResult(int number) {
        // Scommesse principali:
        // 0.50€ su 0
        // 0.50€ su 25-28 e "3rd 12"
        // 0.50€ su 31-34 e "3rd 12"
        // 5€ su "1st 12"
        // 5€ su "2nd 12"

        double totalWin = 0;

        if (number == 0) {
            // Vincita su 0 (payout 35:1)
            totalWin += 0.50 * 35; // 17.50€
        } else if (contains(FIRST_12, number)) {
            // Vincita su "1st 12" (payout 3:1)
            totalWin += 5 * 3; // 15€
            // Sottrai solo le scommesse perdenti
            totalWin -= 0.50; // Scommessa su 0
            totalWin -= 0.50; // Scommessa su 25-28 e "3rd 12"
            totalWin -= 0.50; // Scommessa su 31-34 e "3rd 12"
            totalWin -= 5; // Scommessa su "2nd 12" (perdita)
        } else if (contains(SECOND_12, number)) {
            // Vincita su "2nd 12" (payout 3:1)
            totalWin += 5 * 3; // 15€
            // Sottrai solo le scommesse perdenti
            totalWin -= 0.50; // Scommessa su 0
            totalWin -= 0.50; // Scommessa su 25-28 e "3rd 12"
            totalWin -= 0.50; // Scommessa su 31-34 e "3rd 12"
            totalWin -= 5; // Scommessa su "1st 12" (perdita)
        } else if (contains(THIRD_12, number)) {
            // Vincita su "3rd 12" (payout 2:1)
            totalWin += 0.50 * 2; // 1€
            // Sottrai tutte le altre scommesse
            totalWin -= 0.50; // Scommessa su 0
            totalWin -= 5; // Scommessa su "1st 12"
            totalWin -= 5; // Scommessa su "2nd 12"
        }

        return totalWin; // Ritorna il guadagno netto senza arrotondamenti
    }

    private String getSymbol(double result) {
        if (result > 0) {
            return "."; // Vittoria
        } else {
            return "X"; // Sconfitta
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