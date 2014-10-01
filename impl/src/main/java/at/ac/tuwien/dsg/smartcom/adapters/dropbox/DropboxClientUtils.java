package at.ac.tuwien.dsg.smartcom.adapters.dropbox;

import com.dropbox.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class DropboxClientUtils {
    private static final Logger log = LoggerFactory.getLogger(DropboxClientUtils.class);

    private final DbxClient client;

    public DropboxClientUtils(String accessToken) {
        DbxRequestConfig config = new DbxRequestConfig("SmartCom Adapter", Locale.getDefault().toString());
        client = new DbxClient(config, accessToken);
    }

    public String getAccount() throws DbxException {
        return client.getAccountInfo().displayName;
    }

    public void uploadFile(String path, String filename, long fileLength, InputStream stream) throws IOException, DbxException {
        log.trace("Uploading file...");
        DbxEntry.File uploadedFile = client.uploadFile("/"+path+"/"+filename, DbxWriteMode.add(), fileLength, stream);
        log.trace("Uploaded: " + uploadedFile.toString());
    }

    public DbxEntry findFile(String folder, String filePrefix) throws DbxException {
        log.trace("Searching for file {}", filePrefix);
        if (log.isTraceEnabled()) {
            DbxEntry.WithChildren listing = client.getMetadataWithChildren("/" + folder);

            log.trace("Files in the root path (actual file might be in subdirectory):");
            for (DbxEntry child : listing.children) {
                log.trace("	" + child.name + ": " + child.toString());
            }
        }

        List<DbxEntry> dbxEntries = client.searchFileAndFolderNames("/" + folder, filePrefix);
        if (dbxEntries.size()>0) {
            return dbxEntries.get(0);
        }
        return null;
    }
}
