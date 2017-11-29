public class StringBuilder {
    public StringBuilder( String startString ) {
        currentString = startString;
    }

    public String getNext() {
        return currentString;
    }

    private String currentString;

    private static Character A = 'A';
    private static Character B = 'B';
    private static Character G = 'G';
    private static Character T = 'T';

}
