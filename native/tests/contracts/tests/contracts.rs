use neardrop_core::{accept_offer, IncomingOffer, SessionState};
use neardrop_discovery::Provenance;
use neardrop_protocol::{decode_test_message, encode_test_message, BoundedMessage};
use neardrop_security::ReplayGuard;
use neardrop_transport::{AwdlTransport, Availability, Transport};

#[test]
fn test_protocol_offer_and_consent_contract() {
    let message = BoundedMessage { kind: 1, payload: b"offer".to_vec() };
    let decoded = decode_test_message(&encode_test_message(&message).unwrap()).unwrap();
    assert_eq!(decoded, message);
    let offer = IncomingOffer { transfer_id: "fixture-1".into(), sender: "Peer".into(), files: vec![("readme.txt".into(), 10)] };
    assert!(accept_offer(&offer, false).is_err());
    assert_eq!(accept_offer(&offer, true), Ok(SessionState::Accepted));
}

#[test]
fn unavailable_awdl_and_simulated_provenance_are_explicit() {
    assert_eq!(AwdlTransport.availability(), Availability::Unavailable);
    assert_eq!(Provenance::Simulated, Provenance::Simulated);
}

#[test]
fn replay_window_rejects_duplicate_sequence() {
    let mut guard = ReplayGuard::default();
    assert!(guard.accept(7));
    assert!(!guard.accept(7));
}
