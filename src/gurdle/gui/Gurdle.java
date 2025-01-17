package gurdle.gui;

import gurdle.CharChoice;
import gurdle.Model;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.Observer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.*;

/**
 * The graphical user interface to the Wordle game model in
 * {@link Model}.
 *
 * @author Kody Rogers
 */
public class Gurdle extends Application
        implements Observer< Model, String > {

    private Model model;

    /** the information for the labels */
    private final static int ROWS = 6;
    private final static int COLS = 5;
    private Label[][] labels;

    private static final Background[] colors = {
            new Background( new BackgroundFill( Color.LIGHTGREEN, null, null ) ),
            new Background( new BackgroundFill( Color.BURLYWOOD, null, null ) ),
            new Background( new BackgroundFill( Color.LIGHTGREY, null, null ) ),
            new Background( new BackgroundFill( Color.WHITE, null, null ) )
    };

    /** Colors to indicate whether letters are in the word or not */
    private static final EnumMap< CharChoice.Status, Background > CHAR_FILL =
            new EnumMap<>( Map.of(
                    CharChoice.Status.RIGHT_POS, colors[0],
                    CharChoice.Status.WRONG_POS, colors[1],
                    CharChoice.Status.WRONG, colors[2],
                    CharChoice.Status.EMPTY, colors[3]
            ) );

    private static final EnumMap<Model.GameState, String> STATE_MSGS =
            new EnumMap<>(Map.of(
                    Model.GameState.WON, "You won!",
                    Model.GameState.LOST, "You lost 😥.",
                    Model.GameState.ONGOING, "Make a guess!",
                    Model.GameState.ILLEGAL_WORD, "Illegal word."
            ));

    private BorderPane borderPane;

    //checks if the program is initialized
    private boolean initialized;

    //allows for the buttons to be updated on the hashmap
    private HashMap<Character, Button> keyboardMap;

    //All updating values in the top of boarderPane
    private Label guesses;
    private Label status;
    private Label secret;
    private Pane pane;

    @Override public void init() throws Exception {
        initialized = false;
        this.model = new Model();
        this.model.addObserver( this );

        List< String > paramStrings = getParameters().getRaw();
        if (paramStrings.size() == 1) {
            final String firstWord = paramStrings.get( 0 );
            if ( firstWord.length() == Model.WORD_SIZE ) {
                this.model.newGame( firstWord );
            }
            else {
                throw new Exception(
                        String.format(
                                "\"%s\" is not the required word length (%d)." +
                                        System.lineSeparator(),
                                firstWord, Model.WORD_SIZE
                        )
                );
            }
        }
        else {
            this.model.newGame();
        }

        this.labels = new Label[ ROWS ][ COLS ];
    }

    /**
     * Makes a 5x6
     * @return a grid pane
     */
    private GridPane makeGridPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        for ( int r = 0; r < ROWS; ++r ) {
            for ( int c = 0; c < COLS; ++c ) {
                this.labels[r][c] = new Label( " " );
                this.labels[r][c].setStyle( """
                    -fx-padding: 2;
                    -fx-border-style: solid inside;
                    -fx-border-width: 2;
                    -fx-border-insets: 5;
                    -fx-border-radius: 2;
                    -fx-border-color: black;
                    -fx-font: 18px Menlo;
                    -fx-alignment: Center;
                """);
                labels[r][c].setMinSize(35,50);
                labels[r][c].setBackground(colors[3]);
                grid.add( this.labels[r][c], c, r );
            }
        }
        return grid;
    }

    /**
     * gets the number of guesses currently
     * @return: guesses
     */
    private Label getGuesses() {
        guesses = new Label("#guesses: " + model.numAttempts());
        return guesses;
    }

    /**
     * Gets the current game status
     * @return: status
     */
    private Label getStatus() {
        status = new Label(STATE_MSGS.get(model.gameState()));
        return status;
    }

    /**
     * Gets the secret work
     * @return the secret label
     */
    private Label getSecret() {
        secret = new Label("secret: " + model.secret());
        return secret;
    }

    /**
     * Generates the pane used by boarderPane
     * @return pane
     */
    private Pane getPane() {
        pane = new StackPane(getGuesses(), getStatus());
        secret = getSecret();
        StackPane.setAlignment(guesses, Pos.CENTER_LEFT);
        StackPane.setAlignment(secret, Pos.CENTER_RIGHT);

        return pane;
    }

    /**
     * Starts a new game with the game models reset
     */
    private void newGame() {
        this.model.newGame();
        pane = getPane();
        this.labels = new Label[ ROWS ][ COLS ];
        keyboardMap = new HashMap<>();
        borderPane.setTop(pane);
        borderPane.setCenter(makeGridPane());
        borderPane.setBottom(keyboard());
    }

    /**
     * Creates a gridpane for a keyboard keys
     * @return a keyboard
     */
    private GridPane keyboardGrid() {
        GridPane gridPane = new GridPane();
        keyboardMap = new HashMap<>();

        char[] top = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'};
        char[] mid = {'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'};
        char[] bot = {'Z', 'X', 'C', 'V', 'B', 'N', 'M'};

        ArrayList<char[]> list = new ArrayList<>();
        list.add(top);
        list.add(mid);
        list.add(bot);

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).length; j++) {
                char a = list.get(i)[j];
                Button b = new Button(String.valueOf(a));

                keyboardMap.put(a, b);

                b.setStyle("-fx-border-color: black");
                b.setMinSize(30,30);
                b.setBackground(colors[3]);
                b.setOnAction(event -> model.enterNewGuessChar(a));
                gridPane.add(b, j, i);
            }
        }

        return gridPane;
    }

    /**
     * Generates the keyboard and the New Game, Enter, and Cheat button
     * @return a BoarderPane to be used by another boarderPane
     */
    public BorderPane keyboard() {
        BorderPane keyboard = new BorderPane();
        HBox hbox = new HBox();
        Button newGame = new Button("New Game");
        Button cheat = new Button("Cheat");

        newGame.setOnAction( event -> newGame());
        cheat.setOnAction(event -> cheat());

        hbox.getChildren().addAll(newGame, cheat);
        hbox.setAlignment(Pos.CENTER);


        keyboard.setBottom(hbox);
        keyboard.setCenter(keyboardGrid());

        Button enter = new Button("Enter");
        enter.setOnAction(event -> model.confirmGuess());
        keyboard.setRight(enter);
        return keyboard;
    }

    /**
     * the cheat function reveling the word
     */
    public void cheat() {
        secret = getSecret();
        pane.getChildren().add(secret);
        StackPane.setAlignment(secret, Pos.CENTER_RIGHT);
    }


    @Override
    public void start( Stage mainStage ) {

        borderPane = new BorderPane();

        pane = getPane();

        borderPane.setTop(pane);
        borderPane.setCenter(makeGridPane());
        borderPane.setBottom(keyboard());

        Scene scene = new Scene(borderPane);
        mainStage.setScene( scene );
        mainStage.show();
        initialized = true;
    }


    @Override
    public void update( Model model, String message ) {

        if ( !this.initialized ) return;

        for ( int guessNum = 0; guessNum <= model.numAttempts(); ++guessNum ) {
            if (guessNum == 6) {
                break;
            }
            for ( int charPos = 0; charPos < Model.WORD_SIZE; ++charPos ) {
                CharChoice cc = model.get( guessNum, charPos );
                final char ch = cc.getChar();
                final CharChoice.Status ccStatus = cc.getStatus();
                labels[guessNum][charPos].setText(String.valueOf(ch));
                labels[guessNum][charPos].setBackground(CHAR_FILL.get(ccStatus));
                Button b = keyboardMap.get(ch);
                if (b != null && b.getBackground().equals(colors[3])) {
                    b.setBackground(CHAR_FILL.get(ccStatus));
                }

            }
        }

        final Model.GameState gamestate = model.gameState();
        if ( gamestate != Model.GameState.ONGOING ) {
            status = new Label(STATE_MSGS.get(model.gameState()));
            pane.getChildren().set(0, getGuesses());
            StackPane.setAlignment(guesses, Pos.CENTER_LEFT);
            pane.getChildren().set(1, status);
            borderPane.setTop(pane);
        }
        if ( gamestate == Model.GameState.LOST ) {
            status = new Label(STATE_MSGS.get(model.gameState()));
            secret = getSecret();
            if (!pane.getChildren().contains(secret)) {
                pane.getChildren().add(secret);
                StackPane.setAlignment(secret, Pos.CENTER_RIGHT);
            }
        }
        else {
            pane.getChildren().set(0, getGuesses());
            StackPane.setAlignment(guesses, Pos.CENTER_LEFT);
        }


    }

    public static void main( String[] args ) {
        if ( args.length > 1 ) {
            System.err.println( "Usage: java Gurdle [1st-secret-word]" );
        }
        Application.launch( args );
    }
}
