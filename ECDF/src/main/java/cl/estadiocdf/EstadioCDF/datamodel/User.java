package cl.estadiocdf.EstadioCDF.datamodel;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Franklin Cruz on 06-03-14.
 */
public class User implements DataModel, Serializable {

    private int userId;
    private String email;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String imageUrl;
    private String rut;
    private String address;
    private String country;
    private String state;
    private String city;
    private String phone;
    private String mobilePhone;
    private String favTeam;
    private String genre;
    private Date periodEndDate;
    private boolean active;
    private String status;
    private String cableProvider;
    private int tvChannelId;
    private boolean isCableProviderSelected;


    @DataMember(member = "id")
    public int getUserId() {
        return userId;
    }

    @DataMember(member = "id")
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @DataMember(member = "email")
    public String getEmail() {
        return email;
    }

    @DataMember(member = "email")
    public void setEmail(String email) {
        this.email = email;
    }

    @DataMember(member = "first_name")
    public String getFirstName() {
        return firstName;
    }

    @DataMember(member = "first_name")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @DataMember(member = "last_name")
    public String getLastName() {
        return lastName;
    }

    @DataMember(member = "last_name")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @DataMember(member = "birth_date")
    public Date getBirthDate() {
        return birthDate;
    }

    @DataMember(member = "birth_date")
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @DataMember(member = "picture_path")
    public String getImageUrl() {
        return imageUrl;
    }

    @DataMember(member = "picture_path")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @DataMember(member = "rut")
    public String getRut() {
        return rut;
    }

    @DataMember(member = "rut")
    public void setRut(String rut) {
        this.rut = rut;
    }

    @DataMember(member = "address")
    public String getAddress() {
        return address;
    }

    @DataMember(member = "address")
    public void setAddress(String address) {
        this.address = address;
    }

    @DataMember(member = "country")
    public String getCountry() {
        return country;
    }

    @DataMember(member = "country")
    public void setCountry(String country) {
        this.country = country;
    }

    @DataMember(member = "state")
    public String getState() {
        return state;
    }

    @DataMember(member = "state")
    public void setState(String state) {
        this.state = state;
    }

    @DataMember(member = "city")
    public String getCity() {
        return city;
    }

    @DataMember(member = "city")
    public void setCity(String city) {
        this.city = city;
    }

    @DataMember(member = "phone_fixed")
    public String getPhone() {
        return phone;
    }

    @DataMember(member = "phone_fixed")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @DataMember(member = "phone_mobile")
    public String getMobilePhone() {
        return mobilePhone;
    }

    @DataMember(member = "phone_mobile")
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    @DataMember(member = "fav_team")
    public String getFavTeam() {
        return favTeam;
    }

    @DataMember(member = "fav_team")
    public void setFavTeam(String favTeam) {
        this.favTeam = favTeam;
    }

    @DataMember(member = "genre")
    public String getGenre() {
        return genre;
    }

    @DataMember(member = "genre")
    public void setGenre(String genre) {
        this.genre = genre;
    }

    @DataMember(member = "period_end_date")
    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    @DataMember(member = "period_end_date")
    public void setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    @DataMember(member = "is_active")
    public boolean isActive() {
        return active;
    }

    @DataMember(member = "is_active")
    public void setActive(boolean active) {
        this.active = active;
    }

    @DataMember(member = "status")
    public String getStatus() {
        return status;
    }

    @DataMember(member = "status")
    public void setStatus(String status) {
        this.status = status;
    }

    @DataMember(member = "cable_provider")
    public String getCableProvider() {
        return cableProvider;
    }

    @DataMember(member = "cable_provider")
    public void setCableProvider(String cableProvider) {
        this.cableProvider = cableProvider;
    }

    @DataMember(member = "tv_channel_id")
    public int getTvChannelId() {
        return tvChannelId;
    }

    @DataMember(member = "tv_channel_id")
    public void setTvChannelId(int tvChannelId) {
        this.tvChannelId = tvChannelId;
    }

    @DataMember(member = "is_cable_provider_selected")
    public boolean isCableProviderSelected() {
        return isCableProviderSelected;
    }

    @DataMember(member = "is_cable_provider_selected")
    public void setCableProviderSelected(boolean isCableProviderSelected) {
        this.isCableProviderSelected = isCableProviderSelected;
    }

}
