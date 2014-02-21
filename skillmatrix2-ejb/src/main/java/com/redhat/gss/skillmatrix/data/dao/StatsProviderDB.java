package com.redhat.gss.skillmatrix.data.dao;

import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException;
import com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider;
import com.redhat.gss.skillmatrix.model.*;
import com.redhat.gss.skillmatrix.model.Package;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.LazyInitializationException;

/**
 * Created by jtrantin on 2/6/14.
 */
@Stateless
public class StatsProviderDB implements StatsProvider {
    
    private final Logger log = Logger.getLogger(StatsProviderDB.class.getCanonicalName());

    @Inject
    private EntityManager em;

    @Override
    public long getSbrKnowScore(SBR sbr) throws SbrInvalidException {
        if (sbr == null) 
            throw new NullPointerException("sbr");
            
        
        sbr = checkAndReloadSbr(sbr);

        return sbr.getPackages().size() * 4;
    }

    //TODO: null checks, test!

    @Override
    public long getMemberKnowScore(Member member) throws MemberInvalidException {
        if(member==null)
            throw new NullPointerException("member");
        if(member.getId()==null)
            throw new MemberInvalidException("member has no DB id", member);
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Float> query = cb.createQuery(Float.class);
        Root<PackageKnowledge> pkgKnow = query.from(PackageKnowledge.class);


        // we use polynomial f(x)=1/2x^2 + 1/2x + 1, instead of KnowScore function g(x)=2^x, since f(x)=g(x) on the domain
        // of 0,1,2
        // it is neccesary to use floats
        query.select(cb.sum(cb.sum(
                cb.prod(
                        0.5f,
                        cb.prod(
                                pkgKnow.get(PackageKnowledge_.level).as(Float.class),
                                pkgKnow.get(PackageKnowledge_.level).as(Float.class))),
                cb.sum(
                        cb.prod(
                                0.5f,
                                pkgKnow.get(PackageKnowledge_.level).as(Float.class)),
                        1f))))
                .where(cb.equal(pkgKnow.get(PackageKnowledge_.member), member));
        
        List<Float> results = em.createQuery(query).getResultList();
        if(!results.isEmpty() && results.get(0)!=null) 
            return Math.round(results.get(0));

        return 0;
    }

    @Override
    public long getMemberKnowScoreInSbr(Member member, SBR sbr) throws MemberInvalidException, SbrInvalidException{
        if(member==null)
            throw new NullPointerException("member");
        if(sbr==null)
            throw new NullPointerException("sbr");
        if(member.getId()==null)
            throw new MemberInvalidException("member has no id", new NullPointerException("member.id"), member);
        
        sbr = checkAndReloadSbr(sbr);
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Float> query = cb.createQuery(Float.class);
        Root<PackageKnowledge> pkgKnow = query.from(PackageKnowledge.class);


        // we use polynomial f(x)=1/2x^2 + 1/2x + 1, instead of KnowScore function g(x)=2^x, since f(x)=g(x) on the domain
        // of 0,1,2
        query.select(cb.sum(cb.sum(
                cb.prod(
                        0.5f,
                        cb.prod(
                                pkgKnow.get(PackageKnowledge_.level).as(Float.class),
                                pkgKnow.get(PackageKnowledge_.level).as(Float.class))),
                cb.sum(
                        cb.prod(
                                0.5f,
                                pkgKnow.get(PackageKnowledge_.level).as(Float.class)),
                        1f))))
                .where(cb.equal(pkgKnow.get(PackageKnowledge_.member), member), 
                        pkgKnow.get(PackageKnowledge_.pkg).in(sbr.getPackages()));

        if(sbr.getPackages().isEmpty()) {
            return 0; // if SBR has no pkgs, then KnowScore must be zero
        }
        
        List<Float> results = em.createQuery(query).getResultList();
        if(!results.isEmpty()) {
         Float result = results.get(0);
         if(result!=null)
            return Math.round(result); //round in case the calculation is not precise
            
        }

        return 0;
    }
    
    private SBR checkAndReloadSbr(SBR sbr) throws SbrInvalidException {
        try {
            sbr.getPackages().size();
        } catch (LazyInitializationException e) { 
            // need to reload SBR
            if(sbr.getId()==null)
                throw new SbrInvalidException("missing packages and id", sbr);
            
            sbr = em.find(SBR.class, sbr.getId());
            if(sbr==null)
                throw new SbrInvalidException("invalid sbr.id", sbr);
        } catch (NullPointerException e) {
            // need to reload SBR
            if (sbr.getId() == null) {
                throw new SbrInvalidException("missing packages and id", sbr);
            }

            sbr = em.find(SBR.class, sbr.getId());
            if (sbr == null) {
                throw new SbrInvalidException("invalid sbr.id", sbr);
            }
        }
        
        return sbr;
    }
}
