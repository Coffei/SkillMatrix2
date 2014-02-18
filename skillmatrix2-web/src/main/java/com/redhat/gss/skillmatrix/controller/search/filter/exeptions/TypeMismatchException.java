package com.redhat.gss.skillmatrix.controller.search.filter.exeptions;

/**
 * Created with IntelliJ IDEA.
 * User: jtrantin
 * Date: 12/9/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class TypeMismatchException extends Exception {

    public TypeMismatchException() {
    }

    public TypeMismatchException(String message) {
        super(message);
    }

    public TypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeMismatchException(Throwable cause) {
        super(cause);
    }
}
