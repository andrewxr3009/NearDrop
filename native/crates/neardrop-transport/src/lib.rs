//! Link abstractions. Simulated transport is available only for tests.
use std::fmt;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum TransportKind { Awdl, WifiDirect, Lan, Simulated }
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Availability { Available, Unavailable }
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct TransportError(pub &'static str);
impl fmt::Display for TransportError { fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result { f.write_str(self.0) } }
impl std::error::Error for TransportError {}

pub trait Transport: Send + Sync {
    fn kind(&self) -> TransportKind;
    fn availability(&self) -> Availability;
    fn send(&self, peer: &str, bytes: &[u8]) -> Result<(), TransportError>;
}

/// The default AWDL adapter always reports unavailable. Android public APIs do not expose AWDL.
pub struct AwdlTransport;
impl Transport for AwdlTransport {
    fn kind(&self) -> TransportKind { TransportKind::Awdl }
    fn availability(&self) -> Availability { Availability::Unavailable }
    fn send(&self, _: &str, _: &[u8]) -> Result<(), TransportError> { Err(TransportError("AWDL provider is not implemented")) }
}

#[cfg(test)] pub struct SimulatedTransport;
#[cfg(test)] impl Transport for SimulatedTransport {
    fn kind(&self) -> TransportKind { TransportKind::Simulated }
    fn availability(&self) -> Availability { Availability::Available }
    fn send(&self, _: &str, _: &[u8]) -> Result<(), TransportError> { Ok(()) }
}

#[cfg(test)] mod tests {
    use super::*;
    #[test] fn awdl_is_never_claimed_available() { assert_eq!(AwdlTransport.availability(), Availability::Unavailable); }
    #[test] fn simulated_adapter_is_explicitly_test_only() { assert_eq!(SimulatedTransport.kind(), TransportKind::Simulated); }
}
