package revxrsal.commands.stream;

import org.junit.jupiter.api.Test;
import revxrsal.commands.exception.InvalidDecimalException;
import revxrsal.commands.exception.InvalidIntegerException;

import static org.junit.jupiter.api.Assertions.*;

class NumberParsingTest {

    private static MutableStringStream stream(String input) {
        return StringStream.createMutable(input);
    }

    @Test
    void parsesValidIntegers() {
        assertEquals(42, stream("42").readInt());
        assertEquals(-7, stream("-7").readInt());
        assertEquals(5, stream("+5").readInt());
        assertEquals(3, stream("٣").readInt()); // Arabic-Indic digit three
        assertEquals(9_000_000_000L, stream("9000000000").readLong());
    }

    @Test
    void rejectsInvalidIntegers() {
        assertThrows(InvalidIntegerException.class, () -> stream("abc").readInt());
        assertThrows(InvalidIntegerException.class, () -> stream("-").readInt());
        assertThrows(InvalidIntegerException.class, () -> stream("12abc").readInt());
        assertThrows(InvalidIntegerException.class, () -> stream("9000000000").readInt());
        assertThrows(InvalidIntegerException.class, () -> stream("").readInt());
    }

    @Test
    void parsesValidDecimals() {
        assertEquals(1.5, stream("1.5").readDouble());
        assertEquals(1000.0, stream("1e3").readDouble());
        assertEquals(0.5, stream(".5").readDouble());
        assertEquals(2.5f, stream("2.5f").readFloat());
    }

    @Test
    void rejectsInvalidDecimals() {
        assertThrows(InvalidDecimalException.class, () -> stream("abc").readDouble());
        assertThrows(InvalidDecimalException.class, () -> stream("NaN").readDouble());
        assertThrows(InvalidDecimalException.class, () -> stream("Infinity").readFloat());
        assertThrows(InvalidDecimalException.class, () -> stream("1e999").readDouble());
    }
}
