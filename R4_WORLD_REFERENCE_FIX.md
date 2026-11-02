# Weak legacy world fallback — R4 development checkpoint

LegacyRuntimeContext previously strongly retained the last unscoped world. It now remembers that fallback weakly while preserving strong current/nested ThreadLocal delivery scopes and restoring previous scopes on close. The WeakReference wrapper is reused while the same world remains live.

Validation already executed: 23 contract assertions; full cached build; isolated native control/fix spell registry, nested world scope, reload, player recovery and save/natural exit checks. Stopped IntegratedServer witness decreased 1 to 0. One closed ClientWorld remained via other owners. This is functional lifecycle evidence, not a measured physical-working-set or FPS benefit. No public version or production JAR changed in this checkpoint.
