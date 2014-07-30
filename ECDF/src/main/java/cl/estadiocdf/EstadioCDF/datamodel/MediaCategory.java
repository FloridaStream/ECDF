package cl.estadiocdf.EstadioCDF.datamodel;

import java.io.Serializable;

/**
 * Created by Franklin Cruz on 11-03-14.
 */
public class MediaCategory implements DataModel, Serializable {

    private String categoryId;
    private String name;

    @DataMember(member = "_id")
    public String getCategoryId() {
        return categoryId;
    }

    @DataMember(member = "_id")
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @DataMember(member = "name")
    public String getName() {
        return name;
    }

    @DataMember(member = "name")
    public void setName(String name) {
        this.name = name;
    }
}
