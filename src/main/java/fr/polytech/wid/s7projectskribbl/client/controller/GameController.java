package fr.polytech.wid.s7projectskribbl.client.controller;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.ChatMessagePayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.DrawPayload;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.animation.AnimationTimer;
import fr.polytech.wid.s7projectskribbl.common.payloads.DrawPayload;

public class GameController
{

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
    @FXML private Label mysteryWord;
    @FXML private Label timer;
    @FXML private Label currentRound;
    @FXML private Label roundNumber;
    @FXML private FlowPane playerListContainer;


    // ---- OUTILS DESSIN ----
    @FXML private Canvas drawingCanvas;
    @FXML private VBox mainContainer;
    @FXML private VBox toolbox;
    @FXML private Pane canvasWrapper;
    @FXML private Group canvasScaler;

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

    private final Queue<DrawPayload> drawQueue = new ConcurrentLinkedQueue<>();
    private AnimationTimer drawingLoop;


    private static final double BASE_WIDTH = 1280.0;
    private static final double BASE_HEIGHT = 720.0;

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

        fillCanvasWithWhite();

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

            if (mysteryWord != null)
            {
                WORD = mysteryWord.getText();
            }

            applyAnimations();
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
            //showRoundEnd(isDrawer, WORD, roundActuel, roundTotal, true);    //test quand tt le monde trouve
            UpdatePlayerList();
        });

        startDrawingLoop();
        initResponsiveCanvas();
    }

    private void startDrawingLoop() {
        drawingLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                processDrawQueue();
            }
        };
        drawingLoop.start();
    }

    private void processDrawQueue() {
        while (!drawQueue.isEmpty()) {
            DrawPayload payload = drawQueue.poll(); // Récupère et retire l'élément
            if (payload != null) {
                Internal_ApplyDrawAction(payload);
            }
        }
    }

    public void ApplyDrawAction(DrawPayload payload) {
        if (payload != null) {
            drawQueue.add(payload);
        }
    }

    private void Internal_ApplyDrawAction(DrawPayload payload) {
        if (gc == null) return;

        // 1. Configuration du contexte graphique
        try {
            Color color = Color.web(payload.Color());
            gc.setStroke(color);
            gc.setFill(color);
        } catch (Exception e) {
            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(payload.Size());

        double sX = toScreenX(payload.X());
        double sY = toScreenY(payload.Y());

        // 3. Exécution de l'action
        switch (payload.Action())
        {
            case START:
                gc.beginPath();
                gc.moveTo(sX, sY);
                gc.stroke(); // Point
                break;

            case DRAG:
                gc.lineTo(sX, sY);
                gc.stroke();
                break;

            case FILL:
                // Appel à votre algorithme de remplissage
                // BucketFill(sX, sY, (Color) gc.getStroke());
                // Pour l'instant, on fait un remplissage simple si BucketFill n'est pas prêt :
                gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
                break;
            case CLEAR:
                fillCanvasWithWhite();
                break;
        }
    }

    // --- Méthodes de conversion nécessaires si pas déjà présentes ---
    private double toScreenX(double virtualX) {
        return virtualX * (drawingCanvas.getWidth() / BASE_WIDTH);
    }

    private double toScreenY(double virtualY) {
        return virtualY * (drawingCanvas.getHeight() / BASE_HEIGHT);
    }

    private void initResponsiveCanvas() {
        canvasWrapper.prefWidthProperty().bind(canvasContainer.widthProperty());
        canvasWrapper.prefHeightProperty().bind(canvasContainer.heightProperty());

        Rectangle clipRect = new Rectangle();

        clipRect.widthProperty().bind(canvasWrapper.widthProperty());
        clipRect.heightProperty().bind(canvasWrapper.heightProperty());

        canvasWrapper.setClip(clipRect);

        canvasWrapper.widthProperty().addListener((obs, oldVal, newVal) -> fitCanvas());
        canvasWrapper.heightProperty().addListener((obs, oldVal, newVal) -> fitCanvas());

        fitCanvas();
    }

    // Permet de garder le format 16/9
    private void fitCanvas()
    {
        double availableWidth = canvasWrapper.getWidth();
        double availableHeight = canvasWrapper.getHeight();

        if (availableWidth == 0 || availableHeight == 0) return;

        double scaleX = availableWidth / BASE_WIDTH;
        double scaleY = availableHeight / BASE_HEIGHT;

        double scaleFactor = Math.min(scaleX, scaleY);

        canvasScaler.setScaleX(scaleFactor);
        canvasScaler.setScaleY(scaleFactor);

        canvasScaler.setLayoutX((availableWidth - BASE_WIDTH) / 2);
        canvasScaler.setLayoutY((availableHeight - BASE_HEIGHT) / 2);
    }


    // Colore le canvas en blanc
    private void fillCanvasWithWhite() {

        Paint oldFill = gc.getFill();

        gc.setFill(Color.WHITE);

        gc.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);

        gc.setFill(oldFill);
    }

    public void UpdatePlayerList() { Platform.runLater(this::Internal_UpdatePlayerList); }

    private void Internal_UpdatePlayerList()
    {
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
// Gère les événements Souris sur le Canvas
    private void initDrawingEvents() {
        // 1. CLIC SOURIS (Début du tracé)
        drawingCanvas.setOnMousePressed(e -> {
            if (!isDrawer) return;

            // Dessin local (Feedback immédiat)
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();

            // Préparation et envoi au serveur
            sendDrawCommand(DrawPayload.DrawAction.START, e.getX(), e.getY());
        });

        // 2. GLISSEMENT SOURIS (Continuité du trait)
        drawingCanvas.setOnMouseDragged(e -> {
            if (!isDrawer) return;

            // Dessin local
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();

            // Préparation et envoi au serveur
            sendDrawCommand(DrawPayload.DrawAction.DRAG, e.getX(), e.getY());
        });
    }

    /**
     * Méthode utilitaire pour construire et envoyer le paquet de dessin.
     */
    private void sendDrawCommand(DrawPayload.DrawAction action, double mouseX, double mouseY) {
        // Conversion Coordonnées Écran -> Virtuel (1280x720)
        double vX = toVirtualX(mouseX);
        double vY = toVirtualY(mouseY);

        // Récupération Taille & Couleur
        double size = sliderTaille.getValue();
        String hexColor = toHexString((Color) gc.getStroke());

        // Création du Payload
        DrawPayload payload = new DrawPayload(action, vX, vY, hexColor, size);

        // Envoi
        ClientHandler.Singleton().Out().SendCommand(CommandCode.DRAW_ACTION, payload);
    }

    // --- FONCTIONS UTILITAIRES A AJOUTER A LA FIN DE LA CLASSE ---

    private double toVirtualX(double screenX) {
        if (drawingCanvas.getWidth() == 0) return 0;
        return screenX * (BASE_WIDTH / drawingCanvas.getWidth());
    }

    private double toVirtualY(double screenY) {
        if (drawingCanvas.getHeight() == 0) return 0;
        return screenY * (BASE_HEIGHT / drawingCanvas.getHeight());
    }

    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
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
            fillCanvasWithWhite();
            sendDrawCommand(DrawPayload.DrawAction.CLEAR, 0, 0);
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
        fillCanvasWithWhite();
        messagesContainer.getChildren().clear();

        if (currentRound != null)
        {
            currentRound.setText("1");
        }

        updateGameState(isDrawer, WORD);
    }
}




