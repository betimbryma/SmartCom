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

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This test demonstrates the behaviour of the dropbox input/output adapter.
 * Note that this is not a JUnit test and can only be run by invoking the main method.
 *
 * The test requires the user to add an access token of a dropbox account. This can be generated by
 * creating an app on <a href='https://www.dropbox.com/developers/apps'>https://www.dropbox.com/developers/apps</a>
 * and by clicking the 'Generate' button below the 'Generate access token' headline.
 *
 * The output adapter will create a new file 'task_[TIMESTAMP].task' in the folder 'smartcom' of the linked Dropbox account.
 * Afterwards the input adapter will start looking for the file and will report its existence.
 */
public class DropboxAdapterTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Please insert access token:");
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

        testDropboxInputAdapter(code);
    }

    public static void testDropboxInputAdapter(String code) throws Exception {

        List<Serializable> parameters = new ArrayList<>(2);
        parameters.add(code);
        parameters.add("smartcom");
        PeerChannelAddress address = new PeerChannelAddress(Identifier.peer("test"), Identifier.adapter("Dropbox"), parameters);

        final Message message = new Message.MessageBuilder()
                .setId(Identifier.message("testId"))
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setSenderId(Identifier.peer("sender"))
                .setReceiverId(Identifier.peer("receiver"))
                .setConversationId(""+System.nanoTime())
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        System.out.println("Output Adapter: Connecting...");
        DropboxOutputAdapter adapter = new DropboxOutputAdapter(address);
        adapter.push(message, null);
        System.out.println("File uploaded!");

        System.out.println("Input Adapter: Connecting...");
        DropboxInputAdapter smartcom = new DropboxInputAdapter(code, "smartcom", "task_"+message.getConversationId()+".task", new Message());
        Message msg = smartcom.pull();
        if (msg != null) {
            System.out.println("File found on linked dropbox account");
        } else {
            System.err.println("File NOT found on linked dropbox account");
        }
    }
}