/**
 * Describes one input on a form (one column of a table).
 *   Field.text(...)    -> a text box
 *   Field.fixed(...)   -> a dropdown with fixed choices (e.g. player type)
 *   Field.fk(...)      -> a dropdown filled from another table (foreign key)
 *   Field.autoId(...)  -> an auto-increment ID (shown, but not typed in)
 *   .key()             -> this column is part of the primary key
 *   .nullable()        -> this column may be left empty
 */
public class Field {

    public enum Kind { TEXT, FIXED, FK }

    public final String column;
    public final String label;
    public final Kind kind;
    public String[] options = {};
    public String query = "";
    public boolean nullable = false;
    public boolean key = false;
    public boolean autoId = false;

    private Field(String column, String label, Kind kind) {
        this.column = column;
        this.label = label;
        this.kind = kind;
    }

    public static Field text(String column, String label) {
        return new Field(column, label, Kind.TEXT);
    }

    public static Field autoId(String column, String label) {
        Field f = new Field(column, label, Kind.TEXT);
        f.autoId = true;
        f.key = true;
        return f;
    }

    public static Field fixed(String column, String label, String... options) {
        Field f = new Field(column, label, Kind.FIXED);
        f.options = options;
        return f;
    }

    /** query must return two columns: the id and a readable label. */
    public static Field fk(String column, String label, String query) {
        Field f = new Field(column, label, Kind.FK);
        f.query = query;
        return f;
    }

    public Field key() {
        this.key = true;
        return this;
    }

    public Field nullable() {
        this.nullable = true;
        return this;
    }
}
