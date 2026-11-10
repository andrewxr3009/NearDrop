//! Platform-independent transfer state machine.
use neardrop_security::{require_consent, validate_file_count, validate_filename, validate_total_bytes, ValidationError};

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum SessionState { Offered, AwaitingConsent, Accepted, Transferring { bytes: u64 }, Completed, Rejected, Failed }
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct IncomingOffer { pub transfer_id: String, pub sender: String, pub files: Vec<(String, u64)> }
#[derive(Debug, Clone, PartialEq, Eq)]
pub enum SessionError { InvalidState, Validation(ValidationError) }
impl From<ValidationError> for SessionError { fn from(e: ValidationError) -> Self { Self::Validation(e) } }

pub fn validate_offer(offer: &IncomingOffer) -> Result<(), SessionError> {
    validate_file_count(offer.files.len())?;
    if offer.transfer_id.is_empty() || offer.transfer_id.len() > 128 { return Err(SessionError::InvalidState); }
    let mut total = 0_u64;
    for (name, size) in &offer.files { validate_filename(name)?; total = total.checked_add(*size).ok_or(ValidationError::TooLarge)?; }
    validate_total_bytes(total)?;
    Ok(())
}

pub fn accept_offer(offer: &IncomingOffer, user_accepted: bool) -> Result<SessionState, SessionError> {
    validate_offer(offer)?;
    require_consent(user_accepted)?;
    Ok(SessionState::Accepted)
}

#[cfg(test)] mod tests {
    use super::*;
    fn offer() -> IncomingOffer { IncomingOffer { transfer_id: "id-1".into(), sender: "Nearby sender".into(), files: vec![("notes.txt".into(), 42)] } }
    #[test] fn receiving_requires_explicit_consent() {
        assert_eq!(accept_offer(&offer(), false), Err(SessionError::Validation(ValidationError::ConsentRequired)));
        assert_eq!(accept_offer(&offer(), true), Ok(SessionState::Accepted));
    }
    #[test] fn rejects_traversal_before_acceptance() {
        let mut o = offer(); o.files[0].0 = "../secret".into();
        assert!(validate_offer(&o).is_err());
    }
}
