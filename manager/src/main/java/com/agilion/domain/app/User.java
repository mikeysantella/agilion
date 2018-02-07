package com.agilion.domain.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import jersey.repackaged.com.google.common.collect.Lists;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
@Entity
public class User implements UserDetails
{

    public interface UserRegistration {};

    /**
     * The ID of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * The display name of the user. This value should be displayed in any, and all, user interfaces
     */
    @NotBlank(message = "{validation.generic.notempty}", groups = {UserRegistration.class})
    @Size(max=32, message ="{validation.user.username.size}", groups = {UserRegistration.class} )
    private String username;

    /**
     * The user's email. Used for identification and
     */
    @NotBlank(message = "{validation.generic.notempty}", groups = {UserRegistration.class})
    @Email(message = "{validation.user.email.email}", groups = {UserRegistration.class} )
    private String email;

    /**
     * The user's password
     */
    @Size(min=8, max=64,  message ="{validation.user.password.size}", groups = {UserRegistration.class})
    private String password;

    /**
     * Field that contains the confirmed password (NOTE: this field is not persisted. It's just used for registration.
     */
    @Transient
    @JsonInclude
    private String confirmPassword;

    /**
     * The user's role
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{validation.generic.notempty}")
    private Role role;

    /**
     * The date that this user was created
     */
    @NotNull(message = "{validation.generic.notempty}")
    private Date dateCreated;

    /**
     * A list of IDs corresponding to ALL of the network build jobs that the user has submitted
     */
    @ElementCollection
    private List<String> submittedNetworkBuildJobIds;

    /**
     * A list of IDs corresponding to ALL of the analysis jobs that the user has submitted.
     */
    @ElementCollection
    private List<String> submittedAnalysisJobIds;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Lists.newArrayList(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<String> getSubmittedNetworkBuildJobIds() {
        return submittedNetworkBuildJobIds;
    }

    public void setSubmittedNetworkBuildJobIds(List<String> submittedNetworkBuildJobIds) {
        this.submittedNetworkBuildJobIds = submittedNetworkBuildJobIds;
    }

    public List<String> getSubmittedAnalysisJobIds() {
        return submittedAnalysisJobIds;
    }

    public void setSubmittedAnalysisJobIds(List<String> submittedAnalysisJobIds) {
        this.submittedAnalysisJobIds = submittedAnalysisJobIds;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
