package com.redhat.gss.skillmatrix.data.dao.interfaces;

import com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException;
import com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException;
import com.redhat.gss.skillmatrix.model.Member;
import com.redhat.gss.skillmatrix.model.SBR;

/**
 * Provider of statistical data.
 * <strong>KnowScore</strong> is a unit used to measure knowledge. KnowScore of 1 corresponds to basic, beginners knowledge.
 * KnowScore of 2 corresponds to intermediates knowledge and 4 is an expert knowledge.
 *  Created by jtrantin on 2/6/14.
 */
public interface StatsProvider {

    /**
     * Computes <strong>KnowScore</strong> of an SBR. KnowScore of SBR is a representation of big the SBR is
     * (in terms of tags/packages). KnowScore of an SBR is basically number of tags/packages in the sbr multiplied by 4.
     * @param sbr sbr whose KnowScore should be computed
     * @return KnowScore value
     * @throws com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException
     * when sbr is invalid and cannot be used
     * @see com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider
     */
    long getSbrKnowScore(SBR sbr) throws SbrInvalidException;

    /**
     * Computes <strong>KnowScore</strong> of a Member. The value expresses how much tag/package knowledge (globally) the
     * member has. KnowScore is computed as explained in the class description- 1 for any basic knowledge, 2 for any
     * intermediate knowledge, 4 for any expert knowledge.
     * @param member member whose KnowScore should be computed
     * @return KnowScore value
     * @throws com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException
     * when member is invalid and cannot be used
     * @see com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider
     */
    long getMemberKnowScore(Member member) throws MemberInvalidException;


    /**
     * Computes <strong>KnowScore</strong> of a Member in a particular SBR. The value expresses how much knowledge of
     * the sbr the member has. KnowScore is computed the same as in {@link #getMemberKnowScore(com.redhat.gss.skillmatrix.model.Member)}
     * but only tags/packages in the particular SBR are considered.
     * @param member member whose KnowScore should be computed
     * @param sbr sbr that limits the tags/packages considered
     * @return KnowScore value
     * @throws com.redhat.gss.skillmatrix.data.dao.exceptions.MemberInvalidException
     * when member is invalid and cannot be used
     * @throws com.redhat.gss.skillmatrix.data.dao.exceptions.SbrInvalidException
     * when sbr is invalid and cannot be used
     * @see com.redhat.gss.skillmatrix.data.dao.interfaces.StatsProvider
     */
    long getMemberKnowScoreInSbr(Member member, SBR sbr) throws MemberInvalidException, SbrInvalidException;
}
