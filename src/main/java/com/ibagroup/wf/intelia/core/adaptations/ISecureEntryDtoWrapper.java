package com.ibagroup.wf.intelia.core.adaptations;

import java.util.Date;

/**
 * WF SecureEntryDto consumer(wrapper) interface
 * 
 * @author dmitriev
 *
 * @param <T> - consumer class
 */
@FunctionalInterface
public interface ISecureEntryDtoWrapper<T> {
    T wrap(String alias, String key, String value, Date lastUpdateDate);
}
