package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AddressResolver {
    private static final Logger log = LoggerFactory.getLogger(AddressResolver.class);

    private final LoadingCache<AddressKey, PeerAddress> cache;
    private final ResolverDAO dao;

    AddressResolver(ResolverDAO dao, int cacheSize) {
        this.dao = dao;

        cache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<AddressKey, PeerAddress>() {
                            @Override
                            public PeerAddress load(AddressKey addressKey) throws Exception {
                                log.debug("loading address () from database", addressKey);
                                PeerAddress address = AddressResolver.this.dao.find(addressKey.peerId, addressKey.adapterId);
                                if (address == null) {
                                    throw new AddressResolverException();
                                }
                                return address;
                            }


                        });
    }

    public PeerAddress getPeerAddress(String peerId, String adapterId) {
        try {
            if (adapterId.startsWith("adapter.")) {
                adapterId = adapterId.replaceFirst("adapter.", "");
            }

            return cache.get(new AddressKey(peerId, adapterId));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AddressResolverException) {
                log.debug("There is address for PeerId () and AdapterId ()", peerId, adapterId);
                return null;
            }
            log.error("Exception during retrieval of peer address!", e);
            return null;
        }
    }

    public void addPeerAddress(PeerAddress address) {
        dao.insert(address);
        cache.put(new AddressKey(address.getPeerId(), address.getAdapter()), address);
    }

    public void removePeerAddress(String peerId, String adapterId) {
        cache.invalidate(new AddressKey(peerId, adapterId));
        dao.remove(peerId, adapterId);
    }

    private class AddressKey {
        final String peerId;
        final String adapterId;

        private AddressKey(String peerId, String adapterId) {
            this.peerId = peerId;
            this.adapterId = adapterId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AddressKey that = (AddressKey) o;

            return adapterId.equals(that.adapterId) && peerId.equals(that.peerId);
        }

        @Override
        public int hashCode() {
            int result = peerId.hashCode();
            result = 31 * result + adapterId.hashCode();
            return result;
        }
    }

    class AddressResolverException extends Exception {
    }
}
