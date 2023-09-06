/**
 * Copyright 2020 Soni L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * This code adapts code from the emscripten project. Emscripten's license
 * follows:
 *
Emscripten is available under 2 licenses, the MIT license and the
University of Illinois/NCSA Open Source License.

Both are permissive open source licenses, with little if any
practical difference between them.

The reason for offering both is that (1) the MIT license is
well-known, while (2) the University of Illinois/NCSA Open Source
License allows Emscripten's code to be integrated upstream into
LLVM, which uses that license, should the opportunity arise.

The full text of both licenses follows.

==============================================================================

Copyright (c) 2010-2014 Emscripten authors, see AUTHORS file.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

==============================================================================

Copyright (c) 2010-2014 Emscripten authors, see AUTHORS file.
All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the
"Software"), to deal with the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

    Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimers.

    Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following disclaimers
    in the documentation and/or other materials provided with the
    distribution.

    Neither the names of Mozilla,
    nor the names of its contributors may be used to endorse
    or promote products derived from this Software without specific prior
    written permission. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 */

package space.autistic.wasi_rt

import wasm_rt_impl.Memory
import wasm_rt_impl.Table
import wasm_rt_impl.ModuleRegistry

import kotlin.reflect.KMutableProperty0
import kotlin.Function

import java.io.ByteArrayOutputStream

class CxxExceptionException(val number: Int) : RuntimeException(null, null, false, false)

object LongjmpException : RuntimeException(null, null, false, false)

/**
 * An instance of MemoryHolder specifies the memory layout the wasm module will
 * have access to, and holds the Memory object. Note that memory size must be a
 * multiple of 65536, all values are in bytes, the real stack size may be
 * smaller depending on the size of the module's data section, and the stack
 * must have the correct alignment. It is up to the calling code to set an
 * appropriate stack size.
 * @param startMemory The initial amount of memory to be allocated.
 * @param maxMemory The maximum amount of memory to be allowed.
 * @param maxStack The size of the stack. Must be smaller than startMemory.
 */
public class MemoryHolder(startMemory: Int, maxMemory: Int) {
    public var memory = Memory(0, 0)

    init {
        if ((startMemory or maxMemory) < 0) {
            // JVM limitation, memory may only be as big as 2GB-64KB
            throw IllegalArgumentException("Memory size must be non-negative")
        }
        if ((startMemory and 0xFFFF) != 0 || (maxMemory and 0xFFFF) != 0) {
            throw IllegalArgumentException("Memory size must be a multiple of 65536")
        }
        wasm_rt_impl.allocate_memory(this::memory, startMemory/65536, maxMemory/65536)
    }
}

/**
 * The environment used by wasi. Holds environment variables, program
 * arguments, etc.
 */
public class Environment {
    public val environment: MutableList<String> = ArrayList()
    public val arguments: MutableList<String> = ArrayList()

    // By default import env.memory as the WASI memory
    public var memoryModule = "Z_env"
    public var memoryName = "Z_memory"
}

/**
 * WASI errno.
 */
public enum class WasiErrno {
    SUCCESS;

    companion object {
        const val SIZE = 2
        const val ALIGNMENT = 2
    }
}

/**
 * An extension of the ModuleRegistry with Emscripten ABIs for setjmp/longjmp.
 */
open class EmscriptenModuleRegistry(val mainModuleName: String, val memoryLayout: MemoryHolder) : ModuleRegistry() {
    /* "constants" */
    private val func_type_vii = wasm_rt_impl.register_func_type(2, 0, Int::class, Int::class)

    /* data */
    private var tempRet0 = 0

    /* imports */
    private lateinit var stackSave: () -> Int
    private lateinit var stackRestore: (Int) -> Unit
    private lateinit var setThrew: (Int, Int) -> Unit

    /* dynamic imports */
    private inner class Imported {
        public var table by importTable(mainModuleName, "Z___indirect_function_table")
    }
    private val imported by lazy { Imported() }

    private fun Z_emscripten_longjmpZ_vii(env: Int, value: Int) {
        setThrew(env, if (value != 0) value else 1)
        throw LongjmpException
    }
    private fun Z_invoke_viiZ_viii(index: Int, a1: Int, a2: Int) {
        val sp = stackSave()
        var toRestore = true
        try {
            wasm_rt_impl.CALL_INDIRECT<(Int, Int) -> Unit>(imported.table, func_type_vii, index)(a1, a2)
            toRestore = false
            return
        } catch (e: CxxExceptionException) {
            // "empty" catch.
            // this runs the finally block, and then goes through to the setThrew
            // but for any other exception, it'd run the finally *but not* setThrew!
            // which is important for properly handling wasm exceptions and whatnot.
        } catch (e: LongjmpException) {
            // "empty" catch.
            // this runs the finally block, and then goes through to the setThrew
            // but for any other exception, it'd run the finally *but not* setThrew!
            // which is important for properly handling wasm exceptions and whatnot.
        } finally {
            if (toRestore) {
                stackRestore(sp)
            }
        }
        setThrew(1, 0)
    }
    private fun Z_getTempRet0Z_iv(): Int = tempRet0
    private fun Z_setTempRet0Z_vi(value: Int) {
        tempRet0 = value
    }

    override open fun <T> exportFunc(modname: String, fieldname: String, value: Function<T>) {
        if (modname == mainModuleName) {
            when (fieldname) {
                "Z_stackSaveZ_iv" -> stackSave = value as () -> Int
                "Z_setThrewZ_vii" -> setThrew = value as (Int, Int) -> Unit
                "Z_stackRestoreZ_vi" -> stackRestore = value as (Int) -> Unit
            }
        }
        return super.exportFunc(modname, fieldname, value)
    }

    init {
        // related to memory import
        super.exportMemory("Z_env", "Z_memory", memoryLayout::memory)

        // related to setjmp/longjmp
        super.exportFunc("Z_env", "Z_emscripten_longjmpZ_vii", this::Z_emscripten_longjmpZ_vii)
        super.exportFunc("Z_env", "Z_getTempRet0Z_iv", this::Z_getTempRet0Z_iv)
        super.exportFunc("Z_env", "Z_invoke_viiZ_viii", this::Z_invoke_viiZ_viii)
        super.exportFunc("Z_env", "Z_setTempRet0Z_vi", this::Z_setTempRet0Z_vi)

        // related to memory growth (useless for us)
        // TODO this should not exist
        super.exportFunc("Z_env", "Z_emscripten_notify_memory_growthZ_vi", fun(x: Int): Unit {})
    }
}

/**
 * The WASI module.
 */
public open class WasiModule(private val moduleRegistry: ModuleRegistry, private val env: Environment) {
    /**
     * Imports needed by the WASI module (in particular, memory).
     */
    protected open inner class Imported {
        public var memory by moduleRegistry.importMemory(env.memoryModule, env.memoryName)
    }

    protected open val imported by lazy { Imported() }

    protected open fun Z_args_sizes_getZ_iii(pArgc: Int, pArgvBufSize: Int): Int {
        // TODO handle character encoding errors?
        val argc = env.arguments.size
        val buf = StringBuilder()
        env.arguments.forEach(fun(s: String) {
            buf.append(s)
            buf.append('\u0000')
        })
        val argBufSize = buf.toString().toByteArray(Charsets.UTF_8).size

        imported.memory.i32_store(pArgc.toLong(), argc)
        imported.memory.i32_store(pArgvBufSize.toLong(), argBufSize)

        return WasiErrno.SUCCESS.ordinal
    }
    protected open fun Z_args_getZ_iii(argv: Int, argvBuf: Int): Int {
        // TODO handle character encoding errors?
        var base = argvBuf
        var arg = 0
        env.arguments.forEach(fun(s: String) {
            imported.memory.i32_store((argv + arg).toLong(), base)
            base += LinearMemorySupport.writeCString(imported.memory, base, s)
            arg += 4
        })
        return WasiErrno.SUCCESS.ordinal
    }

    protected open fun Z_environ_sizes_getZ_iii(pEnvironc: Int, pEnvironBufSize: Int): Int {
        // TODO handle character encoding errors?
        val environc = env.environment.size
        val buf = StringBuilder()
        env.environment.forEach(fun(s: String) {
            buf.append(s)
            buf.append('\u0000')
        })
        val environBufSize = buf.toString().toByteArray(Charsets.UTF_8).size

        imported.memory.i32_store(pEnvironc.toLong(), environc)
        imported.memory.i32_store(pEnvironBufSize.toLong(), environBufSize)

        return WasiErrno.SUCCESS.ordinal

    }
    protected open fun Z_environ_getZ_iii(environ: Int, environBuf: Int): Int {
        // TODO handle character encoding errors?
        var base = environBuf
        var envvar = 0
        env.environment.forEach(fun(s: String) {
            imported.memory.i32_store((environ + envvar).toLong(), base)
            base += LinearMemorySupport.writeCString(imported.memory, base, s)
            envvar += 4
        })
        return WasiErrno.SUCCESS.ordinal
    }

    init {
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_environ_sizes_getZ_iii", this::Z_environ_sizes_getZ_iii)
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_environ_getZ_iii", this::Z_environ_getZ_iii)
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_args_sizes_getZ_iii", this::Z_args_sizes_getZ_iii)
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_args_getZ_iii", this::Z_args_getZ_iii)
    }
}

public object LinearMemorySupport {
    /**
     * Reads a NUL-terminated C string from mem, starting at address base. The
     * string is parsed as UTF-8.
     */
    fun readCString(mem: Memory, base: Int): String {
        var pos = base.toLong()
        val buf = ByteArrayOutputStream()
        while (true) {
            val byte = mem.i32_load8_s(pos++)
            if (byte == 0) {
                break
            }
            buf.write(byte)
        }
        return String(buf.toByteArray(), Charsets.UTF_8)
    }

    /**
     * Reads a NUL-terminated C string from mem, starting at address base, in
     * a buffer of size maxLen. The string is parsed as UTF-8.
     */
    fun readCString(mem: Memory, base: Int, maxLen: Int): String {
        val buf = ByteArrayOutputStream()
        for (pos in (base.toLong() until base.toLong() + maxLen.toLong())) {
            val byte = mem.i32_load8_s(pos)
            if (byte == 0) {
                break
            }
            buf.write(byte)
        }
        return String(buf.toByteArray(), Charsets.UTF_8)
    }

    /**
     * Reads an array of bytes from mem.
     */
    fun readBytes(mem: Memory, base: Int, len: Int): ByteArray {
        val buf = ByteArrayOutputStream()
        for (pos in (base.toLong() until base.toLong() + len.toLong())) {
            buf.write(mem.i32_load8_s(pos))
        }
        return buf.toByteArray()
    }

    /**
     * Writes an array of bytes to mem.
     */
    fun writeBytes(mem: Memory, base: Int, b: ByteArray) {
        var bpos = 0
        for (pos in (base.toLong() until base.toLong() + b.size.toLong())) {
            mem.i32_store8(pos, b[bpos++].toInt())
        }
    }

    /**
     * Writes a C string to mem at the given base. The string is written as
     * UTF-8 and a NUL terminator is appended. Returns the number of bytes
     * written, including the NUL terminator. The string may contain embedded
     * NULs.
     */
    fun writeCString(mem: Memory, base: Int, s: String): Int {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeBytes(mem, base, bytes)
        mem.i32_store8(base.toLong() + bytes.size.toLong(), 0)
        return bytes.size + 1
    }
}
