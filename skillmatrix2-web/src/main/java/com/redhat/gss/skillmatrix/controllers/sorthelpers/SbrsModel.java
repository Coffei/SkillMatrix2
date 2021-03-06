package com.redhat.gss.skillmatrix.controllers.sorthelpers;

import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.MemberProducer;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.SbrProducer;
import com.redhat.gss.skillmatrix.model.Member;
import com.redhat.gss.skillmatrix.model.SBR;
import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;

import javax.faces.context.FacesContext;
import java.util.List;

/**
 * RichFaces model for {@link SBR}. This model is very flexible and can be used for any sbr-related tables. Just modify
 * the {@link SbrProducer} before giving it to this class by {@link #getProducer()}.<br/>
 * User: jtrantin<br/>
 * Date: 9/13/13<br/>
 * Time: 2:29 PM
 */
public abstract class SbrsModel extends ExtendedDataModel<SBR> {


    private Integer key;

    private SbrProducer producer;

    private List<SBR> sbrs;

    private SequenceRange range;

    public SbrsModel() {
        this.range = new SequenceRange(0, Integer.MAX_VALUE);
    }

    @Override
    public void setRowKey(Object o) {
        if(o instanceof Integer) {
            key = (Integer)o;
        }
    }

    @Override
    public Object getRowKey() {
        return key;
    }

    @Override
    public void walk(FacesContext facesContext, DataVisitor dataVisitor, Range range, Object o) {
        this.producer = getProducer();

        producer.recordsCount(this.range.getRows())
                .recordsStart(this.range.getFirstRow());

        this.sbrs = producer.getSbrs();

        for(int i = 0; i < this.sbrs.size(); i++) {
            dataVisitor.process(facesContext, i, o);
        }
    }

    @Override
    public boolean isRowAvailable() {
        return key!=null;
    }

    @Override
    public int getRowCount() {
        this.producer = getProducer();

        return (int)producer.getCount();
    }

    @Override
    public SBR getRowData() {
        return sbrs.get(key);
    }

    @Override
    public int getRowIndex() {
        return -1;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRowIndex(int i) {
        //noop
    }

    @Override
    public Object getWrappedData() {
        return null;
    }

    @Override
    public void setWrappedData(Object o) {
        // noop
    }

    /**
     *
     * @return range of displayed data.
     */
    public SequenceRange getRange() {
        return range;
    }

    /**
     * Sets the range of displayed data.
     * @param range
     */
    public void setRange(SequenceRange range) {
        this.range = range;
    }

    /**
     * Must return a valid {@link SbrProducer}. The producer can be already modified.
     * @return
     */
    protected abstract SbrProducer getProducer();
}
