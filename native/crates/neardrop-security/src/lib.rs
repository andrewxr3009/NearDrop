//! Defensive policies shared by protocol and transfer layers.
pub const MAX_REQUEST_BYTES: usize = 1_048_576;
pub const MAX_FILES_PER_TRANSFER: usize = 128;
pub const MAX_NAME_BYTES: usize = 255;
pub const MAX_TOTAL_TRANSFER_BYTES: u64 = 10 * 1024 * 1024 * 1024;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ValidationError { TooLarge, TooManyFiles, InvalidName, PathTraversal, ConsentRequired }
pub fn validate_request_size(bytes: usize) -> Result<(), ValidationError> {
    if bytes > MAX_REQUEST_BYTES { Err(ValidationError::TooLarge) } else { Ok(()) }
}
pub fn validate_file_count(count: usize) -> Result<(), ValidationError> {
    if count > MAX_FILES_PER_TRANSFER { Err(ValidationError::TooManyFiles) } else { Ok(()) }
}
pub fn validate_total_bytes(bytes: u64) -> Result<(), ValidationError> {
    if bytes > MAX_TOTAL_TRANSFER_BYTES { Err(ValidationError::TooLarge) } else { Ok(()) }
}
/// Accept one display name; callers must generate their own private staging path.
pub fn validate_filename(name: &str) -> Result<(), ValidationError> {
    if name.is_empty() || name.len() > MAX_NAME_BYTES || name.chars().any(|c| c.is_control() || c == '\\' || c == '/') { return Err(ValidationError::InvalidName); }
    if name == "." || name == ".." { return Err(ValidationError::PathTraversal); }
    Ok(())
}
pub fn require_consent(accepted: bool) -> Result<(), ValidationError> {
    if accepted { Ok(()) } else { Err(ValidationError::ConsentRequired) }
}

/// Bounded sequence replay guard. A session must authenticate the peer before trusting sequence IDs.
#[derive(Debug, Default)]
pub struct ReplayGuard { highest: Option<u64>, seen: std::collections::BTreeSet<u64> }
impl ReplayGuard {
    pub const WINDOW: u64 = 4096;
    pub fn accept(&mut self, sequence: u64) -> bool {
        if let Some(highest) = self.highest {
            if sequence.saturating_add(Self::WINDOW) <= highest || self.seen.contains(&sequence) { return false; }
            self.highest = Some(highest.max(sequence));
        } else { self.highest = Some(sequence); }
        self.seen.insert(sequence);
        let floor = self.highest.unwrap_or(sequence).saturating_sub(Self::WINDOW);
        self.seen.retain(|value| *value >= floor);
        true
    }
}

#[cfg(test)] mod tests {
    use super::*;
    #[test] fn rejects_traversal_and_separators() {
        assert_eq!(validate_filename("../secret"), Err(ValidationError::InvalidName));
        assert_eq!(validate_filename(".."), Err(ValidationError::PathTraversal));
        assert!(validate_filename("photo.jpg").is_ok());
    }
    #[test] fn enforces_request_bounds_and_consent() {
        assert!(validate_request_size(MAX_REQUEST_BYTES).is_ok());
        assert_eq!(validate_request_size(MAX_REQUEST_BYTES + 1), Err(ValidationError::TooLarge));
        assert_eq!(require_consent(false), Err(ValidationError::ConsentRequired));
    }
    #[test] fn rejects_replayed_or_expired_sequences() {
        let mut guard = ReplayGuard::default();
        assert!(guard.accept(0)); assert!(!guard.accept(0));
        let mut guard = ReplayGuard::default();
        assert!(guard.accept(5_000)); assert!(!guard.accept(5_000)); assert!(guard.accept(5_002));
        assert!(!guard.accept(5_002)); assert!(!guard.accept(906));
    }
}
