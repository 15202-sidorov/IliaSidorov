public enum Status {
    CLOSED,         // default status
    SYN_SENT,       // CLIENT has send connection request to SERVER
    SYN_RECEIVED,   // SERVER has received request and send another request for CLIENT
    ESTABLISHED,    // Connection is established, transaction is in process
    FIN_WAIT_1,     // side 1 is closing the connection, sending FIN
    CLOSE_WAIT,     // side 2 has received FIN, but continues to send data anyway
    FIN_WAIT_2,     // side 1 received ACK and receives data anyway
    LAST_ACK,       // side 2 stops transaction and sends FIN
    TIME_WAIT,      // side 1 received FIN from side 2, send ACK and waits for ACK back
    CLOSING         // side 2 received last ACK and sends ACK back
}
