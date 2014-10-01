package at.ac.tuwien.dsg.smartcom.adapters.dropbox;

import at.ac.tuwien.dsg.smartcom.model.Message;
import com.dropbox.core.DbxEntry;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface DropboxFileToMessageConverter {

    /**
     * converts a file in the dropbox to a valid message
     * @param entry in the dropbox
     * @return valid message
     */
    public Message convert(DbxEntry entry);
}
