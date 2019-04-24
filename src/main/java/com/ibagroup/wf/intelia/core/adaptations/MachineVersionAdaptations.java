package com.ibagroup.wf.intelia.core.adaptations;

import com.amazonaws.services.s3.AmazonS3;
import com.workfusion.bot.service.ISecureStoreService;
import com.workfusion.bot.service.SecureEntryDTO;
import com.workfusion.utils.client.AmazonUtils;
import com.workfusion.utils.client.S3ContextKeyDTO;

public interface MachineVersionAdaptations {

    public static <T> T wrap(SecureEntryDTO dto, ISecureEntryDtoWrapper<T> wrapper) {
        if (dto != null) {
            return wrapper.wrap(dto.getAlias(), dto.getKey(), dto.getValue(), dto.getLastUpdateDate());
        }
        return null;
    }

    public static Class<ISecureStoreService> getISecureStoreServiceClass() {
        return ISecureStoreService.class;
    }

    public static AmazonS3 getS3ClientConnection(String s3AccessKey, String s3SecretKey, String s3EndpointUrl) {
        S3ContextKeyDTO contextKeyDTO = new S3ContextKeyDTO();
        contextKeyDTO.setEndpointURL(s3EndpointUrl);
        contextKeyDTO.setPrivateKey(s3SecretKey);
        contextKeyDTO.setPublicKey(s3AccessKey);
        return AmazonUtils.createS3Client(contextKeyDTO, null);
    }

}
