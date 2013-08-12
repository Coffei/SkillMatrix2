package com.redhat.gss.skillmatrix.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Entity class representing a Geographic location. It is represented by a code of the geo (e.g. EMEA, NA/SA) and the time zone offset.
 * User: jtrantin
 * Date: 7/16/13
 * Time: 10:05 AM
 * @see GeoEnum
 */
@Entity
public class Geo implements Serializable {
    public Geo() {}
    public Geo(GeoEnum geocode, int offset) {
        this.geocode = geocode;
        this.offset = offset;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private GeoEnum geocode;

    private int offset;

    public GeoEnum getGeocode() {
        return geocode;
    }

    public void setGeocode(GeoEnum geocode) {
        this.geocode = geocode;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    // compares ids, if ids are null, it compares geocode and offset
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Geo geo = (Geo) o;

        if(id==null) {
            if(geo.id!=null)
                return false;

            if(geocode==null? geo.geocode!=null : !geocode.equals(geo.geocode))
                return false;

            if(offset!=geo.offset)
                return false;

        } else if (!id.equals(geo.id))
            return false;


        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? (geocode==null? 0 : geocode.hashCode() * (offset==0? 1 : offset)) : id.hashCode());
        return result;
    }
}
