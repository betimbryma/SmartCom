package at.ac.tuwien.dsg.smartcom.exception;

public class RoutingException extends CommunicationException {
    public RoutingException(ErrorCode errcode) {
        super(errcode);
    }
}