package at.ac.tuwien.dsg.smartcom.model;

/**
 * The message log level defines which messages will be logged by the
 * Messaging and Routing Manager. Higher log levels might have a significant impact
 * on the execution speed of SmartCom
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public enum MessageLogLevel {
    NONE, INGOING, OUTGOING, EXTERNAL, INTERNAL, ALL
}
