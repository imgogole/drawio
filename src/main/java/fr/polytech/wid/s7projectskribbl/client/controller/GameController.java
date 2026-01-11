package fr.polytech.wid.s7projectskribbl.client.controller;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.ChatMessagePayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.DrawPayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.NTDecisionResultPayload;
import javafx.animation.*;
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
import javafx.scene.media.AudioClip;

import fr.polytech.wid.s7projectskribbl.common.payloads.DrawPayload;
import javafx.animation.Interpolator;
import javafx.scene.control.ProgressIndicator;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;

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

    // ---- SELECTION DE MOTS ----
    @FXML private StackPane overlayWordSelection; // Le fond noir
    @FXML private VBox wordSelectionContent;      // Le contenu animé
    @FXML private Button btnWord1;
    @FXML private Button btnWord2;
    @FXML private Button btnWord3;

    @FXML private StackPane overlayDecisionWait;
    @FXML private Label lblDecisionWait;

    @FXML private Label wordTitle;

    private AudioClip messageSound;

    // ---- TIMER ----
    private Timeline roundTimer; // L'objet qui gère le décompte
    private int remainingTimeSeconds; // Le temps restant en entier

    private final Queue<DrawPayload> drawQueue = new ConcurrentLinkedQueue<>();
    private AnimationTimer drawingLoop;

    private static final double BASE_WIDTH = 1280.0;
    private static final double BASE_HEIGHT = 720.0;

    private GraphicsContext gc; // L'outil de dessin

    // GESTION DE L'ETAT DES BOUTONS
    private Button currentActiveButton;
    private boolean isEraserActive = false;

    // VARIABLE DE GESTION DU RÔLE
    private boolean isDrawer = false;
    private boolean canDraw = false;

    public boolean CanDraw()
    {
        return isDrawer && canDraw;
    }

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
        HideTitle();

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

            UpdatePlayerList();
        });

        startDrawingLoop();
        initResponsiveCanvas();

        try {
            var soundURL = getClass().getResource("/sounds/Message_Short.mp3");
            if (soundURL != null) {
                messageSound = new AudioClip(soundURL.toExternalForm());
            } else {
                System.err.println("Impossible de trouver le son : /sounds/Message_Short.mp3");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement son : " + e.getMessage());
        }
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

    public void SetWordTitle(String wordToFound)
    {
        Platform.runLater(() -> {
            // 1. Mise à jour du Titre (Logique corrigée)
            if (wordTitle != null) {
                if (isDrawer) {
                    wordTitle.setText("DRAW THE WORD :"); // Le dessinateur dessine
                } else {
                    wordTitle.setText("GUESS THE WORD :"); // Les autres devinent
                }
            }

            // 2. Mise à jour du Mot (Avec sécurité anti-crash)
            if (mysteryWord != null) {
                mysteryWord.setText(wordToFound);
            }
        });
    }

    /**
     * Met à jour l'affichage des rounds (ex: Round 2 / 5).
     * @param current Le numéro du round actuel.
     * @param total Le nombre total de rounds.
     */
    public void SetRound(int current, int total)
    {
        Platform.runLater(() -> {
            if (currentRound != null)
            {
                currentRound.setText(String.valueOf(current));
            }

            if (roundNumber != null)
            {
                roundNumber.setText(String.valueOf(total));
            }
        });
    }

    /**
     * Lance le début d'un round : active les contrôles et lance le chronomètre.
     * @param durationSeconds La durée du round en secondes (envoyée par le serveur).
     */
    public void BeginRound(float durationSeconds)
    {
        this.canDraw = true;
        this.remainingTimeSeconds = (int) durationSeconds;

        Platform.runLater(() -> {
            // 1. Gestion de l'interface (Toolbox)
            if (toolbox != null)
            {
                toolbox.setVisible(isDrawer);
                toolbox.setDisable(!isDrawer);
            }

            if(!isDrawer)
            {
                CloseDecisionPanel(true);
            }

            // 2. Initialisation de l'affichage du timer
            updateTimerLabel();

            // 3. Lancement du Chronomètre
            startRoundTimer();

            String username = ClientHandler.Singleton().GetDrawer().Username();
            AddServerMessageToChat(username + " is drawing now!", "#4287f5");
        });
    }

    /**
     * Configure et démarre le Timeline pour le décompte.
     */
    private void startRoundTimer() {
        // Sécurité : on arrête l'ancien timer s'il tourne encore
        stopRoundTimer();

        roundTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingTimeSeconds--;

            // Mise à jour de l'affichage
            updateTimerLabel();

            // Gestion des 10 dernières secondes (Tic-tac)
            if (remainingTimeSeconds <= 10 && remainingTimeSeconds > 0) {
                playTickTockSound();
            }

            // Fin du temps
            if (remainingTimeSeconds <= 0) {
                stopRoundTimer();
                // Optionnel : Tu peux ajouter une logique locale ici "Time's up"
                // mais généralement le serveur envoie le paquet de fin de round.
            }
        }));

        roundTimer.setCycleCount(Animation.INDEFINITE);
        roundTimer.play();
    }

    /**
     * Arrête proprement le timer.
     */
    private void stopRoundTimer() {
        if (roundTimer != null) {
            roundTimer.stop();
            roundTimer = null;
        }
    }

    /**
     * Met à jour le texte du Label timer avec format MM:SS.
     */
    private void updateTimerLabel() {
        if (timer == null) return;

        // On s'assure de ne pas afficher de temps négatif
        int t = Math.max(0, remainingTimeSeconds);

        int minutes = t / 60;
        int seconds = t % 60;

        // Format "M:SS" (ex: 1:05 ou 0:09)
        timer.setText(String.format("%d:%02d", minutes, seconds));

        // Changement de couleur en rouge pour les 10 dernières secondes
        if (t <= 10) {
            timer.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rouge
        } else {
            timer.setStyle("-fx-text-fill: #E59700; -fx-font-weight: bold;"); // Orange (défaut)
        }
    }

    /**
     * Placeholder pour jouer le son.
     */
    private void playTickTockSound() {
        // TODO: Insérer ici le code pour jouer le son (Media / AudioClip)
        // System.out.println("TICK TOCK - " + remainingTimeSeconds);
        /*
        try {
            AudioClip clip = new AudioClip(getClass().getResource("/sounds/tick.wav").toExternalForm());
            clip.play();
        } catch (Exception e) { e.printStackTrace(); }
        */
    }

    /**
     * Cache le titre et le mot mystère (utilisé à l'initialisation).
     */
    public void HideTitle()
    {
        Platform.runLater(() -> {
            if (wordTitle != null) wordTitle.setText("");
            if (mysteryWord != null) mysteryWord.setText("");
        });
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

    public void Clear()
    {
        Platform.runLater(this::fillCanvasWithWhite);
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

    /**
     * Ajoute un message système venant du serveur avec une couleur spécifique.
     * @param message Le texte à afficher.
     * @param color La couleur au format HEX (ex: "#FF0000").
     */
    public void AddServerMessageToChat(String message, String color)
    {
        Platform.runLater(() -> {
            HBox msgBox = new HBox();

            Label lblMsg = new Label(message);
            lblMsg.setWrapText(true);

            // Sécurité : Si la couleur est null ou vide, on met du noir par défaut
            String finalColor = (color != null && !color.isEmpty()) ? color : "#000000";

            // Application du style dynamique
            // On force le gras (-fx-font-weight: bold) pour les messages serveur
            lblMsg.setStyle("-fx-text-fill: " + finalColor + "; -fx-font-weight: bold; -fx-font-size: 15px;");

            msgBox.getChildren().add(lblMsg);
            messagesContainer.getChildren().add(msgBox);

            // Auto Scroll vers le bas
            scrollPaneMessages.setVvalue(1.0);

            if (messageSound != null) {
                messageSound.play();
            }
        });
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
            if (!CanDraw()) return;

            // Dessin local (Feedback immédiat)
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();

            // Préparation et envoi au serveur
            sendDrawCommand(DrawPayload.DrawAction.START, e.getX(), e.getY());
        });

        // 2. GLISSEMENT SOURIS (Continuité du trait)
        drawingCanvas.setOnMouseDragged(e -> {
            if (!CanDraw()) return;

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
            if (!CanDraw()) return;

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

    public void PrepareForNewRound()
    {
        Platform.runLater(this::Internal_PrepareForNewRound);
    }

    private void Internal_PrepareForNewRound()
    {
        this.isDrawer = ClientHandler.Singleton().GetClientImage(ClientHandler.Singleton().ID()).IsDrawer();

        HideTitle();
        hideRoundEnd();
    }



    private ScaleTransition getScaleTransition()
    {
        ScaleTransition st = new ScaleTransition(Duration.millis(500), wordSelectionContent);
        st.setFromX(0);
        st.setFromY(0);
        st.setToX(1);
        st.setToY(1);
        st.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double t) {
                return 1 - Math.pow(1 - t, 5);
            }
        });
        return st;
    }

    /**
     * Ouvre le panneau de sélection de mots.
     * @param animate Si true, joue l'animation d'ouverture (Scale). Sinon, affiche instantanément.
     */
    public void OpenWordsSelectionPanel(String choice1, String choice2, String choice3, boolean animate)
    {
        Platform.runLater(() -> {
            // Mise à jour des textes des boutons
            if(btnWord1 != null) {
                btnWord1.setText(choice1.toUpperCase());
                btnWord1.setOnAction(e -> handleWordChosen(0));
            }
            if(btnWord2 != null) {
                btnWord2.setText(choice2.toUpperCase());
                btnWord2.setOnAction(e -> handleWordChosen(1));
            }
            if(btnWord3 != null) {
                btnWord3.setText(choice3.toUpperCase());
                btnWord3.setOnAction(e -> handleWordChosen(2));
            }

            // On rend le panneau visible et au premier plan
            overlayWordSelection.setVisible(true);
            overlayWordSelection.toFront();

            if (animate)
            {
                // Animation : on part de 0 pour aller à 1
                wordSelectionContent.setScaleX(0);
                wordSelectionContent.setScaleY(0);

                ScaleTransition st = getScaleTransition();
                st.play();
            }
            else
            {
                // Pas d'animation : on met l'échelle à 1 direct
                wordSelectionContent.setScaleX(1);
                wordSelectionContent.setScaleY(1);
            }
        });
    }

    /**
     * Gère le clic sur un mot et ferme le panneau.
     */
    private void handleWordChosen(int word)
    {
        System.out.println("Mot choisi : " + word);
        ClientHandler.Singleton().Out().SendCommand(CommandCode.NT_DECISION, new NTDecisionResultPayload(word));

        // On ferme avec animation car c'est une action utilisateur
        closeWordSelectionPanel(true);
    }

    /**
     * Ferme le panneau de sélection de mots.
     * @param animate Si true, joue l'animation de fermeture. Sinon, cache instantanément.
     */
    private void closeWordSelectionPanel(boolean animate)
    {
        Platform.runLater(() -> {
            if (animate)
            {
                ScaleTransition st = new ScaleTransition(Duration.millis(300), wordSelectionContent);
                st.setFromX(1);
                st.setFromY(1);
                st.setToX(0);
                st.setToY(0);

                st.setInterpolator(new Interpolator() {
                    @Override
                    protected double curve(double t) {
                        return t * t * t * t * t; // EaseInQuint
                    }
                });

                st.setOnFinished(e -> {
                    overlayWordSelection.setVisible(false);
                });

                st.play();
            }
            else
            {
                overlayWordSelection.setVisible(false);
            }
        });
    }

    /**
     * Ouvre le panneau d'attente indiquant qui est en train de choisir.
     * @param drawerID L'ID du joueur qui dessine.
     * @param animate Si true, joue un fondu d'entrée. Sinon, affiche instantanément.
     */
    public void OpenDecisionPanel(int drawerID, boolean animate)
    {
        Platform.runLater(() -> {
            // 1. Récupération du pseudo
            String username = "Un joueur";
            ClientImage client = ClientHandler.Singleton().GetClientImage(drawerID);
            if (client != null) {
                username = client.Username();
            }

            // 2. Mise à jour du texte
            if (lblDecisionWait != null) {
                lblDecisionWait.setText(username + " est en train de choisir le mot...");
            }

            // 3. Affichage
            overlayDecisionWait.setVisible(true);
            overlayDecisionWait.toFront();

            if (animate)
            {
                overlayDecisionWait.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(300), overlayDecisionWait);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            }
            else
            {
                overlayDecisionWait.setOpacity(1.0);
            }
        });
    }

    /**
     * Ferme le panneau d'attente.
     * @param animate Si true, joue un fondu de sortie. Sinon, cache instantanément.
     */
    public void CloseDecisionPanel(boolean animate)
    {
        Platform.runLater(() -> {
            if (!overlayDecisionWait.isVisible()) return;

            if (animate)
            {
                // Animation (Fade Out)
                FadeTransition ft = new FadeTransition(Duration.millis(300), overlayDecisionWait);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);

                ft.setOnFinished(e -> {
                    overlayDecisionWait.setVisible(false);
                    // On remet l'opacité à 1 pour la prochaine ouverture sans anim
                    overlayDecisionWait.setOpacity(1.0);
                });

                ft.play();
            }
            else
            {
                overlayDecisionWait.setVisible(false);
                overlayDecisionWait.setOpacity(1.0);
            }
        });
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
    }
}




