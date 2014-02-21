package com.redhat.gss.skillmatrix.data.dao.producers.util;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Class representing operator of order.
 * User: jtrantin
 * Date: 8/6/13
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public enum OperatorEnum {

    BIGGER, SMALLER, BIGGER_OR_EQUAL, SMALLER_OR_EQUAL, EQUAL;

    public <Y extends Comparable<? super Y>> Predicate createPredicate(CriteriaBuilder cb, Expression<? extends Y> first, Expression<? extends Y> second) {
        switch (this) {
            case BIGGER:
                return cb.greaterThan(first, second);
            case EQUAL:
                return cb.equal(first, second);
            case SMALLER:
                return cb.lessThan(first, second);
            case BIGGER_OR_EQUAL:
                return cb.greaterThanOrEqualTo(first, second);
            case SMALLER_OR_EQUAL:
                return cb.lessThanOrEqualTo(first, second);
            default:
                //should never happen!
                assert false;

        }
        return null;
    }

    public String createSQLTextPredicate() {
        switch(this){
            case SMALLER:
                return "<";
            case EQUAL:
                return "=";
            case BIGGER:
                return ">";
            case BIGGER_OR_EQUAL:
                return ">=";
            case SMALLER_OR_EQUAL:
                return "<=";
            default:
                //should not happen
                assert false;
        }

        return null;
    }

    public String toReadableText() {
        switch (this) {
            case BIGGER:
                return "bigger than";
            case EQUAL:
                return "equal to";
            case SMALLER:
                return "smaller than";
            case BIGGER_OR_EQUAL:
                return "bigger than or equal to";
            case SMALLER_OR_EQUAL:
                return "smaller than pr equal to";
            default:
                //should not happen
                assert false;
        }

        return null;
    }

    public static OperatorEnum fromReadableText(String text) {
        if(text==null)
            return null;

        if (text.equals("bigger than")) {
            return OperatorEnum.BIGGER;
        }
        if (text.equals("equal to")) {
            return OperatorEnum.EQUAL;
        }
        if (text.equals("smaller than")) {
            return OperatorEnum.SMALLER;
        }
        if (text.equals("bigger than or equal to")) {
            return OperatorEnum.BIGGER_OR_EQUAL;
        }
        if (text.equals("smaller than pr equal to")) {
            return OperatorEnum.SMALLER_OR_EQUAL;
        }

        return null;
    }
    
    public <T extends Comparable<T>> boolean compare(T a, T b) {
        switch(this) {
            case BIGGER:
                return a.compareTo(b) > 0;
            case BIGGER_OR_EQUAL:
                return a.compareTo(b) >= 0;
            case EQUAL:
                return a.compareTo(b) == 0;
            case SMALLER:
                return a.compareTo(b) < 0;
            case SMALLER_OR_EQUAL:
                return a.compareTo(b) <= 0;
            default:
                assert(false); // should never happen
                
        }
        
        return false;
    }
}
