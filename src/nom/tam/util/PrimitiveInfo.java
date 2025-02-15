
package nom.tam.util;


/**
 * This interface collects some information about Java primitives.
 */
public interface PrimitiveInfo {

    /** Suffixes used for the classnames for primitive arrays. */
    char[] suffixes = new char[] {
        'B', 'S', 'C', 'I', 'J', 'F', 'D', 'Z'
    };

    /**
     * Classes of the primitives. These should be in windening order
     * (char is as always a problem).
     */
    Class[] classes = new Class[] {
        byte.class, short.class, char.class, int.class, long.class,
        float.class, double.class, boolean.class
    };

    /** Is this a numeric class */
    boolean[] isNumeric = new boolean[] {
        true, true, true, true, true, true, true, false
    };

    /** Full names */
    String[] types = new String[] {
        "byte", "short", "char", "int", "long", "float", "double", "boolean"
    };

    /** Sizes */
    int[] sizes = new int[] {
        1, 2, 2, 4, 8, 4, 8, 1
    };

    /** Index of first element of above arrays referring to a numeric type */
    int FIRST_NUMERIC = 0;

    /** Index of last element of above arrays referring to a numeric type */
    int LAST_NUMERIC = 6;

    /** _more_ */
    int BYTE_INDEX = 0;

    /** _more_ */
    int SHORT_INDEX = 1;

    /** _more_ */
    int CHAR_INDEX = 2;

    /** _more_ */
    int INT_INDEX = 3;

    /** _more_ */
    int LONG_INDEX = 4;

    /** _more_ */
    int FLOAT_INDEX = 5;

    /** _more_ */
    int DOUBLE_INDEX = 6;

    /** _more_ */
    int BOOLEAN_INDEX = 7;
}
