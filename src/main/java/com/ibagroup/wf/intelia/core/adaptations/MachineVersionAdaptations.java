package com.ibagroup.wf.intelia.core.adaptations;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.workfusion.bot.service.ISecureStoreService;
import com.workfusion.bot.service.SecureEntryDTO;
import com.workfusion.utils.client.AmazonUtils;
import com.workfusion.utils.client.S3ContextKeyDTO;

/**
 * WF specific machine adaptations.
 * <p><em>
 * Ideally all adaptations should be in this single place.
 * </em></p>
 * 
 * @author dmitriev
 *
 */
public interface MachineVersionAdaptations {

    public static <T> T wrap(Object dto, ISecureEntryDtoWrapper<T> wrapper) {
        if (dto instanceof SecureEntryDTO) {
            SecureEntryDTO secureEntryDto = (SecureEntryDTO) dto;
            return wrapper.wrap(secureEntryDto.getAlias(), secureEntryDto.getKey(), secureEntryDto.getValue(), secureEntryDto.getLastUpdateDate());
        } else if (dto instanceof com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO) {
            com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO secureEntryDto = (com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO) dto;
            return wrapper.wrap(secureEntryDto.getAlias(), secureEntryDto.getKey(), secureEntryDto.getValue(), secureEntryDto.getLastUpdateDate());
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

    public static List<Method> getMethodsListWithAnnotation(Class examineClass, Class annotationClass) {
        return MethodUtils.getMethodsListWithAnnotation(examineClass, annotationClass, true, true);
    }
}
