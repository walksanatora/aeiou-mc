package space.autistic.lua53

import wasm_rt_impl.ModuleRegistry
import wasm_rt_impl.Memory
import wasm_rt_impl.Table
import java.io.ByteArrayOutputStream

//  /* import: 'env' 'signal' */
//  private var w2k_Z_signalZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_signalZ_iii");
//  /* import: 'env' 'abort' */
//  private var w2k_Z_abortZ_vv: () -> Unit by moduleRegistry.importFunc("Z_env", "Z_abortZ_vv");
//  /* import: 'env' '_emscripten_throw_longjmp' */
//  private var w2k_Z__emscripten_throw_longjmpZ_vv: ( -> Unit by moduleRegistry.importFunc("Z_env", "Z__emscripten_throw_lonpjmpZ_vv");
//  /* import: 'env' 'getTempRet0' */
//  private var w2k_Z_getTempRet0Z_iv: () -> Int by moduleRegistry.importFunc("Z_env", "Z_getTempRet0Z_iv");
//  /* import: 'env' 'invoke_vii' */
//  private var w2k_Z_invoke_viiZ_viii: (Int, Int, Int) -> Unit by moduleRegistry.importFunc("Z_env", "Z_invoke_viiZ_viii");
//  /* import: 'env' 'setTempRet0' */
//  private var w2k_Z_setTempRet0Z_vi: (Int) -> Unit by moduleRegistry.importFunc("Z_env", "Z_setTempRet0Z_vi");
//  /* import: 'env' 'time' */
//  private var w2k_Z_timeZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_timeZ_ii");
//  /* import: 'env' 'clock' */
//  private var w2k_Z_clockZ_iv: () -> Int by moduleRegistry.importFunc("Z_env", "Z_clockZ_iv");
//  /* import: 'env' 'strftime' */
//  private var w2k_Z_strftimeZ_iiiii: (Int, Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_strftimeZ_iiiii");
//  /* import: 'env' 'difftime' */
//  private var w2k_Z_difftimeZ_dii: (Int, Int) -> Double by moduleRegistry.importFunc("Z_env", "Z_difftimeZ_dii");
//  /* import: 'env' 'system' */
//  private var w2k_Z_systemZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_systemZ_ii");
//  /* import: 'env' 'exit' */
//  private var w2k_Z_exitZ_vi: (Int) -> Unit by moduleRegistry.importFunc("Z_env", "Z_exitZ_vi");
//  /* import: 'env' 'mktime' */
//  private var w2k_Z_mktimeZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_mktimeZ_ii");
//  /* import: 'env' '__sys_fcntl64' */
//  private var w2k_Z___sys_fcntl64Z_iiii: (Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_fcntl64Z_iiii");
//  /* import: 'env' '__sys_dup3' */
//  private var w2k_Z___sys_dup3Z_iiii: (Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_dup3Z_iiii");
//  /* import: 'env' '__sys_dup2' */
//  private var w2k_Z___sys_dup2Z_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_dup2Z_iii");
//  /* import: 'env' '__clock_gettime' */
//  private var w2k_Z___clock_gettimeZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___clock_gettimeZ_iii");
//  /* import: 'env' '__sys_open' */
//  private var w2k_Z___sys_openZ_iiii: (Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_openZ_iiii");
//  /* import: 'env' '__sys_unlink' */
//  private var w2k_Z___sys_unlinkZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_unlinkZ_ii");
//  /* import: 'wasi_snapshot_preview1' 'fd_close' */
//  private var w2k_Z_fd_closeZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_wasi_snapshot_preview1", "Z_fd_closeZ_ii");
//  /* import: 'env' '__sys_rmdir' */
//  private var w2k_Z___sys_rmdirZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_rmdirZ_ii");
//  /* import: 'wasi_snapshot_preview1' 'fd_write' */
//  private var w2k_Z_fd_writeZ_iiiii: (Int, Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_wasi_snapshot_preview1", "Z_fd_writeZ_iiiii");
//  /* import: 'env' '__sys_rename' */
//  private var w2k_Z___sys_renameZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_renameZ_iii");
//  /* import: 'env' '__sys_lstat64' */
//  private var w2k_Z___sys_lstat64Z_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_lstat64Z_iii");
//  /* import: 'wasi_snapshot_preview1' 'fd_read' */
//  private var w2k_Z_fd_readZ_iiiii: (Int, Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_wasi_snapshot_preview1", "Z_fd_readZ_iiiii");
//  /* import: 'env' '__sys_ioctl' */
//  private var w2k_Z___sys_ioctlZ_iiii: (Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___sys_ioctlZ_iiii");
//  /* import: 'env' '__gmtime_r' */
//  private var w2k_Z___gmtime_rZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___gmtime_rZ_iii");
//  /* import: 'env' '__localtime_r' */
//  private var w2k_Z___localtime_rZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z___localtime_rZ_iii");
//  /* import: 'wasi_snapshot_preview1' 'environ_sizes_get' */
//  private var w2k_Z_environ_sizes_getZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_wasi_snapshot_preview1", "Z_environ_sizes_getZ_iii");
//  /* import: 'wasi_snapshot_preview1' 'environ_get' */
//  private var w2k_Z_environ_getZ_iii: (Int, Int) -> Int by moduleRegistry.importFunc("Z_wasi_snapshot_preview1", "Z_environ_getZ_iii");
//  /* import: 'env' 'emscripten_resize_heap' */
//  private var w2k_Z_emscripten_resize_heapZ_ii: (Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_emscripten_resize_heapZ_ii");
//  /* import: 'env' 'emscripten_memcpy_big' */
//  private var w2k_Z_emscripten_memcpy_bigZ_iiii: (Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_env", "Z_emscripten_memcpy_bigZ_iiii");
//  /* import: 'wasi_snapshot_preview1' 'fd_seek' */
//  private var w2k_Z_fd_seekZ_iiiiii: (Int, Int, Int, Int, Int) -> Int by moduleRegistry.importFunc("Z_wasi_snapshot_preview1", "Z_fd_seekZ_iiiiii");
//  /* import: 'env' 'memory' */
//  private var w2k_Z_memory: wasm_rt_impl.Memory by moduleRegistry.importMemory("Z_env", "Z_memory");
//
//    /* export: '__indirect_function_table' */
//    moduleRegistry.exportTable(name, "Z___indirect_function_table", this@Lua53::w2k___indirect_function_table);
//    /* export: '__wasm_call_ctors' */
//    moduleRegistry.exportFunc(name, "Z___wasm_call_ctorsZ_vv", this@Lua53::w2k___wasm_call_ctors);
//    /* export: 'main' */
//    moduleRegistry.exportFunc(name, "Z_mainZ_iii", this@Lua53::w2k_main);
//    /* export: 'fflush' */
//    moduleRegistry.exportFunc(name, "Z_fflushZ_ii", this@Lua53::w2k_fflush);
//    /* export: 'malloc' */
//    moduleRegistry.exportFunc(name, "Z_mallocZ_ii", this@Lua53::w2k_malloc);
//    /* export: 'saveSetjmp' */
//    moduleRegistry.exportFunc(name, "Z_saveSetjmpZ_iiiii", this@Lua53::w2k_saveSetjmp);
//    /* export: 'testSetjmp' */
//    moduleRegistry.exportFunc(name, "Z_testSetjmpZ_iiii", this@Lua53::w2k_testSetjmp);
//    /* export: 'free' */
//    moduleRegistry.exportFunc(name, "Z_freeZ_vi", this@Lua53::w2k_free);
//    /* export: '__errno_location' */
//    moduleRegistry.exportFunc(name, "Z___errno_locationZ_iv", this@Lua53::w2k___errno_location);
//    /* export: 'realloc' */
//    moduleRegistry.exportFunc(name, "Z_reallocZ_iii", this@Lua53::w2k_realloc);
//    /* export: '_get_tzname' */
//    moduleRegistry.exportFunc(name, "Z__get_tznameZ_iv", this@Lua53::w2k__get_tzname);
//    /* export: '_get_daylight' */
//    moduleRegistry.exportFunc(name, "Z__get_daylightZ_iv", this@Lua53::w2k__get_daylight);
//    /* export: '_get_timezone' */
//    moduleRegistry.exportFunc(name, "Z__get_timezoneZ_iv", this@Lua53::w2k__get_timezone);
//    /* export: 'stackSave' */
//    moduleRegistry.exportFunc(name, "Z_stackSaveZ_iv", this@Lua53::w2k_stackSave);
//    /* export: 'stackRestore' */
//    moduleRegistry.exportFunc(name, "Z_stackRestoreZ_vi", this@Lua53::w2k_stackRestore);
//    /* export: 'stackAlloc' */
//    moduleRegistry.exportFunc(name, "Z_stackAllocZ_ii", this@Lua53::w2k_stackAlloc);
//    /* export: 'setThrew' */
//    moduleRegistry.exportFunc(name, "Z_setThrewZ_vii", this@Lua53::w2k_setThrew);
//    /* export: '__data_end' */
//    moduleRegistry.exportGlobal(name, "Z___data_endZ_i", this@Lua53::w2k___data_end);
//    /* export: 'dynCall_jiji' */
//    moduleRegistry.exportFunc(name, "Z_dynCall_jijiZ_iiiiii", this@Lua53::w2k_dynCall_jiji);

class NumberEmscriptenException(val number: Int) : RuntimeException(null, null, false, false) {
}

object LongjmpException : RuntimeException(null, null, false, false) {
}

class ProcExit : RuntimeException(null, null, false, false) {
}

class LuaRunner(moduleRegistry: ModuleRegistry) {
    var Z_proc_exitZ_vi: (Int) -> Unit
    var Z_emscripten_notify_memory_growthZ_vi: (Int) -> Unit
    var Z_clock_time_getZ_iiji: (Int, Long, Int) -> Int
    var Z_abortZ_vv: () -> Unit
    var Z_exitZ_vi: (Int) -> Unit
    var Z___clock_gettimeZ_iii: (Int, Int) -> Int 
    var Z_clockZ_iv: () -> Int 
    var Z_difftimeZ_dii: (Int, Int) -> Double 
    var Z__emscripten_throw_longjmpZ_vv: () -> Unit
    var Z_emscripten_memcpy_bigZ_iiii: (Int, Int, Int) -> Int 
    var Z_emscripten_resize_heapZ_ii: (Int) -> Int 
    var Z_getTempRet0Z_iv: () -> Int 
    var Z___gmtime_rZ_iii: (Int, Int) -> Int 
    var Z_invoke_viiZ_viii: (Int, Int, Int) -> Unit
    var Z___localtime_rZ_iii: (Int, Int) -> Int 
    var Z_mktimeZ_ii: (Int) -> Int 
    var Z_setTempRet0Z_vi: (Int) -> Unit
    var Z_signalZ_iii: (Int, Int) -> Int 
    var Z_strftimeZ_iiiii: (Int, Int, Int, Int) -> Int 
    var Z___sys_dup2Z_iii: (Int, Int) -> Int 
    var Z___sys_dup3Z_iiii: (Int, Int, Int) -> Int 
    var Z___sys_fcntl64Z_iiii: (Int, Int, Int) -> Int 
    var Z___sys_ioctlZ_iiii: (Int, Int, Int) -> Int 
    var Z___sys_lstat64Z_iii: (Int, Int) -> Int 
    var Z___sys_openZ_iiii: (Int, Int, Int) -> Int 
    var Z___sys_renameZ_iii: (Int, Int) -> Int 
    var Z___sys_rmdirZ_ii: (Int) -> Int 
    var Z_systemZ_ii: (Int) -> Int 
    var Z___sys_unlinkZ_ii: (Int) -> Int 
    var Z_timeZ_ii: (Int) -> Int 
    var Z_environ_getZ_iii: (Int, Int) -> Int 
    var Z_environ_sizes_getZ_iii: (Int, Int) -> Int 
    var Z_fd_closeZ_ii: (Int) -> Int 
    var Z_fd_readZ_iiiii: (Int, Int, Int, Int) -> Int 
    var Z_fd_seekZ_iijii: (Int, Long, Int, Int) -> Int 
    var Z_fd_writeZ_iiiii: (Int, Int, Int, Int) -> Int 

    var Z_memory: Memory = Memory(1, 16*16)
    var Z_table: Table = Table(0, 0)

    var tempRet0: Int = 0;

    val invoke_functype =  wasm_rt_impl.register_func_type(2, 0, Int::class, Int::class);

    init {
        Z_abortZ_vv = fun() {
            throw Exception("abort")
        }
        Z_exitZ_vi = fun(a: Int) {
            throw Exception("exit with code " + a)
        }

        Z___clock_gettimeZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z_clockZ_iv = fun(): Int { throw Exception("unimplemented"); }
        Z_difftimeZ_dii = fun(a: Int, b: Int): Double { throw Exception("unimplemented"); }
        Z__emscripten_throw_longjmpZ_vv = fun() { throw Exception("unimplemented"); }
        Z_emscripten_memcpy_bigZ_iiii = fun(a: Int, b: Int, c: Int): Int { throw Exception("unimplemented"); }
        Z_emscripten_resize_heapZ_ii = fun(a: Int): Int { throw Exception("unimplemented"); }

        Z_getTempRet0Z_iv = fun(): Int = tempRet0

        Z___gmtime_rZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }

        Z_invoke_viiZ_viii = fun(index: Int, a1: Int, a2: Int) {
            try {
                wasm_rt_impl.CALL_INDIRECT<(Int, Int) -> Unit>(Z_table, invoke_functype, index)(a1, a2)
            } catch (e: NumberEmscriptenException) {
                // setThrew(1, 0)
            } catch (e: LongjmpException) {
                // setThrew(1, 0)
            }
        }

        Z___localtime_rZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z_mktimeZ_ii = fun(a: Int): Int { throw Exception("unimplemented"); }

        Z_setTempRet0Z_vi = fun(value: Int) {
            tempRet0 = value
        }

        Z_signalZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z_strftimeZ_iiiii = fun(a: Int, b: Int, c: Int, d: Int): Int { throw Exception("unimplemented"); }
        Z___sys_dup2Z_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z___sys_dup3Z_iiii = fun(a: Int, b: Int, c: Int): Int { throw Exception("unimplemented"); }
        Z___sys_fcntl64Z_iiii = fun(a: Int, b: Int, c: Int): Int { throw Exception("unimplemented"); }
        Z___sys_ioctlZ_iiii = fun(a: Int, b: Int, c: Int): Int { throw Exception("unimplemented"); }
        Z___sys_lstat64Z_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z___sys_openZ_iiii = fun(a: Int, b: Int, c: Int): Int { throw Exception("unimplemented"); }
        Z___sys_renameZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z___sys_rmdirZ_ii = fun(a: Int): Int { throw Exception("unimplemented"); }
        Z_systemZ_ii = fun(a: Int): Int { throw Exception("unimplemented"); }
        Z___sys_unlinkZ_ii = fun(a: Int): Int { throw Exception("unimplemented"); }

        Z_timeZ_ii = fun(ptr: Int): Int {
            var ret = 0; // Date.now() / 1000 | 0
            if (ptr != 0) {
                Z_memory.i32_store(ptr, ret)
            }
            return ret
        }

        Z_environ_getZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z_environ_sizes_getZ_iii = fun(a: Int, b: Int): Int { throw Exception("unimplemented"); }
        Z_fd_closeZ_ii = fun(a: Int): Int { throw Exception("unimplemented"); }
        Z_fd_readZ_iiiii = fun(fd: Int, iov: Int, iovcnt: Int, pnum: Int): Int {
            if (fd == 0) {
                var written = 0
                for (i in 0 until iovcnt) {
                    val ptr = Z_memory.i32_load(iov + i*8)
                    val size = Z_memory.i32_load(iov + i*8 + 4)
                    val bytes = ByteArray(size)
                    val s = System.`in`.read(bytes)
                    LinearMemorySupport.writeBytes(Z_memory, ptr, bytes, 0, s)
                    written += s
                    if (s < size) {
                        break
                    }
                }
                Z_memory.i32_store(pnum, written)
                return 0
            }
            return 2
        }
        Z_fd_seekZ_iijii = fun(a: Int, b: Long, c: Int, d: Int): Int { throw Exception("unimplemented"); }
        Z_fd_writeZ_iiiii = fun(fd: Int, iov: Int, iovcnt: Int, pnum: Int): Int {
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
        Z_proc_exitZ_vi = fun(a: Int): Unit { throw ProcExit() }
        Z_emscripten_notify_memory_growthZ_vi = fun(a: Int): Unit { }
        Z_clock_time_getZ_iiji = fun(a: Int, b: Long, z: Int): Int { return 2 }

        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_proc_exit", Z_proc_exitZ_vi);
        moduleRegistry.exportFunc("Z_env", "Z_emscripten_notify_memory_growth", Z_emscripten_notify_memory_growthZ_vi);
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_clock_time_get", Z_clock_time_getZ_iiji);

        //  /* import: 'env' 'signal' */
        moduleRegistry.exportFunc("Z_env", "Z_signal", Z_signalZ_iii);
        //  /* import: 'env' 'abort' */
        moduleRegistry.exportFunc("Z_env", "Z_abort", Z_abortZ_vv);
        //  /* import: 'env' '_emscripten_throw_longjmp' */
        moduleRegistry.exportFunc("Z_env", "Z__emscripten_throw_longjmp", Z__emscripten_throw_longjmpZ_vv);
        //  /* import: 'env' 'getTempRet0' */
        moduleRegistry.exportFunc("Z_env", "Z_getTempRet0", Z_getTempRet0Z_iv);
        //  /* import: 'env' 'invoke_vii' */
        moduleRegistry.exportFunc("Z_env", "Z_invoke_vii", Z_invoke_viiZ_viii);
        //  /* import: 'env' 'setTempRet0' */
        moduleRegistry.exportFunc("Z_env", "Z_setTempRet0", Z_setTempRet0Z_vi);
        //  /* import: 'env' 'time' */
        moduleRegistry.exportFunc("Z_env", "Z_time", Z_timeZ_ii);
        //  /* import: 'env' 'clock' */
        moduleRegistry.exportFunc("Z_env", "Z_clock", Z_clockZ_iv);
        //  /* import: 'env' 'strftime' */
        moduleRegistry.exportFunc("Z_env", "Z_strftime", Z_strftimeZ_iiiii);
        //  /* import: 'env' 'difftime' */
        moduleRegistry.exportFunc("Z_env", "Z_difftime", Z_difftimeZ_dii);
        //  /* import: 'env' 'system' */
        moduleRegistry.exportFunc("Z_env", "Z_system", Z_systemZ_ii);
        //  /* import: 'env' 'exit' */
        moduleRegistry.exportFunc("Z_env", "Z_exit", Z_exitZ_vi);
        //  /* import: 'env' 'mktime' */
        moduleRegistry.exportFunc("Z_env", "Z_mktime", Z_mktimeZ_ii);
        //  /* import: 'env' '__sys_fcntl64' */
        moduleRegistry.exportFunc("Z_env", "Z___sys_fcntl64", Z___sys_fcntl64Z_iiii);
        //  /* import: 'env' '__sys_dup3' */
        moduleRegistry.exportFunc("Z_env", "Z___syscall_dup3", Z___sys_dup3Z_iiii);
        //  /* import: 'env' '__sys_dup2' */
        moduleRegistry.exportFunc("Z_env", "Z___sys_dup2", Z___sys_dup2Z_iii);
        //  /* import: 'env' '__clock_gettime' */
        moduleRegistry.exportFunc("Z_env", "Z___clock_gettime", Z___clock_gettimeZ_iii);
        //  /* import: 'env' '__sys_open' */
        moduleRegistry.exportFunc("Z_env", "Z___sys_open", Z___sys_openZ_iiii);
        //  /* import: 'env' '__sys_unlink' */
        moduleRegistry.exportFunc("Z_env", "Z___syscall_unlink", Z___sys_unlinkZ_ii);
        //  /* import: 'wasi_snapshot_preview1' 'fd_close' */
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_close", Z_fd_closeZ_ii);
        //  /* import: 'env' '__sys_rmdir' */
        moduleRegistry.exportFunc("Z_env", "Z___syscall_rmdir", Z___sys_rmdirZ_ii);
        //  /* import: 'wasi_snapshot_preview1' 'fd_write' */
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_write", Z_fd_writeZ_iiiii);
        //  /* import: 'env' '__sys_rename' */
        moduleRegistry.exportFunc("Z_env", "Z___syscall_rename", Z___sys_renameZ_iii);
        //  /* import: 'env' '__sys_lstat64' */
        moduleRegistry.exportFunc("Z_env", "Z___syscall_lstat64", Z___sys_lstat64Z_iii);
        //  /* import: 'wasi_snapshot_preview1' 'fd_read' */
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_read", Z_fd_readZ_iiiii);
        //  /* import: 'env' '__sys_ioctl' */
        moduleRegistry.exportFunc("Z_env", "Z___syscall_ioctl", Z___sys_ioctlZ_iiii);
        //  /* import: 'env' '__gmtime_r' */
        moduleRegistry.exportFunc("Z_env", "Z___gmtime_r", Z___gmtime_rZ_iii);
        //  /* import: 'env' '__localtime_r' */
        moduleRegistry.exportFunc("Z_env", "Z___localtime_r", Z___localtime_rZ_iii);
        //  /* import: 'wasi_snapshot_preview1' 'environ_sizes_get' */
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_environ_sizes_get", Z_environ_sizes_getZ_iii);
        //  /* import: 'wasi_snapshot_preview1' 'environ_get' */
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_environ_get", Z_environ_getZ_iii);
        //  /* import: 'env' 'emscripten_resize_heap' */
        moduleRegistry.exportFunc("Z_env", "Z_emscripten_resize_heap", Z_emscripten_resize_heapZ_ii);
        //  /* import: 'env' 'emscripten_memcpy_big' */
        moduleRegistry.exportFunc("Z_env", "Z_emscripten_memcpy_big", Z_emscripten_memcpy_bigZ_iiii);
        //  /* import: 'wasi_snapshot_preview1' 'fd_seek' */
        moduleRegistry.exportFunc("Z_wasi_snapshot_preview1", "Z_fd_seek", Z_fd_seekZ_iijii);
        //  /* import: 'env' 'memory' */
        moduleRegistry.exportMemory("Z_env", "Z_memory", Z_memory);
    }
}

public object LinearMemorySupport {
    /**
     * Reads a NUL-terminated C string from mem, starting at address base. The
     * string is parsed as UTF-8.
     */
    fun readCString(mem: Memory, base: Int): String {
        var pos = base
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
        for (pos in (base until base + maxLen)) {
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
        for (pos in (base until base + len)) {
            buf.write(mem.i32_load8_s(pos))
        }
        return buf.toByteArray()
    }

    /**
     * Writes an array of bytes to mem.
     */
    fun writeBytes(mem: Memory, base: Int, b: ByteArray) {
        writeBytes(mem, base, b, 0, b.size)
    }

    fun writeBytes(mem: Memory, base: Int, b: ByteArray, offset: Int, len: Int) {
        var bpos = offset
        for (pos in (base until base + len)) {
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
        mem.i32_store8(base + bytes.size, 0)
        return bytes.size + 1
    }
}

fun main(args: Array<String>) {
    var reg = ModuleRegistry()
    var runner = LuaRunner(reg)
    var lua = Lua53(reg, "Z_lua")
    runner.Z_table = reg.importTable("Z_lua", "Z___indirect_function_table");
    var newstate: () -> Int = reg.importFunc("Z_lua", "Z_luaL_newstate")
    var state = newstate()
    var openlibs: (Int) -> Unit = reg.importFunc("Z_lua", "Z_luaL_openlibs")
    openlibs(state)
    var callk: (Int, Int, Int, Int, Int) -> Unit = reg.importFunc("Z_lua", "Z_lua_callk")
    var pushcclosure: (Int, Int, Int) -> Unit = reg.importFunc("Z_lua", "Z_lua_pushcclosure")
    pushcclosure(state, 37, 0); // load
    pushcclosure(state, 85, 0); // io.read
    callk(state, 0, 1, 0, 0)
    callk(state, 1, 1, 0, 0)
    callk(state, 0, 0, 0, 0)
    var memory = reg.importMemory("Z_env", "Z_memory");
    println(memory.pages);
}

