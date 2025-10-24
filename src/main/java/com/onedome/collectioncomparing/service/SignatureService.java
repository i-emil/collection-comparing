package com.onedome.collectioncomparing.service;

import com.onedome.collectioncomparing.model.Signature;

import java.util.List;

public interface SignatureService {

    Signature createSignature(List<Long> dataIds);

    double compareSignatures(byte[] oldCollectionSignature, byte[] currentSignature);
}
