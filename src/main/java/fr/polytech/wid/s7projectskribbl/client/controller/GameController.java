package fr.polytech.wid.s7projectskribbl.client.controller;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.ChatMessagePayload;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Pos;

import java.util.List;

public class GameController {

    // COULEUR DU BOUTON ACTIF
    private final String ACTIVE_COLOR_STYLE = "-fx-background-color: #e59700;";

    // RADIUS
    private final CornerRadii RADIUS_ROUND = new CornerRadii(50, true); // Pour outils ronds
    private final CornerRadii RADIUS_STD = new CornerRadii(4);
    public StackPane canvasContainer;

    // ---- CHAT ----
    @FXML private TextField chatInput;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPaneMessages;
    @FXML private Button sendChat;

    // ---- INFO JEU ----
    @FXML private Label statusLabel;            //présent dans le prototype mais peut être à enlever
    @FXML private Label mysteryWord;
    @FXML private Label timer;
    @FXML private Label currentRound;
    @FXML private Label roundNumber;
    @FXML private FlowPane playerListContainer;


    // ---- OUTILS DESSIN ----
    @FXML private Canvas drawingCanvas;
    @FXML private VBox mainContainer;
    @FXML private VBox toolbox;


    // ---- TOOLBOX BUTTONS ----
    @FXML private Button btnBrush;
    @FXML private Button btnEraser;
    @FXML private Button btnClear;

    @FXML private Button btnQuit ;

    // ---- POPUP FIN DE ROUND ----
    @FXML private VBox overlayEndRound;
    @FXML private Label endRoundTitle;
    @FXML private Label endRoundWord;

    // --- POPUP FIN DE JEU ---
    @FXML private HBox endGameButtons;
    @FXML private Button btnReplay;
    @FXML private Button btnEndQuit;

    // ---- CONFIGURATION TAILLE & COULEUR (BRUSH) ----
    @FXML private MenuButton menuTaille;
    @FXML private Slider sliderTaille;
    @FXML private Circle circleTailleIcone;
    @FXML private ColorPicker colorPicker;


    private GraphicsContext gc; // L'outil de dessin

    // GESTION DE L'ETAT DES BOUTONS
    private Button currentActiveButton;
    private boolean isEraserActive = false;

    // VARIABLE DE GESTION DU RÔLE
    private boolean isDrawer = true;

    // ON STOCKE LE MOT MYSTERE
    private String WORD;

    private static GameController instance;

    public static GameController Instance()
    {
        return instance;
    }

    @FXML
    private void initialize(){

        instance = this;
        // permet de cliquer au travers de la toolbox (pour atteindre le canvas)
        if (toolbox != null){
            toolbox.setPickOnBounds(false);
        }


        // Initialisation du gc
        // On récupère l'outil de dessin du canvas
        gc = drawingCanvas.getGraphicsContext2D();
        initDrawingSettings();
        initDrawingEvents();

        // Initialisation des outils
        initToolButtons();

        // Initialisation du Chat
        initChatSystem();

        // Initialisation des boutons Replay/Quit
        initEndButtons();

        // Initialisation du bouton Quit
        if(btnQuit != null)
        {
            btnQuit.setOnAction(e -> {
                System.out.println("Quitting game...");
                Platform.exit();
                System.exit(0);
            });
        }

        Platform.runLater(() -> {

            if(mysteryWord != null)
            {
                WORD = mysteryWord.getText();

            }

            applyAnimations();

            // On définit le pinceau comme actif au démarrage
            setActiveTool(btnBrush);


            // --- SIMULATION POUR LE TEST ---

            // On simule que c'est le round 5 sur 5 (FIN DE PARTIE)
            int roundActuel = 4;
            int roundTotal = 5;

            // Mise à jour des labels (pour l'affichage)
            if(currentRound != null) currentRound.setText(String.valueOf(roundActuel));
            if(roundNumber != null) roundNumber.setText(String.valueOf(roundTotal));


            // CAS DESSINATEUR
            updateGameState(isDrawer, WORD);
            showRoundEnd(isDrawer, WORD, roundActuel, roundTotal, true);    //test quand tt le monde trouve
            UpdatePlayerList();
        });
    }

    public void UpdatePlayerList() {
        // On récupère la liste fraîche depuis le ClientHandler
        List<ClientImage> clients = ClientHandler.Singleton().ClientImages();

        playerListContainer.getChildren().clear();

        for (ClientImage player : clients) {
            VBox card = createGamePlayerCard(player);
            playerListContainer.getChildren().add(card);
        }
    }

    private VBox createGamePlayerCard(ClientImage player) {
        VBox card = new VBox();

        // --- STYLE & TAILLE FIXE ---
        card.getStyleClass().add("userBox");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setSpacing(5);

        // Fixe la taille de la carte (Responsive mais contraint)
        card.setPrefWidth(160);
        card.setPrefHeight(100);
        card.setMinWidth(160);
        card.setMinHeight(100);

        // --- PSEUDO ---
        Label nameLabel = new Label(player.Username());
        nameLabel.getStyleClass().add("orangeText");
        nameLabel.setStyle("-fx-font-size: 18px;"); // Un peu plus petit pour rentrer

        // --- PLACEHOLDER STATUT (Trouvé / Dessine) ---
        // "Plus tard tu remplaceras cette condition par player.HasFoundWord()"
        String statusText = player.IsDrawer() ? "Drawing..." : "Guessing";
        Label statusLabel = new Label(statusText);
        statusLabel.getStyleClass().add("grayText");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");

        // --- PLACEHOLDER POINTS ---
        HBox scoreBox = new HBox(5);
        scoreBox.setAlignment(Pos.CENTER);

        // "Plus tard tu remplaceras '0' par player.GetScore()"
        Label pointsLabel = new Label("0");
        pointsLabel.getStyleClass().add("whiteText");
        pointsLabel.setStyle("-fx-font-size: 16px;");

        Label ptsSuffix = new Label("pts");
        ptsSuffix.getStyleClass().add("grayText");
        ptsSuffix.setStyle("-fx-font-size: 14px;");

        scoreBox.getChildren().addAll(pointsLabel, ptsSuffix);

        // Assemblage
        card.getChildren().addAll(nameLabel, statusLabel, scoreBox);

        return card;
    }

    // Configure les paramètres de base de la brush (taille, couleur)
    private void initDrawingSettings(){
        //brush ronde
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        // donne taille et la couleur de base
        gc.setLineWidth(sliderTaille.getValue());
        gc.setStroke(Color.BLACK);

        // Initialise la couleur du ColorPicker à noir
        colorPicker.setValue(Color.BLACK);

    }

    // Gère les événements Souris sur le Canvas
    private void initDrawingEvents(){
        // nouveau tracé quand la souris est cliquée
        drawingCanvas.setOnMousePressed(e -> {

            // Ne peut pas dessiner si l'on est pas le dessinateur
            if(!isDrawer)
            {
                return;
            }

            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        });

        // Dessine quand la souris est glissée
        drawingCanvas.setOnMouseDragged(e -> {

            if(!isDrawer)
            {
                return;
            }

            gc.lineTo(e.getX(), e.getY());
            gc.stroke();


            // TODO , il faut envoyer les coordonnées au serveur

        });


    }

    // Gère les boutons de la toolbox
    private void initToolButtons(){

        // on définit une taille par défaut pour la brush
        gc.setLineWidth(4);
        double firstRad = Math.min(15, Math.max(2, 4/3));
        circleTailleIcone.setRadius(firstRad);

        // --- SLIDER (taille brush) ---
        sliderTaille.valueProperty().addListener((obs, oldVal, newVal) -> {
            double taille = newVal.doubleValue();
            gc.setLineWidth(taille);

            //rétrécissement de la taille du cercle
            double radius = Math.min(15, Math.max(2, taille / 3));
            circleTailleIcone.setRadius(radius);
        });

        // --- COLOR PICKER ---
        colorPicker.setOnAction(e -> {
            isEraserActive = false; // changement de couleur -> repasser au mode brush
            gc.setStroke(colorPicker.getValue());

        });

        // --- BOUTON BRUSH ---
        btnBrush.setOnAction(e -> {
            isEraserActive = false;
            gc.setStroke(colorPicker.getValue());
            setActiveTool(btnBrush);

        });

        // --- BOUTON GOMME ---
        btnEraser.setOnAction(e -> {
            isEraserActive = true;
            gc.setStroke(Color.WHITE);
            setActiveTool(btnEraser);

        });

        // --- BOUTON CLEAR ---
        btnClear.setOnAction(e -> {
            // Efface tout le rectangle du canvas
            gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        });


    }

    // Change visuellement le bouton actif
    private void setActiveTool(Button newBtn){
        if (currentActiveButton == newBtn)
        {
            return;
        }

        //Réinitialiser la couleur de l'ancien bouton actif
        if (currentActiveButton != null) {
            currentActiveButton.setStyle("");
        }

        // mise à jour du bouton actif
        currentActiveButton = newBtn;

        // Appliquer la nouvelle couleur au nouveau bouton actif
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(ACTIVE_COLOR_STYLE);
        }

    }


    // Gère l'envoi et l'affichage des messages
    private void initChatSystem()
    {
        // action sur le bouton "Chat"
        sendChat.setOnAction(e -> handleSendAction());

        // action quand on appuie sur Entrée dans l'input de texte
        chatInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
            {
                handleSendAction();
            }
        });
    }


    @FXML
    private void handleSendAction()
    {
        String message = chatInput.getText().trim();

        if (message.isEmpty())
        {
            return;
        }

        chatInput.clear();

        ChatMessagePayload chatMessagePayload = new ChatMessagePayload(ClientHandler.Singleton().ID(), message);
        ClientHandler.Singleton().Out().SendCommand(CommandCode.CHAT_MESSAGE_SENT, chatMessagePayload);
    }

    public void AddMessageToChat(String sender, String message)
    {
        Platform.runLater(() -> {
            Internal_AddMessageToChat(sender, message);
        });
    }

    /**
     * Ajoute un message système indiquant qu'un joueur a quitté la partie.
     * @param username Le pseudo du joueur déconnecté.
     */
    public void AddDisconnectionMessage(String username)
    {
        Platform.runLater(() -> {
            HBox msgBox = new HBox();

            // Création du label avec le message complet
            Label lblMsg = new Label(username + " a quitté la partie.");

            // Application du style défini dans le CSS
            lblMsg.getStyleClass().add("chatDisconnect");
            lblMsg.setWrapText(true); // Permet le retour à la ligne si le pseudo est long

            msgBox.getChildren().add(lblMsg);
            messagesContainer.getChildren().add(msgBox);

            // Auto Scroll vers le bas pour voir le message
            scrollPaneMessages.setVvalue(1.0);
        });
    }

    private void Internal_AddMessageToChat(String user, String text)
    {
        HBox msgBox = new HBox(5); // espacement de 5px entre le pseudo et le message

        Label lblUser = new Label(user + ": ");
        lblUser.getStyleClass().add("chatUser");    // lui ajoute la classe "chatUser" pour le css

        Label lblText = new Label(text);
        lblText.getStyleClass().add("chatText");
        lblText.setWrapText(true); // le message passe à la ligne si trop long

        msgBox.getChildren().addAll(lblUser, lblText);
        messagesContainer.getChildren().add(msgBox);

        // Auto Scroll pour voir le dernier message à chaque fois
        Platform.runLater(() -> scrollPaneMessages.setVvalue(1.0));

    }


    private void applyAnimations() {

        Button[] buttonsToAnimate = { btnBrush, btnEraser, btnClear, sendChat };

        Color baseColor = Color.web("#f0c50d");
        Color hoverColor = Color.web("#e59700");

        //radius du bouton chat
        CornerRadii chatRadius = new CornerRadii(0, 13, 13,0, false);

        if (sendChat != null) {
            sendChat.hoverProperty().addListener((obs, oldState, isHovered) -> {
                if (isHovered) {
                    animateColor(sendChat, baseColor, hoverColor, chatRadius);
                } else {
                    animateColor(sendChat, hoverColor, baseColor, chatRadius);
                }
            });
        }


    }


    private void animateColor(Button btn, Color oldColor, Color newColor, CornerRadii radii){

        final Transition transition = new Transition() {
            {
                setCycleDuration(Duration.millis(200));
                setInterpolator(Interpolator.EASE_OUT);
            }

            @Override
            protected void interpolate(double v) {
                Color currentCol = oldColor.interpolate(newColor, v);
                btn.setBackground(new Background(new BackgroundFill(
                        currentCol,
                        radii,
                        new Insets(0)
                )));
            }
        };
        transition.play();

    }


    // Affiche le pop up en fin de round
    public void showRoundEnd(boolean amIdrawing, String realWord, int currentR, int totalR, boolean everyoneFound)
    {

        boolean isGameOver = (currentR >= totalR);

        // Cas de fin de partie
        if (isGameOver) {
            endRoundTitle.setText("GAME FINISHED !");
            endRoundWord.setText("The Winner is ...");
            endRoundWord.setVisible(true);
            endRoundWord.setManaged(true);

            // Afficher les boutons relancer/quitter
            if (endGameButtons != null) {
                endGameButtons.setVisible(true);
                endGameButtons.setManaged(true);
            }

        }

        // Cas où tout le monde trouve
        else if (everyoneFound)
        {
            if(endGameButtons != null)
            {
                endGameButtons.setVisible(false);
                endGameButtons.setManaged(false);
            }

            endRoundTitle.setText("Everyone found the word !");
            endRoundWord.setText("Perfect !");

            // On affiche ce message pour tout le monde
            endRoundWord.setVisible(true);
            endRoundWord.setManaged(true);
        }


        // Cas de temps écoulé (pas le dernier round)
        else {

            // Cache les boutons
            if (endGameButtons != null) {
                endGameButtons.setVisible(false);
                endGameButtons.setManaged(false);
            }

            if (amIdrawing) {
                endRoundTitle.setText("Time is up !");
                endRoundWord.setVisible(false);
                endRoundWord.setManaged(false);
            } else {
                endRoundTitle.setText("The word was :");
                endRoundWord.setText(realWord.toUpperCase());
                endRoundWord.setVisible(true);
                endRoundWord.setManaged(true);
            }
        }

        // Rend le pop up visible
        overlayEndRound.setVisible(true);

        // Passe le pop up au 1er plan
        overlayEndRound.toFront();





    }

    // Cache le pop up (pour le début des rounds)
    public void hideRoundEnd(){
        overlayEndRound.setVisible(false);

    }



    // Met à jour l'interface selon le rôle du joueur
    private void updateGameState(boolean amIdrawing, String word)
    {
        this.isDrawer = amIdrawing;

        // GESTION TOOLBOX
        if (toolbox != null)
        {
            toolbox.setVisible(isDrawer); // cache/affiche la toolbox
            toolbox.setDisable(!isDrawer);
        }

        // GESTION DU MOT MYSTERE
        if (amIdrawing)
        {
            // si je dessine, le mot s'affiche
            mysteryWord.setText(word);
        }
        else
        {
            // si je devine, chaque lettre est remplacée par "_"
            String hiddenWord = word.replaceAll("[^ ]", "_ ");
            mysteryWord.setText(hiddenWord);
        }


        hideRoundEnd();




    }

    // Gestion des boutons de fin de jeu
    private void initEndButtons() {
        if (btnEndQuit != null)
        {
            btnEndQuit.setOnAction(e -> quitGame());
        }

        if (btnReplay != null)
        {
            btnReplay.setOnAction(e -> restartGame());
        }
    }


    private void quitGame()
    {
        try
        {
            ClientHandler.Singleton().Disconnect();
        }
        catch (Exception e)
        {

        }
        Platform.exit();
        System.exit(0);
    }

    private void restartGame()
    {
        System.out.println("Relancement de la partie...");
        hideRoundEnd();
        gc.clearRect(0,0,drawingCanvas.getWidth(), drawingCanvas.getHeight());
        messagesContainer.getChildren().clear();

        if (currentRound != null)
        {
            currentRound.setText("1");
        }

        updateGameState(isDrawer, WORD);
    }
}




