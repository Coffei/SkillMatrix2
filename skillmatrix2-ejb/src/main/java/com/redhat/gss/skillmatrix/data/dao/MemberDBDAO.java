package com.redhat.gss.skillmatrix.data.dao;

import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.interfaces.MemberDAO;
import com.redhat.gss.skillmatrix.data.dao.producers.MemberProducerDB;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.MemberProducer;
import com.redhat.gss.skillmatrix.model.Knowledge;
import com.redhat.gss.skillmatrix.model.LanguageKnowledge;
import com.redhat.gss.skillmatrix.model.Member;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 * Database implementation of member dao.
 * User: jtrantin
 * Date: 8/6/13
 * Time: 2:10 PM
 * To change this template use File | Settings | File Templates.
 */
@Stateless
public class MemberDBDAO implements MemberDAO {

    @Inject
    private EntityManager em;

    @Inject
    private UserTransaction transaction;

    @Override
    public MemberProducer getProducerFactory() {
        return new MemberProducerDB(em, transaction);
    }

    @Override
    public void create(Member member) throws UnsupportedOperationException, MemberInvalidException {
        if(member==null)
            throw new NullPointerException("member");

        em.persist(member);
    }

    @Override
    public void update(Member member) throws UnsupportedOperationException, MemberInvalidException {
        if(member==null)
            throw new NullPointerException("member");
        if(member.getId()==null)  // member has no DB ID
            throw new MemberInvalidException("member has to have an ID in order to be updated", member);

        member = em.merge(member);

        //clean language knowledge
        if(member.getKnowledges().isEmpty()) {
            Query query = em.createNativeQuery("DELETE FROM languageknowledge WHERE member_id = :member");
            query.setParameter("member", member.getId());
            query.executeUpdate();
        } else {
            Query query = em.createNativeQuery("DELETE FROM languageknowledge WHERE member_id = :member AND ID NOT IN (:knows)");
            query.setParameter("member", member.getId());
            query.setParameter("knows", member.getKnowledges());
            query.executeUpdate();
        }
    }

    @Override
    public void delete(Member member) throws UnsupportedOperationException, MemberInvalidException {
        if(member==null)
            throw new NullPointerException("member");
        if(member.getId()==null)
            throw new MemberInvalidException("member has to have an ID in order to be deleted", member);

        if(!em.contains(member))
            member = em.merge(member);

        em.remove(member);
    }

    @Override
    public boolean canModify() {
        return true;
    }
}
