package com.authy.api;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Moisés Vargas
 *
 */

public class PhoneInfoResponse extends Request {
  private int status = 503;
  private String response;
  private org.json.JSONObject jsonResponse;
  private String message = "Something went wrong!";
  private String provider = "";
  private String type = "";
  private boolean isPorted = false;

  public PhoneInfoResponse() {
  }

  public PhoneInfoResponse(int status, String response, String message) {
    this.status = status;
    this.response = response;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public String getProvider() {
    return provider;
  }

  public String getType() {
    return type;
  }

  public String getSuccess(){
    return Boolean.toString(this.isOk());
  }

  public String getIsPorted(){
    return Boolean.toString(this.isPorted);
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setResponse(String response) {
    this.response = response;
    this.jsonResponse = new org.json.JSONObject (response);
    this.parseResponseToOjbect(jsonResponse);
  }

  public boolean isOk() {
    return status == 200;
  }

  /**
   * Map a Token instance to its XML representation.
   * @return a String with the description of this object in XML.
   */
  public String toXML() {
    return "";
  }

  /**
   * Map a Token instance to its Java's Map representation.
   * @return a Java's Map with the description of this object.
   */
  public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<String, String>();

    map.put("message", this.getMessage());
    map.put("success", this.getSuccess());
    map.put("is_ported", this.getIsPorted());
    map.put("provider", this.getProvider());
    map.put("type", this.getType());


    return map;
  }

  public String toJSON(){
    org.json.JSONObject info = new org.json.JSONObject();

    info.put("message", this.getMessage());
    info.put("success", this.getSuccess());
    info.put("is_ported", this.getIsPorted());
    info.put("provider", this.getProvider());
    info.put("type", this.getType());

    return info.toString();
  }

  private void parseResponseToOjbect(org.json.JSONObject json){
    if( !json.isNull("message") )
      this.message = json.getString("message");

    if( !json.isNull("ported") )
      this.isPorted = json.getBoolean("ported");

    if( !json.isNull("provider") )
      this.provider = json.getString("provider");

    if( !json.isNull("type") )
      this.type = json.getString("type");
  }

  @Override
  public Serialization preferredSerialization() {
    return Serialization.JSON;
  }
}
