package com.authy.api;

import com.authy.AuthyException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Julian Camargo
 */
public class Users extends Resource {
    public static final String NEW_USER_PATH = "/protected/json/users/new";
    public static final String DELETE_USER_PATH = "/protected/json/users/delete/";
    public static final String SMS_PATH = "/protected/json/sms/";
    public static final String ONE_CODE_CALL_PATH = "/protected/json/call/";
    public static final String USER_STATUS_PATH = "/protected/json/users/%d/status";
    public static final String DEFAULT_COUNTRY_CODE = "1";
    /* Added for sending email tokens */
    public static final String EMAIL_PATH = "/protected/json/email/";

    public Users(String uri, String key) {
        super(uri, key, Resource.JSON_CONTENT_TYPE);
    }

    public Users(String uri, String key, boolean testFlag) {
        super(uri, key, testFlag, Resource.JSON_CONTENT_TYPE);
    }

    /**
     * Create a new user using his e-mail, phone and country code.
     *
     * @param email
     * @param phone
     * @param countryCode
     * @return a User instance
     */
    public com.authy.api.User createUser(String email, String phone, String countryCode) throws AuthyException {
        User user = new User(email, phone, countryCode);
        final Response response = this.post(NEW_USER_PATH, user);
        return userFromJson(response.getStatus(), response.getBody());
    }

    /**
     * Create a new user using his e-mail and phone. It uses USA country code by default.
     *
     * @param email
     * @param phone
     * @return a User instance
     */
    public com.authy.api.User createUser(String email, String phone) throws AuthyException {
        return createUser(email, phone, DEFAULT_COUNTRY_CODE);
    }

    /**
     * Send token via sms to a user.
     *
     * @param userId
     * @return Hash instance with API's response.
     */
    public Hash requestSms(int userId) throws AuthyException {
        return requestSms(userId, new HashMap<>(0));
    }

    /**
     * Send token via sms to a user with some options defined.
     *
     * @param userId
     * @param options
     * @return Hash instance with API's response.
     */
    public Hash requestSms(int userId, Map<String, String> options) throws AuthyException {
        MapToResponse opt = new MapToResponse(options);
        final Response response = this.get(SMS_PATH + Integer.toString(userId), opt);
        return instanceFromJson(response.getStatus(), response.getBody());
    }

    /**
     * Send token via call to a user.
     *
     * @param userId
     * @return Hash instance with API's response.
     */
    public Hash requestCall(int userId) throws AuthyException {
        return requestCall(userId, new HashMap<>(0));
    }

    /**
     * Send token via call to a user with some options defined.
     *
     * @param userId
     * @param options
     * @return Hash instance with API's response.
     */
    public Hash requestCall(int userId, Map<String, String> options) throws AuthyException {
        MapToResponse opt = new MapToResponse(options);
        final Response response = this.get(ONE_CODE_CALL_PATH + Integer.toString(userId), opt);
        return instanceFromJson(response.getStatus(), response.getBody());
    }
    
    /**
     * Send token via email to a user.
     * 
     * @param userId
     * @return
     * @throws AuthyException
     */
    public Hash requestEmail(int userId) throws AuthyException {
        return requestEmail(userId, new HashMap<>(0));
    }
    
    /**
     * Send token via email to a user which some options defined.
     * 
     * @param userId
     * @param options
     * @return
     * @throws AuthyException
     */
    public Hash requestEmail(int userId, Map<String, String> options) throws AuthyException {
        MapToResponse opt = new MapToResponse(options);
        final Response response = this.post(EMAIL_PATH + Integer.toString(userId), opt);
        return instanceFromJson(response.getStatus(), response.getBody());
    }

    /**
     * Delete a user.
     *
     * @param userId
     * @return Hash instance with API's response.
     */
    public Hash deleteUser(int userId) throws AuthyException {
        final Response response = this.post(DELETE_USER_PATH + Integer.toString(userId), null);
        return instanceFromJson(response.getStatus(), response.getBody());
    }

    /**
     * Get user status.
     *
     * @return object containing user status
     */
    public UserStatus requestStatus(int userId) throws AuthyException {
        final Response response = this.get(String.format(USER_STATUS_PATH, userId), null);
        UserStatus userStatus = userStatusFromJson(response);
        return userStatus;
    }

    private com.authy.api.User userFromJson(int status, String content) throws AuthyException {
        com.authy.api.User user = new com.authy.api.User(status, content);
        if (user.isOk()) {
            JSONObject userJson = new JSONObject(content);
            user.setId(userJson.getJSONObject("user").getInt("id"));
        } else {
            Error error = errorFromJson(content);
            user.setError(error);
        }
        return user;
    }

    private Hash instanceFromJson(int status, String content) throws AuthyException {
        Hash hash = new Hash(status, content);
        if (hash.isOk()) {
            try {
                JSONObject jsonResponse = new JSONObject(content);
                String message = jsonResponse.optString("message");
                hash.setMessage(message);

                boolean success = jsonResponse.optBoolean("success");
                hash.setSuccess(success);

                String token = jsonResponse.optString("token");
                hash.setToken(token);
            } catch (JSONException e) {
                throw new AuthyException("Invalid response from server", e);
            }
        } else {
            Error error = errorFromJson(content);
            hash.setError(error);
        }

        return hash;
    }

    private UserStatus userStatusFromJson(Response response) throws AuthyException {
        UserStatus userStatus = new UserStatus(response.getStatus(), response.getBody());
        if (userStatus.isOk()) {
            try {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String message = jsonResponse.optString("message");
                userStatus.setMessage(message);

                boolean success = jsonResponse.optBoolean("success");
                userStatus.setSuccess(success);

                JSONObject status = jsonResponse.getJSONObject("status");
                int userId = status.getInt("authy_id");
                userStatus.setUserId(userId);

                boolean confirmed = status.getBoolean("confirmed");
                userStatus.setConfirmed(confirmed);

                boolean registered = status.getBoolean("registered");
                userStatus.setRegistered(registered);

                int countryCode = status.getInt("country_code");
                userStatus.setCountryCode(countryCode);

                String phoneNumber = status.getString("phone_number");
                userStatus.setPhoneNumber(phoneNumber);

                JSONArray devicesArray = status.getJSONArray("devices");
                List<String> devices = userStatus.getDevices();
                for (int i = 0; i < devicesArray.length(); i++) {
                    devices.add(devicesArray.getString(i));
                }

            } catch (JSONException e) {
                throw new AuthyException("Invalid response from server", e);
            }
        } else {
            Error error = errorFromJson(response.getBody());
            userStatus.setError(error);
        }

        return userStatus;
    }

    static class MapToResponse implements Formattable {
        private Map<String, String> options;

        MapToResponse(Map<String, String> options) {
            this.options = options;
        }

        public String toXML() {
            return "";
        }

        public Map<String, String> toMap() {
            return options;
        }
    }

    @XmlRootElement(name = "user")
    static class User implements Formattable {
        String email, cellphone, countryCode;

        public User() {
        }

        public User(String email, String cellphone, String countryCode) {
            this.email = email;
            this.cellphone = cellphone;
            this.countryCode = countryCode;
        }

        @XmlElement(name = "email")
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @XmlElement(name = "cellphone")
        public String getCellphone() {
            return cellphone;
        }

        public void setCellphone(String cellphone) {
            this.cellphone = cellphone;
        }

        @XmlElement(name = "country_code")
        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String toXML() {
            StringWriter sw = new StringWriter();
            String xml = "";

            try {
                JAXBContext context = JAXBContext.newInstance(this.getClass());
                Marshaller marshaller = context.createMarshaller();

                marshaller.marshal(this, sw);

                xml = sw.toString();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return xml;
        }

        public Map<String, String> toMap() {

            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("cellphone", cellphone);
            map.put("country_code", countryCode);

            return map;
        }

        @Override
        public String toJSON() {
            JSONObject json = new JSONObject();
            json.put("user", toMap());
            return json.toString();
        }
    }
}
