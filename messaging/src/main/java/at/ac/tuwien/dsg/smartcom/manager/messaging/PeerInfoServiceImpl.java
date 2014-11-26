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
package at.ac.tuwien.dsg.smartcom.manager.messaging;

import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.manager.dao.PeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerInfoServiceImpl implements PeerInfoService {
    private static final Logger log = LoggerFactory.getLogger(PeerInfoService.class);
    private static final int DEFAULT_CACHE_SIZE = 1000;

    private ExecutorService executor;

    private final LoadingCache<Identifier, PeerInfo> cache;

    @Inject
    private PeerInfoCallback callback;

    @Inject
    private PeerChannelAddressResolverDAO dao;

    public PeerInfoServiceImpl() {
        this(DEFAULT_CACHE_SIZE);
    }

    public PeerInfoServiceImpl(int cacheSize) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        //load entries from the database in case of a cache miss
                        new CacheLoader<Identifier, PeerInfo>() {
                            @Override
                            public PeerInfo load(Identifier id) throws Exception {
                                log.debug("loading peerinfo for peer {} from peer manager", id);

                                final PeerInfo peerInfo = callback.getPeerInfo(id);

                                if (peerInfo == null) {
                                    throw new PeerInfoUnavailableException();
                                }

                                //speed up the retrieval (saving the data in the db might be expensive
                                executor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (PeerChannelAddress address : peerInfo.getAddresses()) {
                                            dao.insert(address);
                                        }
                                    }
                                });

                                return peerInfo;
                            }
                        });
    }


    @PostConstruct
    public void init() {
        executor  = Executors.newSingleThreadExecutor();
    }

    @PreDestroy
    public void destroy() {
        //shut down the executor
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            executor.shutdownNow();
        }
    }

    @Override
    public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
        try {
            return cache.get(id);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof PeerInfoUnavailableException) {
                log.debug("No PeerInfo available for peer {}", id);
            } else {
                log.error("Error while retrieving peerinfo for peer {}", id, e);
            }
            return null;
        }
    }

    private class PeerInfoUnavailableException extends Exception {
    }
}
