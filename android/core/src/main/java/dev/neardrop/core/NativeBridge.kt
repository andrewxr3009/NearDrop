package dev.neardrop.core

/** Narrow JNI surface. The library is optional in this scaffold; absence is reported explicitly. */
object NativeBridge {
    val available: Boolean = try { System.loadLibrary("neardrop_ffi"); true } catch (_: UnsatisfiedLinkError) { false }
    external fun nativeVersion(): String
}
