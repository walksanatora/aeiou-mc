@file:Suppress("PrivatePropertyName")

package net.walksanator.aeiou

import net.walksanator.aeiou.wasm.LinearMemorySupport
import wasm_rt_impl.Memory
import wasm_rt_impl.ModuleRegistry

@Suppress("LocalVariableName")
class Functions (private val mr: ModuleRegistry) {
    private var Z_memory: Memory = Memory(16, 32)

    fun setupModuleRegister() {
        mr.exportMemory("Z_env", "Z_memory", Z_memory)
        mr.exportFunc("Z_env", "Z_emscripten_notify_memory_growth", fun (_: Int) {})
        mr.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_close", fun (_: Int) {throw NotImplementedError()})

        val Z_fd_writeZ_iiiii = fun(fd: Int, iov: Int, iovcnt: Int, pnum: Int): Int {
            if (fd == 1 || fd == 2) {
                var written = 0
                for (i in 0 until iovcnt) {
                    val ptr = Z_memory.i32_load(iov + i*8)
                    val size = Z_memory.i32_load(iov + i*8 + 4)
                    val s = LinearMemorySupport.readCString(Z_memory, ptr, size)
                    print(s)
                    written += size
                }
                Z_memory.i32_store(pnum, written)
                return 0
            }
            return 2
        }
        mr.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_write", Z_fd_writeZ_iiiii)

        mr.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_seek", fun (_: Int,_: Long,_: Int,_: Int) {throw NotImplementedError()})
    }
}