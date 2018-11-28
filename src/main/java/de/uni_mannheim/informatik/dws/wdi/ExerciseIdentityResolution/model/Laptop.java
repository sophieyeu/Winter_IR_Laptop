package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model;

import de.uni_mannheim.informatik.dws.winter.model.Matchable;

public class Laptop implements Matchable {

    protected Integer size;
    protected String clusterid;
    private String productid;
    private String productname;
    private String provenance;

    public Laptop(String id, String provenance) {
        this.productid = id;
        this.provenance = provenance;
    }

    @Override
    public String toString() {
        return String.format("[Laptop %s: %s / %s / %s]", getIdentifier(), getProductname(),
                getClusterid(), getSize());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Laptop){
            return this.getIdentifier().equals(((Laptop) obj).getIdentifier());
        }else
            return false;
    }

    @Override
    public String getIdentifier() {
        return productid;
    }

    @Override
    public String getProvenance() {
        return provenance;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getClusterid() {
        return clusterid;
    }

    public void setClusterid(String clusterid) {
        this.clusterid = clusterid;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }
}
