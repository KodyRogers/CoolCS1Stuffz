package gurdle;

/**
 * Represents a letter in a guessed word.
 */
public class CharChoice {

    public enum Status {WRONG, WRONG_POS, RIGHT_POS, EMPTY}

    /**
     * the status of the character vis-a-vis the secret word
     */
    private Status status;

    /**
     * the character in the guess word
     */
    private char ch;

    /**
     * Create a new instance with default status = Status.EMPTY and
     * a white space as character.
     */
    public CharChoice() {
        this.status = Status.EMPTY;
        this.ch = ' ';
    }

    @Override
    public String toString() {
        return String.valueOf(this.ch);
    }

    /**
     * The status of the character choice.
     * @return the status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * The letter
     * @return the letter
     */
    public char getChar() {
        return this.ch;
    }

    /**
     * Set a new character
     * @param ch the new letter
     */
    public void setChar(char ch) {
        this.ch = ch;
    }

    /**
     * Set a new status
     * @param status the new status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
