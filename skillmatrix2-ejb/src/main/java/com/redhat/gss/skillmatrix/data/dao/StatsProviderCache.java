package com.redhat.gss.skillmatrix.data.dao;

import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException;
import com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider;
import com.redhat.gss.skillmatrix.model.Member;
import com.redhat.gss.skillmatrix.model.SBR;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

/**
 * Cache for {@link StatsProvider}
 * @author jtrantin
 */
@Decorator
public class StatsProviderCache implements StatsProvider {
    @Inject
    private Logger log;
    
    @Inject
    @Delegate
    private StatsProvider statsProvider;
    
    private Map<SBR, Long> sbrKnowScoreCache;
    private Map<Member, Long> memberKnowScoreCache;
    private Map<Membership, Long> memberSbrKnowScoreCache;
    
    
    @Override
    public long getSbrKnowScore(SBR sbr) throws SbrInvalidException {
        Long value;
        if((value = sbrKnowScoreCache.get(sbr))!=null) {
            log.log(Level.INFO, "Sbr cache used {0}", sbr);
            return value;
        }
        
        log.log(Level.INFO, "Sbr cache NOT used {0}", sbr);
        value = statsProvider.getSbrKnowScore(sbr);
        sbrKnowScoreCache.put(sbr, value);
        return value;
    }
    
    @Override
    public long getMemberKnowScore(Member member) throws MemberInvalidException {
        Long value;
        if ((value = memberKnowScoreCache.get(member)) != null) {
            log.log(Level.INFO, "Member cache used {0}", member);
            return value;
        }
        log.log(Level.INFO, "Member cache NOT used {0}", member);
        value =  statsProvider.getMemberKnowScore(member);
        memberKnowScoreCache.put(member, value);
        return value;
    }
    
    @Override
    public long getMemberKnowScoreInSbr(Member member, SBR sbr) throws MemberInvalidException, SbrInvalidException {
        Long value;
        if ((value = memberSbrKnowScoreCache.get(new Membership(member, sbr))) != null) {
            log.log(Level.INFO, "MemberSbr cache used {0}{1}", new String[]{sbr.toString(), member.toString()});
            return value;
        }
        
        log.log(Level.INFO, "MemberSbr cache NOT used {0}{1}", new String[]{sbr.toString(), member.toString()});
        value =  statsProvider.getMemberKnowScoreInSbr(member, sbr);
        memberSbrKnowScoreCache.put(new Membership(member, sbr), value);
        return value;
    }
    
    @PostConstruct
    private void init() {
        log.log(Level.INFO, "StatsProvider cache instantiated");
        sbrKnowScoreCache = new HashMap<>(); // create all maps- caches
        memberKnowScoreCache = new HashMap<>();
        memberSbrKnowScoreCache = new HashMap<>();
    }
    
    private final class Membership {
        private Member member;
        private SBR sbr;

        public Membership(Member member, SBR sbr) {
            this.member = member;
            this.sbr = sbr;
        }

        public Membership() {
        }
        
        public Member getMember() {
            return member;
        }

        public void setMember(Member member) {
            this.member = member;
        }

        public SBR getSbr() {
            return sbr;
        }

        public void setSbr(SBR sbr) {
            this.sbr = sbr;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.member);
            hash = 89 * hash + Objects.hashCode(this.sbr);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Membership other = (Membership) obj;
            if (!Objects.equals(this.member, other.member)) {
                return false;
            }
            if (!Objects.equals(this.sbr, other.sbr)) {
                return false;
            }
            return true;
        }
        
        
        
    }
}
