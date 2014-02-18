package com.redhat.gss.skillmatrix.data.dao.producers;

import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.SbrProducer;
import com.redhat.gss.skillmatrix.model.Member;
import com.redhat.gss.skillmatrix.model.*;



import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: jtrantin
 * Date: 8/14/13
 * Time: 9:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class SbrProducerDB implements SbrProducer {
    private final Logger log = Logger.getLogger(getClass().getName());

    private EntityManager em;

    private List<Filter> filters;
    private List<Ordering> orders;
    private UserTransaction transaction;

    private Integer maxRecords;
    private Integer startOffset;

    public SbrProducerDB(EntityManager em, UserTransaction transaction) {
        if(em==null)
            throw new IllegalArgumentException("entity manager must be provided", new NullPointerException("em"));
        if(transaction==null)
            throw new IllegalArgumentException("user transaction must be provided", new NullPointerException("transaction"));

        this.em = em;
        this.filters = new LinkedList<Filter>();
        this.orders = new LinkedList<Ordering>();
        this.transaction = transaction;
    }

    @Override
    public SbrProducer filterId(final long id) {
        this.filters.add(new Filter() {
            @Override
            public Predicate apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return cb.equal(root.get(SBR_.id), id);
            }
        });

        return this;
    }

    @Override
    public SbrProducer filterName(final String nameFragment) {
        if(nameFragment == null)
            throw new NullPointerException("nameFragment");
        if(nameFragment.isEmpty())
            throw new IllegalArgumentException("nameFragment cannot be empty");

        this.filters.add(new Filter() {
            @Override
            public Predicate apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return cb.like(cb.lower(root.get(SBR_.name)), "%" + nameFragment.toLowerCase(Locale.ENGLISH) + "%");
            }
        });

        return this;
    }

    @Override
    public SbrProducer filterNameExact(final String name) {
        if(name==null)
            throw new NullPointerException("name");
        if(name.isEmpty())
            throw new IllegalArgumentException("name cannot be empty");

        this.filters.add(new Filter() {
            @Override
            public Predicate apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return cb.equal(root.get(SBR_.name), name);
            }
        });

        return this;
    }

    @Override
    public SbrProducer filterMember(final Member member) throws MemberInvalidException {
        if(member==null)
            throw new NullPointerException("member");
        if(member.getId()==null)
            throw new MemberInvalidException("member has no db ID", new NullPointerException("member.id"), member);

        filters.add(new Filter() {
            @Override
            public Predicate apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return cb.isMember(member, root.get(SBR_.members));
            }
        });

        return this;
    }

    @Override
    public SbrProducer sortName(final boolean ascending) {
        orders.add(new Ordering() {
            @Override
            public Order apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return createOrder(ascending, cb, root.get(SBR_.name));
            }
        });

        return this;
    }

    @Override
    public SbrProducer sortMembersCount(final boolean ascending) {
        this.orders.add(new Ordering() {
            @Override
            public Order apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return createOrder(ascending, cb, cb.size(root.get(SBR_.members)));
            }
        });

        return this;
    }

    @Override
    public SbrProducer sortPackagesCount(final boolean ascending) {
        this.orders.add(new Ordering() {
            @Override
            public Order apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query) {
                return createOrder(ascending, cb, cb.size(root.get(SBR_.packages)));
            }
        });

        return this;
    }

    @Override
    public SbrProducer recordsCount(int count) {
        if(count > 0)
            this.maxRecords = count;

        return this;
    }

    @Override
    public SbrProducer recordsStart(int start) {
        if(start >= 0)
            this.startOffset = start;

        return this;
    }

    @Override
    public List<SBR> getSbrs() {
        try {
            transaction.begin();
        } catch (NotSupportedException e) {
            log.severe(String.format("%s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
        } catch (SystemException e) {
            log.severe(String.format("%s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SBR> criteria = cb.createQuery(SBR.class);
        Root<SBR> sbr = criteria.from(SBR.class);

        criteria.select(sbr);

        //apply filters
        List<Predicate> predicates = new LinkedList<Predicate>();
        for(Filter filter : this.filters) {
            predicates.add(filter.apply(cb, sbr, criteria));
        }
        criteria.where(predicates.toArray(new Predicate[0]));

        //add orders
        // for now just add the first one
        if(!this.orders.isEmpty())
            criteria.orderBy(this.orders.get(0).apply(cb, sbr, criteria));

        TypedQuery<SBR> query = em.createQuery(criteria);

        if(this.maxRecords!=null)
            query.setMaxResults(this.maxRecords);

        if(this.startOffset!=null)
            query.setFirstResult(this.startOffset);

        List<SBR> result = fetchCollections(query.getResultList());

        try {
            transaction.commit();
        } catch (RollbackException e) {
            log.severe(String.format("%s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
        } catch (HeuristicMixedException e) {
            log.severe(String.format("%s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
        } catch (HeuristicRollbackException e) {
            log.severe(String.format("%s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
        } catch (SystemException e) {
            log.severe(String.format("%s\n%s", e.toString(), Arrays.toString(e.getStackTrace())));
        }

        //return the results
        return result;
    }

    @Override
    public long getCount() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<SBR> sbr = criteria.from(SBR.class);

        criteria.select(cb.count(sbr));

        //apply filters
        List<Predicate> predicates = new LinkedList<Predicate>();
        for(Filter filter : this.filters) {
            predicates.add(filter.apply(cb, sbr, criteria));
        }
        criteria.where(predicates.toArray(new Predicate[0]));

       // we can ignore order
        TypedQuery<Long> query = em.createQuery(criteria);


        //return the results
        return query.getResultList().get(0);
    }

    private Order createOrder(boolean ascending, CriteriaBuilder cb, Expression<?> sortedExpression) {
        if(ascending) {
            return cb.asc(sortedExpression);
        } else {
            return cb.desc(sortedExpression);
        }
    }

    private SBR fetchCollections(SBR sbr) {
        sbr.getCoaches().size();
        sbr.getMembers().size();
        sbr.getPackages().size();

        return sbr;
    }

    private List<SBR> fetchCollections(List<SBR> sbrs) {
        for(SBR sbr : sbrs) {
            fetchCollections(sbr);
        }

        return sbrs;
    }

    private static interface Filter {
        public Predicate apply(CriteriaBuilder cb, Root<SBR> root, CriteriaQuery query);
    }

    private static interface Ordering {
        public javax.persistence.criteria.Order apply(CriteriaBuilder cb, Root<SBR> root,  CriteriaQuery query);
    }
}
