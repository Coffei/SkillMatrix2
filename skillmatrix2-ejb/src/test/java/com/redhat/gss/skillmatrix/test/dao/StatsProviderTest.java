/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
    
package com.redhat.gss.skillmatrix.test.dao;

import com.redhat.gss.skillmatrix.data.dao.SbrDBDAO;
import com.redhat.gss.skillmatrix.data.dao.StatsProviderDB;
import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException;
import com.redhat.gss.skillmatrix.data.dao.interfaces.SbrDAO;
import com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider;
import com.redhat.gss.skillmatrix.data.dao.producers.SbrProducerDB;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.SbrProducer;
import com.redhat.gss.skillmatrix.model.Geo;
import com.redhat.gss.skillmatrix.model.GeoEnum;
import com.redhat.gss.skillmatrix.model.LanguageKnowledge;
import com.redhat.gss.skillmatrix.model.Member;
import com.redhat.gss.skillmatrix.model.PackageKnowledge;
import com.redhat.gss.skillmatrix.model.SBR;
import com.redhat.gss.skillmatrix.util.Resources;
import java.util.Arrays;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author jtrantin
 */
@RunWith(Arquillian.class)
public class StatsProviderTest {
    private boolean isSetUp = false;
    
    @Inject
    private UserTransaction transaction;
    
    @Inject
    private EntityManager em;
    
    @Inject
    private StatsProvider statsProvider;
    
    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "prodtest.war")
                .addPackage(SBR.class.getPackage()) //all model classes
                .addClasses(StatsProvider.class, StatsProviderDB.class)
                .addClasses(MemberInvalidException.class, SbrInvalidException.class, Resources.class)
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                //.addAsWebInfResource("test-ds.xml", "test-ds.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    
    
    @Test(expected = MemberInvalidException.class)
    public void testGetMemberKnowScoreInvalidMember() throws Exception {
        Member member = new Member();
        statsProvider.getMemberKnowScore(member);
    }
    
    @Test(expected = SbrInvalidException.class)
    public void testGetSbrKnowScoreInvalidSbr() throws Exception {
        SBR sbr = new SBR();
        statsProvider.getSbrKnowScore(sbr);        
    }
    
    @Test(expected= MemberInvalidException.class)
    public void testGetMemberKnowScoreInSbrInvalidMember() throws Exception {
        SBR sbr = em.find(SBR.class , wf_id); //get valid SBR
        Member member = new Member(); // get invalid member
        
        statsProvider.getMemberKnowScoreInSbr(member, sbr);
    }
    
    @Test(expected = SbrInvalidException.class)
    public void testGetMemberKnowScoreInSbrInvalidSBR() throws Exception {
        SBR sbr = new SBR(); // get invalid SBR
        Member member = em.find(Member.class, me_id); // get valid member

        statsProvider.getMemberKnowScoreInSbr(member, sbr);
    }
    
    // valid tests
    @Test
    public void testGetMemberKnowScore() throws Exception {
        Member me = em.find(Member.class, me_id);  // init members
        Member agi = em.find(Member.class, agi_id);
        Member ako = em.find(Member.class, ako_id);
        
        long score = statsProvider.getMemberKnowScore(me);
        assertEquals("invalid KnowScore value", 9l, score);
        
        score = statsProvider.getMemberKnowScore(agi);
        assertEquals("invalid KnowScore value", 10l, score);
        
        score = statsProvider.getMemberKnowScore(ako);
        assertEquals("invalid KnowScore value", 11l, score);
    }
    
    @Test
    public void testGetMemberKnowScoreInSbr() throws Exception {
        Member me = em.find(Member.class, me_id);  // init members and sbrs
        Member agi = em.find(Member.class, agi_id);
        Member ako = em.find(Member.class, ako_id);
        SBR wf = em.find(SBR.class, wf_id);
        SBR jbossas = em.find(SBR.class , jbossas_id);
        
        long score = statsProvider.getMemberKnowScoreInSbr(me, wf);
        assertEquals("invalid KnowScore value", 6l, score);
        
        score = statsProvider.getMemberKnowScoreInSbr(me, jbossas);
        assertEquals("invalid KnowScore value", 3l, score);
        
        score = statsProvider.getMemberKnowScoreInSbr(agi, wf);
        assertEquals("invalid KnowScore value", 5l, score);
        
        score = statsProvider.getMemberKnowScoreInSbr(ako, wf);
        assertEquals("invalid KnowScore value", 3l, score);
        
        score = statsProvider.getMemberKnowScoreInSbr(ako, jbossas);
        assertEquals("invalid KnowScore value", 8l, score); 
    }
    
    @Test
    public void testGetSbrKnowScore() throws Exception {
        SBR wf = em.find(SBR.class, wf_id);
        SBR jbossas = em.find(SBR.class, jbossas_id);
        
        long score = statsProvider.getSbrKnowScore(wf);
        assertEquals("invalid KnowScore value", 8, score);
        
        score = statsProvider.getSbrKnowScore(jbossas);
        assertEquals("invalid KnowScore value", 8, score);
    }
    
    // Setup stuff
    @Before
    public void setUp() throws Exception {
        if (!isSetUp) {
            clearData();
            prepareData();
            isSetUp = true;
        }
    }

    private void clearData() throws Exception {
        transaction.begin();
        em.joinTransaction();

        em.createNativeQuery("delete from MEMBER_SBR").executeUpdate();
        em.createQuery("delete from Knowledge").executeUpdate();
        /* em.createQuery("delete from PackageKnowledge");
         em.createQuery("delete from LanguageKnowledge");*/
        em.createQuery("delete from Coach").executeUpdate();
        em.createQuery("delete from Member").executeUpdate();
        em.createQuery("delete from Geo").executeUpdate();
        em.createQuery("delete from Package").executeUpdate();
        em.createQuery("delete from SBR").executeUpdate();

        transaction.commit();
    }

    private long me_id, agi_id, ako_id;
    private long wf_id, jbossas_id;
    private long rich_id, seam_id, ejb_id, log_id;

    private void prepareData() throws Exception {
        transaction.begin();
        em.joinTransaction();
        SBR wf = new SBR();
        wf.setName("Web Frameworks");
        em.persist(wf);
        wf_id = wf.getId();

        SBR jbossas = new SBR();
        jbossas.setName("JBoss AS");
        em.persist(jbossas);
        jbossas_id = jbossas.getId();

        com.redhat.gss.skillmatrix.model.Package richfaces = new com.redhat.gss.skillmatrix.model.Package();
        richfaces.setName("RichFaces");
        richfaces.setSbr(wf);
        em.persist(richfaces);
        rich_id = richfaces.getId();

        com.redhat.gss.skillmatrix.model.Package seam = new com.redhat.gss.skillmatrix.model.Package();
        seam.setName("Seam");
        seam.setSbr(wf);
        em.persist(seam);
        seam_id = seam.getId();

        com.redhat.gss.skillmatrix.model.Package ejb = new com.redhat.gss.skillmatrix.model.Package();
        ejb.setName("EJB");
        ejb.setSbr(jbossas);
        em.persist(ejb);
        ejb_id = ejb.getId();

        com.redhat.gss.skillmatrix.model.Package logging = new com.redhat.gss.skillmatrix.model.Package();
        logging.setName("Logging");
        logging.setSbr(jbossas);
        em.persist(logging);
        log_id = logging.getId();

        //members
        Member me = new Member();
        me.setNick("jtrantin");
        me.setEmail("jtrantin@redhat.com");
        me.setName("Jonas Trantina");
        me.setExtension("62918");
        me.setGeo(new Geo(GeoEnum.EMEA, 120));
        me.setRole("ITSE");
        me.setSbrs(Arrays.asList(wf));
        em.persist(me);
        me_id = me.getId();

        Member akovari = new Member();
        akovari.setNick("akovari");
        akovari.setEmail("akovari@redhat.com");
        akovari.setName("Adam Kovari");
        akovari.setExtension("62915");
        akovari.setGeo(new Geo(GeoEnum.NA, 120));
        akovari.setRole("STSE");
        akovari.setSbrs(Arrays.asList(jbossas));

        em.persist(akovari);
        ako_id = akovari.getId();

        Member agiertli = new Member();
        agiertli.setNick("agiertli");
        agiertli.setEmail("agiertli@redhat.com");
        agiertli.setName("Anton Giertli");
        agiertli.setExtension("62917");
        agiertli.setGeo(new Geo(GeoEnum.APAC, 120));
        agiertli.setRole("ITSE");
        agiertli.setSbrs(Arrays.asList(jbossas, wf));

        em.persist(agiertli);
        agi_id = agiertli.getId();
        
        //create languages
        LanguageKnowledge me_en = new LanguageKnowledge();
        me_en.setLanguage("en");
        me_en.setMember(me);

        em.persist(me_en);

        LanguageKnowledge ako_en = new LanguageKnowledge();
        ako_en.setLanguage("en");
        ako_en.setMember(akovari);

        em.persist(ako_en);

        LanguageKnowledge ako_ge = new LanguageKnowledge();
        ako_ge.setLanguage("ge");
        ako_ge.setMember(akovari);

        em.persist(ako_ge);

        LanguageKnowledge agi_en = new LanguageKnowledge();
        agi_en.setLanguage("en");
        agi_en.setMember(agiertli);

        em.persist(agi_en);

        LanguageKnowledge agi_sp = new LanguageKnowledge();
        agi_sp.setLanguage("sp");
        agi_sp.setMember(agiertli);

        em.persist(agi_sp);

        //package knowledge
        // me
        PackageKnowledge me_rich = new PackageKnowledge();
        me_rich.setMember(me);
        me_rich.setPackage(richfaces);
        me_rich.setLevel(2);
        em.persist(me_rich);

        PackageKnowledge me_seam = new PackageKnowledge();
        me_seam.setMember(me);
        me_seam.setPackage(seam);
        me_seam.setLevel(1);
        em.persist(me_seam);

        PackageKnowledge me_ejb = new PackageKnowledge();
        me_ejb.setMember(me);
        me_ejb.setPackage(ejb);
        me_ejb.setLevel(1);
        em.persist(me_ejb);

        PackageKnowledge me_log = new PackageKnowledge();
        me_log.setMember(me);
        me_log.setPackage(logging);
        me_log.setLevel(0);
        em.persist(me_log);

        // akovari
        PackageKnowledge ako_rich = new PackageKnowledge();
        ako_rich.setMember(akovari);
        ako_rich.setPackage(richfaces);
        ako_rich.setLevel(0);
        em.persist(ako_rich);

        PackageKnowledge ako_seam = new PackageKnowledge();
        ako_seam.setMember(akovari);
        ako_seam.setPackage(seam);
        ako_seam.setLevel(1);
        em.persist(ako_seam);

        PackageKnowledge ako_ejb = new PackageKnowledge();
        ako_ejb.setMember(akovari);
        ako_ejb.setPackage(ejb);
        ako_ejb.setLevel(2);
        em.persist(ako_ejb);

        PackageKnowledge ako_log = new PackageKnowledge();
        ako_log.setMember(akovari);
        ako_log.setPackage(logging);
        ako_log.setLevel(2);
        em.persist(ako_log);

        // agiertli
        PackageKnowledge agi_rich = new PackageKnowledge();
        agi_rich.setMember(agiertli);
        agi_rich.setPackage(richfaces);
        agi_rich.setLevel(0);
        em.persist(agi_rich);

        PackageKnowledge agi_seam = new PackageKnowledge();
        agi_seam.setMember(agiertli);
        agi_seam.setPackage(seam);
        agi_seam.setLevel(2);
        em.persist(agi_seam);

        PackageKnowledge agi_ejb = new PackageKnowledge();
        agi_ejb.setMember(agiertli);
        agi_ejb.setPackage(ejb);
        agi_ejb.setLevel(0);
        em.persist(agi_ejb);

        PackageKnowledge agi_log = new PackageKnowledge();
        agi_log.setMember(agiertli);
        agi_log.setPackage(logging);
        agi_log.setLevel(2);
        em.persist(agi_log);

        transaction.commit();
    }
    
    
}
