package com.redhat.gss.skillmatrix.controller.search.filter.filters;

import com.redhat.gss.skillmatrix.controller.search.filter.BasicAttributeFilter;
import com.redhat.gss.skillmatrix.controller.search.filter.Filter;
import com.redhat.gss.skillmatrix.controller.search.filter.FilterType;
import com.redhat.gss.skillmatrix.controller.search.filter.MemberFilter;
import com.redhat.gss.skillmatrix.controller.search.filter.exeptions.TypeMismatchException;
import com.redhat.gss.skillmatrix.controller.search.filter.filters.util.AttributeEncoder;
import com.redhat.gss.skillmatrix.controllers.sorthelpers.MemberModelHelper;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.MemberProducer;

/**
 * Created with IntelliJ IDEA.
 * User: jtrantin
 * Date: 12/6/13
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
@MemberFilter(id = "langFilter",
        name = "language",
        page = "basic.xhtml",
        type = FilterType.BASIC)
public class LanguageFilter implements Filter, BasicAttributeFilter {

    private String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LanguageFilter{" +
                "value='" + value + '\'' +
                '}';
    }

    @Override
    public String encode() {
        return AttributeEncoder.encodeBasicFilter(this);
    }

    @Override
    public void decode(String filter) throws TypeMismatchException, IllegalArgumentException {
        AttributeEncoder.decodeBasicFilter(filter, this);
    }

    @Override
    public boolean apply(MemberModelHelper modelHelper) {
        if(modelHelper==null)
            throw new NullPointerException("modelHelper");

        modelHelper.setLanguagesFilter(this.value);

        return true;
    }

    @Override
    public void applyOnProducer(MemberProducer producer) {
        if(producer==null)
            throw new NullPointerException("producer");

        String[] langs = this.value.split(",");
        for(String lang : langs) {
            if(!lang.trim().isEmpty()) // split languages into separate strings and filter them individually
                producer.filterLanguage(lang.trim());
        }
    }

    @Override
    public String explain() {
        return String.format("language contains '%s'", this.value);
    }
}
