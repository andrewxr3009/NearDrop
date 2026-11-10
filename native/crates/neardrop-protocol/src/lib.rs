//! Protocol namespace. Apple AirDrop codecs are intentionally not implemented yet.
use neardrop_security::{validate_filename, validate_request_size, ValidationError};

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct BoundedMessage { pub kind: u8, pub payload: Vec<u8> }
#[derive(Debug, Clone, PartialEq, Eq)]
pub enum CodecError { Truncated, InvalidVersion, Security(ValidationError) }

/// Tiny versioned framing for local NearDrop test fixtures; it is not AirDrop.
pub fn encode_test_message(message: &BoundedMessage) -> Result<Vec<u8>, CodecError> {
    validate_request_size(message.payload.len()).map_err(CodecError::Security)?;
    let len: u32 = message.payload.len().try_into().map_err(|_| CodecError::Security(ValidationError::TooLarge))?;
    let mut out = Vec::with_capacity(10 + message.payload.len());
    out.extend_from_slice(b"NDRP"); out.push(1); out.push(message.kind); out.extend_from_slice(&len.to_be_bytes()); out.extend_from_slice(&message.payload);
    Ok(out)
}
pub fn decode_test_message(input: &[u8]) -> Result<BoundedMessage, CodecError> {
    if input.len() < 10 { return Err(CodecError::Truncated); }
    if &input[..4] != b"NDRP" || input[4] != 1 { return Err(CodecError::InvalidVersion); }
    let len = u32::from_be_bytes(input[6..10].try_into().expect("fixed slice")) as usize;
    validate_request_size(len).map_err(CodecError::Security)?;
    if input.len() != 10 + len { return Err(CodecError::Truncated); }
    Ok(BoundedMessage { kind: input[5], payload: input[10..].to_vec() })
}
pub fn validate_archive_entry_name(name: &str) -> Result<(), ValidationError> { validate_filename(name) }

#[cfg(test)] mod tests {
    use super::*;
    #[test] fn test_frame_round_trip() {
        let msg = BoundedMessage { kind: 3, payload: vec![1, 2, 3] };
        assert_eq!(decode_test_message(&encode_test_message(&msg).unwrap()), Ok(msg));
    }
    #[test] fn refuses_truncated_frame() { assert_eq!(decode_test_message(b"NDRP\x01"), Err(CodecError::Truncated)); }
}
