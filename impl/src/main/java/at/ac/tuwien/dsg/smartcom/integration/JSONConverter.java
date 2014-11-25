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
package at.ac.tuwien.dsg.smartcom.integration;

import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class JSONConverter {
    private static final Logger log = LoggerFactory.getLogger(JSONConverter.class);

    public static class ConverterException extends Exception {

    }

    public static CollectiveInfo getCollectiveInfo(Identifier collective, String content) throws NoSuchCollectiveException {
        CollectiveInfo info = new CollectiveInfo();
        info.setId(collective);
        info.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ALL_MEMBERS); //fake value

        final List<Identifier> peerIDs = new ArrayList<>();
        info.setPeers(peerIDs);

        JSONObject jsonOutput = (JSONObject) JSONValue.parse(content);
        String[] strings = parseResults(jsonOutput);

        if (strings == null) {
            throw new NoSuchCollectiveException();
        }

        for (String string : strings) {
            peerIDs.add(Identifier.peer(string));
        }

        return info;
    }

    public static String[] parsePeerInfo(Identifier peerId, String content) throws NoSuchPeerException {
        String[] values = new String[5];

        JSONObject jsonOutput = (JSONObject) JSONValue.parse(content);
        String[] strings = parseResults(jsonOutput);

        if (strings == null || strings.length != 5) {
            throw new NoSuchPeerException(peerId);
        }

        values[0] = strings[0];
        values[1] = strings[1];
        values[2] = strings[2];
        values[3] = strings[3];
        values[4] = strings[4];

        return values;
    }

    public static String[] parseUserInfo(Identifier peerId, String content) throws NoSuchPeerException {

        JSONObject jsonOutput = (JSONObject) JSONValue.parse(content);

        String[] strings = parseResults(jsonOutput);

        if (strings == null) {
            throw new NoSuchPeerException(peerId);
        }

        return strings;
    }

    public static PeerChannelAddress parsePeerAddressInfo(Identifier peerId, String content) throws NoSuchPeerException {
        PeerChannelAddress address = new PeerChannelAddress();
        address.setPeerId(peerId);

        JSONObject jsonOutput = (JSONObject) JSONValue.parse(content);

        String[] strings = parseResults(jsonOutput);

        if (strings == null || strings.length < 2) {
            throw new NoSuchPeerException(peerId);
        }

        address.setChannelType(Identifier.channelType(strings[1]));

        List<String> valueList = Arrays.asList(strings).subList(2, strings.length);
        address.setContactParameters(valueList);

        return address;
    }

    private static String[] parseResults(JSONObject json) {
        List<String> valueList = new ArrayList<>();

        if (json.containsKey("results")) {
            JSONArray array = (JSONArray) json.get("results");

            if (array.size() == 0) {
                return null;
            }

            if (array.size() > 1) {
                log.warn("Multiple instances in 'results' returned, not sure how to handle this (ignoring further instances)...");
            }

            JSONArray innerArray = (JSONArray) array.get(0);

            for (Object o : innerArray) {
                try {
                    valueList.add(parseToString(o));
                } catch (ConverterException e) {
                    return null;
                }
            }

            return valueList.toArray(new String[valueList.size()]);
        } else {
            return null;
        }
    }

    private static String parseToString(Object obj) throws ConverterException {
        if (obj == null) {
            throw new ConverterException();
        }

        String result;

        if (obj instanceof String) {
            result = (String) obj;
        } else {
            result = String.valueOf(obj);
        }

        if (result == null || "null".equals(result)) {
            throw new ConverterException();
        }

        return result;
    }
}
