//! Discovery provider contracts. No BLE, mDNS or AWDL provider is active yet.
use std::time::{Duration, Instant};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Provenance { Real, Simulated, Unavailable }
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Peer { pub id: String, pub display_name: String, pub service: String, pub provenance: Provenance }
#[derive(Debug)]
pub struct SeenPeer { pub peer: Peer, pub last_seen: Instant }
pub trait DiscoveryProvider { fn name(&self) -> &'static str; fn provenance(&self) -> Provenance; }
pub fn expire_peers(peers: &mut Vec<SeenPeer>, now: Instant, ttl: Duration) {
    peers.retain(|p| now.saturating_duration_since(p.last_seen) <= ttl);
}

#[cfg(test)] mod tests {
    use super::*;
    #[test] fn expires_old_peers() {
        let now = Instant::now();
        let peer = Peer { id: "test-only".into(), display_name: "Fixture".into(), service: "test".into(), provenance: Provenance::Simulated };
        let mut peers = vec![SeenPeer { peer, last_seen: now - Duration::from_secs(60) }];
        expire_peers(&mut peers, now, Duration::from_secs(5)); assert!(peers.is_empty());
    }
}
