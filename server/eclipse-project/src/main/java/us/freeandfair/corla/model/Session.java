package us.freeandfair.corla.model;


import java.util.Map.Entry;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import us.freeandfair.corla.auth.AuthenticationStage;
import us.freeandfair.corla.persistence.HashMapAssignmentConverter;
import us.freeandfair.corla.persistence.PersistentEntity;
import us.freeandfair.corla.query.AdministratorQueries;

@Entity
@Table(name = "session",
       uniqueConstraints = {
         @UniqueConstraint(columnNames = {"id"}) },
       indexes = { @Index(name = "idx_session_id",
                          columnList = "id", unique = true) })
public class Session implements PersistentEntity, Serializable {

  /**
   * Class-wide logger
   */
  public static final Logger LOGGER = LogManager.getLogger(Session.class);

  /**
   * The ID number.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;


  @Convert(converter = HashMapAssignmentConverter.class)
  private Map<String,String> vars = new HashMap<>();

  private Long version;

  public Long version() {
    return this.version;
  }

  /** the primary key **/
  public Long id() {
    return this.id;
  }

  public void setID (Long id) {
    this.id = id;
  }

  public void copyTo(spark.Session reqSess) {
    if (null != this.vars.get("username")) {
      reqSess.attribute("admin", AdministratorQueries.byUsername(this.vars.get("username")));
    }
    if (null != this.vars.get("county")) {
      reqSess.attribute("county", this.vars.get("county"));
    }
    if (null != this.vars.get("authentication_stage")) {
      reqSess.attribute("authentication_stage", AuthenticationStage.valueOf(this.vars.get("authentication_stage")));
    }
    if (null != this.vars.get("challenge")) {
      reqSess.attribute("challenge", this.vars.get("challenge"));
    }
  }

  public void copyFrom(spark.Session reqSess) {
    this.vars.clear();
    Administrator admin = reqSess.attribute("admin");
    if (null != admin) {
      this.vars.put("username", admin.username());
      this.vars.put("county", admin.county().name());
    }
    AuthenticationStage authStage = reqSess.attribute("authentication_stage");
    if (null != authStage) {
      this.vars.put("authentication_stage", authStage.toString());
    }
  }

  public String attribute(String key) {
    return this.vars.get(key);
  }

  public Set<String> attributes() {
    return this.vars.keySet();
  }

  public String toString() {
    return String.format("[Session id= %s vars=%s]", id(), this.vars);
  }

}
