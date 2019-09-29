package com.lhc.webviewjsbridge;

/**
 * @author  lhc
 */

public interface ResponseHandler<T> {

    void complete(T responseData);
}
