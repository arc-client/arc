
import com.arc.util.VarIntIterator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class VarIntIteratorTest {

    private lateinit var iterator: VarIntIterator

    @Test
    fun `test single byte varint`() {
        val bytes = byteArrayOf(0b01111111.toByte())  // Maximum single-byte VarInt (127)
        iterator = VarIntIterator(bytes)

        assertTrue(iterator.hasNext())
        assertEquals(127, iterator.next())
        assertFalse(iterator.hasNext())  // No more elements after this
    }

    @Test
    fun `test multi-byte varint`() {
        val bytes = byteArrayOf(0b10000001.toByte(), 0b00000001.toByte()) // Represents 129
        iterator = VarIntIterator(bytes)

        assertTrue(iterator.hasNext())
        assertEquals(129, iterator.next())
        assertFalse(iterator.hasNext())  // No more elements after this
    }

    @Test
    fun `test varint iterator with multiple values`() {
        val bytes = byteArrayOf(
            0b10000001.toByte(), 0b00000001.toByte(),  // 129
            0b01111111.toByte(),  // 127
            0b10000000.toByte(), 0b00000001.toByte()   // 128
        )
        iterator = VarIntIterator(bytes)

        assertTrue(iterator.hasNext())
        assertEquals(129, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(127, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(128, iterator.next())
        assertFalse(iterator.hasNext())  // No more elements after this
    }

    @Test
    fun `test varint iterator with no elements`() {
        val bytes = byteArrayOf()  // Empty byte array
        iterator = VarIntIterator(bytes)

        assertFalse(iterator.hasNext())  // There are no elements
        assertFails {
            iterator.next()  // Should throw exception since there are no elements
        }
    }

    @Test
    fun `test reading varint with unexpected end of byte array`() {
        val bytes = byteArrayOf(0b10000001.toByte())  // Only part of a VarInt
        iterator = VarIntIterator(bytes)

        assertFails {
            iterator.next()  // Should throw exception since the VarInt is incomplete
        }
    }
}
