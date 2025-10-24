package com.onedome.collectioncomparing.service.impl;

import com.dynatrace.hash4j.hashing.Hashing;
import com.dynatrace.hash4j.similarity.ElementHashProvider;
import com.dynatrace.hash4j.similarity.SimilarityHashPolicy;
import com.dynatrace.hash4j.similarity.SimilarityHasher;
import com.dynatrace.hash4j.similarity.SimilarityHashing;
import com.onedome.collectioncomparing.model.Signature;
import com.onedome.collectioncomparing.service.SignatureService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

@Service
public class SignatureServiceImpl implements SignatureService {

    private static final int NUM_COMPONENTS = 256;
    private static final int BAND_COUNT = 32;
    private static final int BITS_PER_COMPONENT = 1;

    private static final SimilarityHashPolicy POLICY = SimilarityHashing.superMinHash(NUM_COMPONENTS, BITS_PER_COMPONENT);
    private static final SimilarityHasher HASHER = POLICY.createHasher();
    private static final ToLongFunction<Long> ELEM_HASH = v -> Hashing.komihash5_0().hashLongToLong(v);

    @Override
    public Signature createSignature(List<Long> dataIds) {
        byte[] signature = HASHER.compute(ElementHashProvider.ofCollection(dataIds, ELEM_HASH));
        List<Long> bandHashes = computeBandHashes(signature);

        return Signature.builder().hash(signature).bandHashes(bandHashes).collection(dataIds).build();
    }

    @Override
    public double compareSignatures(byte[] oldCollectionSignature, byte[] currentSignature) {
        double fraction = POLICY.getFractionOfEqualComponents(currentSignature, oldCollectionSignature);
        return (fraction - Math.pow(2.0, -BITS_PER_COMPONENT))
                / (1.0 - Math.pow(2.0, -BITS_PER_COMPONENT));
    }

    private List<Long> computeBandHashes(byte[] signature) {
        int totalBytes = signature.length;
        int base = totalBytes / BAND_COUNT;
        int rem = totalBytes % BAND_COUNT;

        List<Long> result = new ArrayList<>(BAND_COUNT);
        int offset = 0;
        for (int b = 0; b < BAND_COUNT; b++) {
            int len = base + (b < rem ? 1 : 0);
            long h = Hashing.komihash5_0().hashBytesToLong(signature, offset, len);
            result.add(h);
            offset += len;
        }
        return result;
    }
}
