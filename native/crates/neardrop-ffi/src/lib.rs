//! JNI entry points kept deliberately small; no protocol parsing occurs at this boundary.
use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_dev_neardrop_core_NativeBridge_nativeVersion(mut env: JNIEnv, _class: JClass) -> jstring {
    match env.new_string("NearDrop native 0.1.0 (AirDrop compatibility not implemented)") {
        Ok(value) => value.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
