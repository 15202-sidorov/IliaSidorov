public enum Status {
    CLOSED,         // default status
    LISTEN,         // SERVER is waiting for client to send something ( works after accept() )
    SYN_SENT,       // CLIENT has send connection request to SERVER
    SYN_RECEIVED,   // SERVER has received request and send another request for CLIENT
    ESTABLISHED,    // Connection is established, transaction is in process
    FIN_WAIT,
    FIN_RECEIVED,
    CLOSING         // side 2 received last ACK and sends ACK back
}
