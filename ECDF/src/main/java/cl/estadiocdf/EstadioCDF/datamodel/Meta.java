package cl.estadiocdf.EstadioCDF.datamodel;

import java.io.Serializable;

/**
 * Created by Boris on 23-04-14.
 */
public class Meta implements DataModel, Serializable {

    private String id;
    private String url;
    private String label;

    @DataMember(member = "_id")
    public void setid(String id) {
        this.id = id;
    }

    @DataMember(member = "_id")
    public String getId() {
        return this.id;
    }

    @DataMember(member = "url")
    public void setUrl(String url) {
        this.url = url;
    }

    @DataMember(member = "url")
    public String getUrl() {
        return this.url;
    }

    @DataMember(member = "label")
    public void setLabel(String label) {
        this.label = label;
    }

    @DataMember(member = "label")
    public String getLabel() {
        return this.label;
    }
}
