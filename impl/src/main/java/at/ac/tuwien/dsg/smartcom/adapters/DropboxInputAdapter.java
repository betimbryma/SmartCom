/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.adapters.dropbox.DropboxClientUtils;
import at.ac.tuwien.dsg.smartcom.adapters.dropbox.DropboxFileToMessageConverter;
import at.ac.tuwien.dsg.smartcom.model.Message;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class DropboxInputAdapter implements InputPullAdapter {
    private static final Logger log = LoggerFactory.getLogger(DropboxInputAdapter.class);

    private final DropboxClientUtils client;
    private final String folder;
    private final String filePrefix;
    private final DropboxFileToMessageConverter converter;
    private final Message returnMessage;

    public DropboxInputAdapter(String accessToken, String folder, String file, Message returnMessage) {
        this.folder = folder;
        this.filePrefix = file;
        this.returnMessage = returnMessage;
        this.converter = null;
        client = new DropboxClientUtils(accessToken);

        try {
            log.debug("Linked account: " + client.getAccount());
        } catch (DbxException e) {
            log.error("Error while accessing account information of dropbox client!", e);
        }
    }

    public DropboxInputAdapter(String accessToken, String folder, String file, DropboxFileToMessageConverter converter) {
        this.folder = folder;
        this.filePrefix = file;
        this.converter = converter;
        this.returnMessage = null;
        client = new DropboxClientUtils(accessToken);

        try {
            log.debug("Linked account: " + client.getAccount());
        } catch (DbxException e) {
            log.error("Error while accessing account information of dropbox client!", e);
        }
    }

    @Override
    public Message pull() throws AdapterException {
        try {
            DbxEntry found = client.findFile(folder, filePrefix);

            if (found == null) {
                return null;
            } else {
                log.debug("Found requested file {}", found.name);
                if (converter == null) {
                    return returnMessage;
                } else {
                    return converter.convert(found);
                }
            }
        } catch (DbxException e) {
            log.error("Error while accessing dropbox account", e);
        }

        return null;
    }
}
