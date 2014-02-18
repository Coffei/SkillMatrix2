package com.redhat.gss.skillmatrix.controller.matrix;

import com.redhat.gss.skillmatrix.controller.util.MemberUtils;
import com.redhat.gss.skillmatrix.controllers.sorthelpers.MemberModelHelper;
import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException;
import com.redhat.gss.skillmatrix.data.dao.interfaces.MemberDAO;
import com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider;
import com.redhat.gss.skillmatrix.data.dao.producers.interfaces.MemberProducer;
import com.redhat.gss.skillmatrix.model.Member;
import com.redhat.gss.skillmatrix.model.SBR;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author jtrantin
 */
@ManagedBean
@ViewScoped
public class Matrix implements Serializable {
    private final Logger log = Logger.getLogger(Matrix.class.getName());
    
    @Inject
    private StatsProvider statsProvider;
    
    @Inject
    @Named
    private List<Member> allMembers;
    
    @Inject
    private MemberDAO memberDAO;
    
    @Inject
    private MemberUtils memberUtils;
    
    private MemberModelHelper modelHelper;
    
    private String page;
    
    private Map<SBR, Long> cacheSbrs = new HashMap<SBR, Long>();
    
    @PostConstruct
    private void init() {
        modelHelper = new MemberModelHelper(allMembers.size()) { // display all members on one page
            
            @Override
            protected MemberProducer getProducerFactory() {
                return memberDAO.getProducerFactory();
            }
        };
    }
    
    public String getCellText(Member member, SBR sbr) {
        final String mutedTemplate = "<p class=\"text-muted\" style=\"display:inline;\">%s%%</p>"; // when the information returned is not interesting, usualy 0%
        final String template = "<p style=\"display:inline;\">%s%%</p>"; // normal template
        final String errorTemplate = ""; // to be returned in case of error
        
        if(member==null || sbr==null)
            return errorTemplate;
        
        long result = 0;
        try {
            Long sbrScore;
            long memberScore = statsProvider.getMemberKnowScoreInSbr(member, sbr);
            if((sbrScore = cacheSbrs.get(sbr))==null)
                sbrScore = statsProvider.getSbrKnowScore(sbr);
            
            if (sbrScore > 0l) {
                result = (memberScore*100) / sbrScore;
            }
        } catch (SbrInvalidException ex) {
            log.log(Level.WARNING, "invalid sbr when creating cell text", ex);
            return errorTemplate;
        } catch (MemberInvalidException ex) {
            log.log(Level.WARNING, "invalid member when creating cell text", ex);
            return errorTemplate;
        }
        
        
        if(result > 0) {
            return String.format(template, result);
        } else {
            return String.format(mutedTemplate, result);
        }
    }
    
    public String getTooltipText(Member member, SBR sbr) {
        if(member==null || sbr==null) {
            return null;
        }
        
        Long result = 0L;
        try {
            Long memberSbrScore = statsProvider.getMemberKnowScoreInSbr(member, sbr);
            Long memberScore = statsProvider.getMemberKnowScore(member);
            if (memberScore > 0) {
                result = (memberSbrScore * 100) / memberScore;
            }
            
        } catch (SbrInvalidException sbrInvalidException) {// just log
            log.log(Level.WARNING, "Sbr invalid: {0}", sbrInvalidException.getInvalidSbr());
        } catch (MemberInvalidException memberInvalidException) {
            log.log(Level.WARNING, "Member invalid {0}", memberInvalidException.getInvalidMember());
        }
        
        String template = "<table class=\"table popovertable\">"
                + "<tr>"
                    + "<td>SBR</td>"
                    + "<td >%1$s</td>"
                + "</tr>"
                + "<tr>"
                    + "<td >%% of member knowledge</td>"
                    + "<td style=\"text-align:center;\">%2$s%%</td>"
                + "</tr>"
                + "</table>"
                + "<table class=\"table popovertable\">"
                + "<tr>"
                    + "<td style=\"border-top: 2px solid #aaaaaa\">Member</td>"
                    + "<td style=\"border-top: 2px solid #aaaaaa\">%3$s</td>"
                + "</tr>"
                + "<tr>"
                    + "<td >Nick</td>"
                    + "<td >%4$s</td>"
                + "</tr>"
                + "<tr>"
                    + "<td >Geo</td>"
                    + "<td >%5$s</td>"
                + "</tr>"
                + "<tr>"
                    + "<td >SBRs</td>"
                    + "<td >%6$s</td>"
                + "</tr>"
                + "</table>";
                
        
        String text = String.format(template, sbr.getName(), result, member.getName(), member.getNick(), 
                memberUtils.geo(member), memberUtils.sbrs(member));
        return text;
    }
    
    public boolean isMember(Member member, SBR sbr) {
        return member.getSbrs().contains(sbr);
    }
    
    public void togglePage() {
        this.page = "matrix_content.xhtml";
    }
    
    public String getPage() {
        return page;
    }
    
    public void setPage(String page) {
        this.page = page;
    }
    
    public MemberModelHelper getModelHelper() {
        return modelHelper;
    }
    
}
