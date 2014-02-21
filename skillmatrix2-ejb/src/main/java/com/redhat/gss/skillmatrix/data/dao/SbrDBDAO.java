package com.redhat.gss.skillmatrix.data.dao;

import com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException;
import com.redhat.gss.skillmatrix.data.dao.interfaces.SbrDAO;
import com.redhat.gss.skillmatrix.data.dao.producers.SbrProducerDB;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.SbrProducer;
import com.redhat.gss.skillmatrix.model.*;
import com.redhat.gss.skillmatrix.model.Package;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jtrantin
 * Date: 8/14/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
@Stateless
public class SbrDBDAO implements SbrDAO {
    
    @Inject
    private EntityManager em;
    
    @Inject
    private UserTransaction transaction;
    
    
    @Override
    public SbrProducer getProducerFactory() {
        return new SbrProducerDB(em, transaction);
    }
    
    @Override
    public void create(SBR sbr) throws SbrInvalidException {
        if(sbr==null)
            throw new NullPointerException("sbr");
        
        em.persist(sbr);
        
        //add new members
        if (sbr.getMembers() != null) {
            for(Member member : sbr.getMembers()) {
                if(!member.getSbrs().contains(sbr)) {
                    member.getSbrs().add(sbr);
                    em.merge(member);
                }
                
            }
        }
        
        //add new packages
        if(sbr.getPackages()!=null) {
            for(Package pkg : sbr.getPackages()) {
                if(!sbr.equals(pkg.getSbr())) {
                    pkg.setSbr(sbr);
                    em.merge(pkg);
                }
            }
        }
        
    }
    
    @Override
    public void update(SBR sbr) throws SbrInvalidException {
        if(sbr==null)
            throw new NullPointerException("sbr");
        if(sbr.getId() == null)
            throw new SbrInvalidException("sbr has no DB ID", new NullPointerException("sbr.id"), sbr);
        
        em.merge(sbr);
        
        //remove old members, TODO: watchout! depends on internal representation, but seems better than erasing one by one using JPA.
        if(sbr.getMembers().isEmpty()) { // cannot use empty list in PSQL
            Query deleteOldMembers = em.createNativeQuery("delete from member_sbr where sbrs_id = :sbrid");
            deleteOldMembers.setParameter("sbrid", sbr.getId());
            deleteOldMembers.executeUpdate();
        } else {
            Query deleteOldMembers = em.createNativeQuery("delete from member_sbr where sbrs_id = :sbrid AND members_id NOT IN (:membersid)");
            deleteOldMembers.setParameter("sbrid", sbr.getId());
            deleteOldMembers.setParameter("membersid", mapIDsMembers(sbr.getMembers()));
            deleteOldMembers.executeUpdate();
        }
        
        //delete old packages
        if(sbr.getPackages().isEmpty()) { // cannot use empty list in PSQL
            Query deleteOldPkgs = em.createNativeQuery("UPDATE package SET sbr_id=null WHERE sbr_id = :sbrid");
            deleteOldPkgs.setParameter("sbrid", sbr.getId());
            deleteOldPkgs.executeUpdate();
        } else {
            Query deleteOldPkgs = em.createNativeQuery("UPDATE package SET sbr_id=null WHERE sbr_id = :sbrid AND id not in (:pkgsid)");
            deleteOldPkgs.setParameter("sbrid", sbr.getId());
            deleteOldPkgs.setParameter("pkgsid", mapIDsPackages(sbr.getPackages()));
            deleteOldPkgs.executeUpdate();
        }
        
        //delete unassigned coaches
        if(sbr.getCoaches().isEmpty()) {
            Query query = em.createNativeQuery("DELETE FROM coach WHERE sbr_id = :sbr");
            query.setParameter("sbr", sbr.getId());
            query.executeUpdate();
        } else {
            Query query = em.createNativeQuery("DELETE FROM coach WHERE sbr_id = :sbr AND id NOT IN (:coaches)");
            query.setParameter("sbr", sbr.getId());
            query.setParameter("coaches", mapIDsCoaches(sbr.getCoaches()));
            query.executeUpdate();
        }
        
        
        //add new members
        if(sbr.getMembers()!=null) {
            for(Member member : sbr.getMembers()) {
                if(!member.getSbrs().contains(sbr)) {
                    member.getSbrs().add(sbr);
                    em.merge(member);
                }
            }
        }
        
        //add new packages
        if(sbr.getPackages()!=null) {
            for(Package pkg : sbr.getPackages()) {
                if(!sbr.equals(pkg.getSbr())) {
                    pkg.setSbr(sbr);
                    em.merge(pkg);
                }
            }
        }
        
    }
    
    private List<Long> mapIDsMembers(List<Member> members) {
        if(members==null)
            return Collections.emptyList();
        
        List<Long> ids = new ArrayList<Long>(members.size());
        for(Member member : members) {
            ids.add(member.getId());
        }
        return ids;
    }
    
    private List<Long> mapIDsPackages(List<Package> pkgs) {
        if(pkgs==null)
            return Collections.emptyList();
        
        List<Long> ids = new ArrayList<Long>(pkgs.size());
        for(Package pkg : pkgs) {
            ids.add(pkg.getId());
        }
        return ids;
    }
    
    @Override
    public void delete(SBR sbr) throws SbrInvalidException {
        if(sbr==null)
            throw new NullPointerException("sbr");
        if(sbr.getId() == null)
            throw new SbrInvalidException("sbr has no DB ID", new NullPointerException("sbr.id"), sbr);
        
        if(!em.contains(sbr))
            sbr = em.merge(sbr);
        
        // delete all members
        Query deleteMembers = em.createNativeQuery("DELETE FROM member_sbr WHERE sbrs_id=:sbrid");
        deleteMembers.setParameter("sbrid", sbr.getId());
        deleteMembers.executeUpdate();
        
        //delete all packages
        Query deletePkgs = em.createNativeQuery("UPDATE package SET sbr_id=null WHERE sbr_id = :sbrid");
        deletePkgs.setParameter("sbrid", sbr.getId());
        deletePkgs.executeUpdate();
        
        em.remove(sbr);
    }
    
    @Override
    public boolean canModify() {
        return true;
    }
    
    private List<Long> mapIDsCoaches(List<Coach> coaches) {
        if (coaches==null)
            return Collections.emptyList();
        
        List<Long> ids = new ArrayList<Long>(coaches.size());
        for (Coach coach : coaches) {
            if (coach != null && coach.getId() != null) {
                ids.add(coach.getId());
            }
        }
        
        return ids;
    }
}
