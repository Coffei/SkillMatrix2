package com.redhat.gss.skillmatrix.data.api;

import com.redhat.gss.skillmatrix.model.*;
import com.redhat.gss.skillmatrix.model.api.MemberApi;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jtrantin
 * Date: 6/14/13
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
@Stateless
public class MemberApiBuilder {

    public MemberApi buildMember(Member member) {
        MemberApi api = new MemberApi();

        api.setEmail(member.getEmail());
        api.setExtension(member.getExtension());
        api.setGeo(buildGeo(member.getGeo()));
        api.setId(member.getId());
        api.setName(member.getName());
        api.setNick(member.getNick());
        api.setRole(member.getRole());

        List<MemberApi.SbrHelper> apiSbrs = new ArrayList<MemberApi.SbrHelper>(member.getSbrs().size());
        for(SBR sbr : member.getSbrs()) {
            MemberApi.SbrHelper sbrApi = new MemberApi.SbrHelper();
            sbrApi.setName(sbr.getName());
            sbrApi.setId(sbr.getId());

            apiSbrs.add(sbrApi);
        }

        api.setSbrs(apiSbrs);

        List<MemberApi.KnowledgeHelper> knowledges = new ArrayList<MemberApi.KnowledgeHelper>();
        List<String> languages = new ArrayList<String>();
        for(Knowledge know : member.getKnowledges()) {
            if(know instanceof PackageKnowledge) {
                PackageKnowledge pkgKnow = (PackageKnowledge)know;
                MemberApi.KnowledgeHelper helper = new MemberApi.KnowledgeHelper(pkgKnow.getLevel(), pkgKnow.getPackage().getId(), pkgKnow.getPackage().getName());
                knowledges.add(helper);

            } else if (know instanceof LanguageKnowledge) {
                String lang = ((LanguageKnowledge)know).getLanguage();
                languages.add(lang);

            } //ignore all other knowledges
        }
        api.setKnows(knowledges);
        api.setLangs(languages);

        return api;
    }

    String buildGeo(Geo geo) {
        StringBuilder builder = new StringBuilder();
        builder.append(geo.getGeocode().toString());

        builder.append(" (");
        int offset = geo.getOffset();
        if(offset < 0) {
            builder.append("-");
            offset *= -1;
        }
        Period offsetPeriod = new Duration(offset * 60 * 1000).toPeriod();

        builder.append(String.format("%02d:02d)", offsetPeriod.getHours(), offsetPeriod.getMinutes()));

        return builder.toString();
    }

    public List<MemberApi> buildMembers(List<Member> members) {
        List<MemberApi> apis = new ArrayList<MemberApi>(members.size());

        for(Member member : members) {
            apis.add(buildMember(member));
        }

        return apis;
    }

}
