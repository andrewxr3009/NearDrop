# Hardware and compatibility matrix

Status is evidence-scoped; model names alone do not establish support. One handset was used to confirm Nearby Connections advertising starts, but no peer-to-peer file transfer has been verified yet.

| Platform/device class | Ordinary Android app | Privileged/system integration | Evidence/status |
|---|---|---|---|
| Generic Android device | Compose, SAF, intents; LAN/Wi-Fi Direct are possible future NearDrop transports | Depends on vendor image | AirDrop/AWDL unavailable in this project |
| Motorola moto g35 5G (connected test device) | Android API 35; ADB reports `UMS9620`/`ums9620`; Motorola's spec lists UniSOC T760 ([source](https://en-us.support.motorola.com/app/answers/detail/a_id/187376/~/specifications---moto-g35-5g)); Nearby Connections can be used by the APK when Google Play services and runtime permissions are available | No privileged AWDL adapter or compatible driver path has been verified for this handset | APK built and installed; Nearby advertising and BLE announcement started after adding `ACCESS_WIFI_STATE`. A second phone was not connected, so peer discovery, connection, and file transfer remain unverified. Google’s published Quick Share-to-AirDrop device list does not list Moto G35 ([source](https://blog.google/products-and-platforms/platforms/android/new-android-updates/)); Quick Share remains OS-owned. No AirDrop interoperability claim |
| Pixel 10 family with supported Broadcom `wonder` driver path | Public app APIs do not expose the full AWDL control path | Tarish reports its OS-integrated stack tested on selected Pixel 10 variants; radio may leave infrastructure Wi-Fi | Project-reported, not reproduced here; verify exact SKU/kernel/vendor image |
| Pixel 10a or device without `wonder` support | No AWDL through public APIs | Tarish says AWDL path unavailable; its Quick Share path may still work | Vendor-specific observation, not a universal chipset rule |
| OnePlus 15 / Qualcomm `wonder.ko` + KernelSU | No unprivileged access to required driver controls | Mosey-Bada describes an experimental privileged integration | Repository archived 2026-10-20; requires root/module/vendor binary and carries system modification risks |
| Linux with compatible driver/OWL | Not Android | Root/driver/channel support required | OWL is GPL and archived; research reference |
| macOS | Native AWDL interface | System support | Useful test peer; behavior varies by OS version |

Tarish's current hardware notes describe AirDrop support as dependent on Google's `wonder` Wi-Fi driver/shim in selected Pixel vendor images and list Pixel 10 variants, not the moto g35 ([Tarish hardware and integration notes](https://tarish.dev/)). This is not evidence that the g35 radio is physically incapable; it means this project has no verified AWDL path for it, and the current ordinary APK cannot provide that missing privileged path.

## Validation record

No device compatibility tests were run in this workspace. Before adding a supported entry, record exact model/SKU, SoC, Wi-Fi firmware/driver, kernel, vendor image, SELinux state, Android/iOS/macOS version, transport settings, Wi-Fi concurrency behavior, both directions, and test artifact/log hashes. Test stock Apple devices with no helper app and do not use Contacts Only unless identity proof is legitimately supported.
