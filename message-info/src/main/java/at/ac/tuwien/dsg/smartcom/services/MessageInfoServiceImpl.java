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
package at.ac.tuwien.dsg.smartcom.services;

import at.ac.tuwien.dsg.smartcom.exception.UnknownMessageException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.MessageInformation;
import at.ac.tuwien.dsg.smartcom.services.dao.MessageInfoDAO;
import at.ac.tuwien.dsg.smartcom.services.rest.MessageInfoResource;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MessageInfoServiceImpl implements MessageInfoService {
    private static final Logger log = LoggerFactory.getLogger(MessageInfoService.class);
    private static final int DEFAULT_CACHE_SIZE = 1000;

    private final LoadingCache<MessageInformation.Key, MessageInformation> cache;

    private HttpServer server;
    private final URI serverURI;

    @Inject
    private MessageInfoDAO dao;

    public MessageInfoServiceImpl(int port, String serverURIPostfix, MessageInfoDAO dao) {
        this(port, serverURIPostfix, DEFAULT_CACHE_SIZE);
        this.dao = dao;
    }

    public MessageInfoServiceImpl(int port, String serverURIPostfix) {
        this(port, serverURIPostfix, DEFAULT_CACHE_SIZE);
    }

    public MessageInfoServiceImpl(int port, String serverURIPostfix, long cacheSize) {
        this.serverURI = URI.create("http://localhost:" + port + "/" + serverURIPostfix);

        cache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        //load entries from the database in case of a cache miss
                        new CacheLoader<MessageInformation.Key, MessageInformation>() {
                            @Override
                            public MessageInformation load(MessageInformation.Key key) throws Exception {
                                log.debug("loading message info {} from database", key);

                                MessageInformation info = dao.find(key);

                                //throw an exception if there is no such address because it is not allowed to return null here
                                if (info == null) {
                                    throw new MISException();
                                }
                                return info;
                            }
                        });
    }

    public void init() {
        server = GrizzlyHttpServerFactory.createHttpServer(serverURI, new RESTApplication());
        try {
            server.start();
        } catch (IOException e) {
            log.error("Could not initialize MessageInfoServiceImpl", e);
        }
    }

    public void cleanUp() {
        server.shutdown();
        cache.cleanUp();
    }

    @Override
    public MessageInformation getInfoForMessage(Message message) throws UnknownMessageException {
        try {
            return cache.get(new MessageInformation.Key(message.getType(), message.getSubtype()));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MISException) {
                log.trace("There is no message info for type {} and subtype {}", message.getType(), message.getSubtype());
                throw new UnknownMessageException();
            }
            log.error("Exception during retrieval of message info!", e);
            throw new UnknownMessageException();
        }
    }

    @Override
    public void addMessageInfo(Message message, MessageInformation info) {
        dao.insert(info);
        cache.put(info.getKey(), info);
    }

    /**
     * Internal exception that indicates that the address could not be resolved.
     * It has been introduced because a CacheLoader in the Cache is not allowed to
     * return null if the entry could not be loaded.
     */
    private static class MISException extends Exception {
    }

    private class RESTApplication extends ResourceConfig {
        private RESTApplication() {
            register(MessageInfoResource.class);

            register(JacksonFeature.class);
//            register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(MessageInfoServiceImpl.this).to(MessageInfoService.class);
                }
            });
        }
    }
}
