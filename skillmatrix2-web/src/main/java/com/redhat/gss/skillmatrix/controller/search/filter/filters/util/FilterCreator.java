package com.redhat.gss.skillmatrix.controller.search.filter.filters.util;

import com.redhat.gss.skillmatrix.controller.search.filter.Filter;
import com.redhat.gss.skillmatrix.controller.search.filter.MemberFilter;
import com.redhat.gss.skillmatrix.controller.search.filter.exeptions.TypeMismatchException;
import org.reflections.Reflections;

import javax.enterprise.context.Dependent;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: jtrantin
 * Date: 2/3/14
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
@Dependent
public class FilterCreator {

    private Logger log = Logger.getLogger(getClass().getName());

    @Inject
    private Reflections reflections;

    Pattern typePattern = Pattern.compile("^([^:]+):(.)*$"); //group 1 is filter type
    public Filter createFilter(String encodedFilter) {
        if(encodedFilter==null)
            throw new NullPointerException("encodedFilter");
        Matcher matcher = typePattern.matcher(encodedFilter);
        if(!matcher.matches())
            throw new IllegalArgumentException("encodedFilter is not an encoded filter");

        String type = matcher.group(1);

        //lookup filter class
        Class<? extends Filter> filterClass = lookupFilterClass(type);

        if (filterClass == null) {// if no filter class found
            throw new IllegalArgumentException("Unsupported or unknown filter.");
        }

        Filter filter = null;
        try {
            filter = filterClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(String.format("Unable to create new filter of type %s. \nRoot: %s\n%s",
                    type, e.toString(), Arrays.toString(e.getStackTrace())));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Unable to create new filter of type %s. \nRoot: %s\n%s",
                    type, e.toString(), Arrays.toString(e.getStackTrace())));
        }

        if (filter != null) {
            try {
                filter.decode(encodedFilter);
            } catch (TypeMismatchException e) {
                throw new IllegalArgumentException(String.format("Unable to decode a filter. %s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
            }
        }

        return filter;
    }

    private Class<? extends Filter> lookupFilterClass(String type) {
        //do the magic
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(MemberFilter.class);

        for (Class<?> cls : classes) {
            MemberFilter annt = cls.getAnnotation(MemberFilter.class);
            if (annt.id().equals(type) && Filter.class.isAssignableFrom(cls))
                return cls.asSubclass(Filter.class);

        }

        return null;
    }

}
