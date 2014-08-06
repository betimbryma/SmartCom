package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.OutputAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.annotations.Adapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.dropbox.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Locale;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
@Adapter(name = "Dropbox", stateful = true)
public class DropboxOutputAdapter implements OutputAdapter {
    private static final Logger log = LoggerFactory.getLogger(DropboxOutputAdapter.class);

    private final DbxClient client;
    private final String path;

    public DropboxOutputAdapter(PeerAddress address) throws AdapterException {
        DbxRequestConfig config = new DbxRequestConfig("SmartCom Adapter", Locale.getDefault().toString());
        client = new DbxClient(config, (String) address.getContactParameters().get(0));

        path = (String) address.getContactParameters().get(1);
        try {
            log.debug("Linked account: " + client.getAccountInfo().displayName);
        } catch (DbxException e) {
            log.error("Error while accessing account information of dropbox client!", e);
            throw new AdapterException("Could not create Adapter: "+e.getLocalizedMessage());
        }
    }

    @Override
    public void push(Message message, PeerAddress address) throws AdapterException {
        File taskFile = null;
        try {
            taskFile = File.createTempFile("task_"+message.getConversationId(), "task");
            FileWriter writer = new FileWriter(taskFile);

            writer.write(messageToString(message));
            writer.close();
        } catch (IOException e) {
            log.error("Could not create temporary file 'task_{}", message.getConversationId());
            throw new AdapterException("Could not create temporary file");
        }

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(taskFile);
        } catch (FileNotFoundException e) {
            throw new AdapterException("Could not create temporary file");
        }
        try {
            log.debug("Uploading file...");
            DbxEntry.File uploadedFile = client.uploadFile("/"+path+"/task_"+message.getConversationId()+".task", DbxWriteMode.add(), taskFile.length(), inputStream);
            log.debug("Uploaded: " + uploadedFile.toString());
        } catch (IOException | DbxException e) {
            throw new AdapterException("Could not upload file!");
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String messageToString(Message message) {
        return message.toString();
    }
}
