package net.walksanator.aeiou.wasm
import wasm_rt_impl.Memory
import java.io.ByteArrayOutputStream

object LinearMemorySupport {
    /**
     * Reads a NUL-terminated C string from mem, starting at address base. The
     * string is parsed as UTF-8.
     */
    fun readCString(mem: Memory, base: Int): String {
        var pos = base.toLong()
        val buf = ByteArrayOutputStream()
        while (true) {
            val byte = mem.i32_load8_s(pos++.toInt())
            if (byte == 0) {
                break
            }
            buf.write(byte)
        }
        return String(buf.toByteArray(), Charsets.US_ASCII)
    }

    /**
     * Reads a NUL-terminated C string from mem, starting at address base, in
     * a buffer of size maxLen. The string is parsed as UTF-8.
     */
    fun readCString(mem: Memory, base: Int, maxLen: Int): String {
        val buf = ByteArrayOutputStream()
        for (pos in (base.toLong() until base.toLong() + maxLen.toLong())) {
            val byte = mem.i32_load8_s(pos.toInt())
            if (byte == 0) {
                break
            }
            buf.write(byte)
        }
        return String(buf.toByteArray(), Charsets.US_ASCII)
    }

    /**
     * Reads an array of bytes from mem.
     */
    fun readBytes(mem: Memory, base: Int, len: Int): ByteArray {
        val buf = ByteArrayOutputStream()
        for (pos in (base.toLong() until base.toLong() + len.toLong())) {
            buf.write(mem.i32_load8_s(pos.toInt()))
        }
        return buf.toByteArray()
    }

    /**
     * Writes an array of bytes to mem.
     */
    fun writeBytes(mem: Memory, base: Int, b: ByteArray) {
        var bpos = 0
        for (pos in (base.toLong() until base.toLong() + b.size.toLong())) {
            mem.i32_store8(pos.toInt(), b[bpos++].toInt())
        }
    }

    /**
     * Writes a C string to mem at the given base. The string is written as
     * UTF-8 and a NUL terminator is appended. Returns the number of bytes
     * written, including the NUL terminator. The string may contain embedded
     * NULs.
     */
    fun writeCString(mem: Memory, base: Int, s: String): Int {
        val bytes = s.toByteArray(Charsets.US_ASCII)
        writeBytes(mem, base, bytes)
        mem.i32_store8((base.toLong() + bytes.size.toLong()).toInt(), 0)
        return bytes.size + 1
    }
}
