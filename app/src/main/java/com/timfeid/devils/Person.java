package com.timfeid.devils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tim on 2/10/2018.
 * Person object from NHL api
 */

class Person implements Parcelable {
    private JSONObject data;
    private JSONObject person;
    private Stats currentStats;
    private Stats careerStats;
    Person(JSONObject data) {
        this.data = data;
        setPerson();
        setCurrentStats();
        setCareerStats();
    }

    private Person(Parcel in) {
        try {
            String data = in.readString();
            Helpers.d(data);
            this.data = new JSONObject(data);
            setPerson();
            setCurrentStats();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    private void setPerson() {
        try {
            person = data.getJSONObject("person");
        } catch (JSONException e) {
            try {
                person = data.getJSONArray("people").getJSONObject(0);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    Stats getCurrentStats() {
        return currentStats;
    }

    public Stats getCareerStats() {
        return careerStats;
    }

    private void setCurrentStats() {
        try {
            JSONArray stats = person.getJSONArray("stats");
            if (stats.length() > 0) {
                for (int i = 0; i < stats.length(); i++) {
                    JSONObject stat = stats.getJSONObject(i);
                    String displayName = stat.getJSONObject("type").getString("displayName");
                    if (displayName.equals("yearByYear") || displayName.equals("statsSingleSeason")) {
                        JSONArray splits = stat.getJSONArray("splits");
                        for (int j = 0; j < splits.length(); j++) {
                            JSONObject split = splits.getJSONObject(j);
                            if (split.getString("season").equals(Config.getValue("season"))) {
                                currentStats = new Stats(split.getJSONObject("stat"));
                                return;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCareerStats() {
        try {
            JSONArray stats = person.getJSONArray("stats");
            if (stats.length() > 0) {
                for (int i = 0; i < stats.length(); i++) {
                    JSONObject stat = stats.getJSONObject(i);
                    if (stat.getJSONObject("type").getString("displayName").equals("careerRegularSeason")) {
                        JSONObject splitStats = stat.getJSONArray("splits").getJSONObject(0).getJSONObject("stat");
                        careerStats = new Stats(splitStats);
                        return;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getGameLogStats() {
        try {
            JSONArray stats = person.getJSONArray("stats");
            if (stats.length() > 0) {
                for (int i = 0; i < stats.length(); i++) {
                    JSONObject stat = stats.getJSONObject(i);
                    if (stat.getJSONObject("type").getString("displayName").equals("gameLog")) {
                        return stat.getJSONArray("splits");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    String getShortName() throws JSONException {
        return person.getString("firstName").substring(0, 1) + ". " + person.getString("lastName");
    }

    public int getId() throws JSONException {
        return person.getInt("id");
    }

    public JSONObject getPerson() {
        return person;
    }

    public String getNumber() throws JSONException {
        return person.getString("primaryNumber");
    }

    public String getPositionCode() throws JSONException {
        return person.getJSONObject("primaryPosition").getString("code");
    }

    public String getImageUrl() throws JSONException {
        return "https://nhl.bamcontent.com/images/headshots/current/60x60/"+getId()+".png";
    }

    public String getActionPhotoUrl() throws JSONException {
        return "https://nhl.bamcontent.com/images/actionshots/"+getId()+".jpg";
    }

    public String getFullName() throws JSONException {
        return person.getString("firstName") + " " + person.getString("lastName");
    }

    public String getPositionAbbreviation() throws JSONException {
        return person.getJSONObject("primaryPosition").getString("abbreviation");
    }

    public String getDraft() throws JSONException {
        Helpers.d(person.getJSONArray("draft").toString());
        JSONObject draft = person.getJSONArray("draft").getJSONObject(0);
        return draft.getInt("year")
                + " "
                + draft.getJSONObject("team").getString("abbreviation")
                + ", "
                + Helpers.ordinal(Integer.parseInt(draft.getString("round")))
                + " rd, "
                + Helpers.ordinal(draft.getInt("pickInRound"))
                + " pk ("
                + draft.getInt("pickOverall")
                + " overall)";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data.toString());
    }

    public String getHeight() throws JSONException {
        return person.getString("height");
    }

    public String getWeight() throws JSONException {
        return person.getString("weight");
    }

    public String getAge() throws JSONException {
        return person.getString("currentAge");
    }

    public Date getBirthDate() throws JSONException {
        Date date = null;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = parser.parse(person.getString("birthDate"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public String getBirthday() throws JSONException {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");

        return formatter.format(getBirthDate());
    }

    public String getBirthPlace() throws JSONException {
        String state = "";
        try {
            state = ", "+person.getString("birthStateProvince");
        } catch (JSONException e) {
            // no state, no prob
        }
        return person.getString("birthCity") + state + ", " + person.getString("birthCountry");
    }

    public String getShootsCatches() throws JSONException {
        String shoots = "Right";
        if (person.getString("shootsCatches").equals("L")) {
            shoots = "Left";
        }
        return shoots;
    }

    class Stats {
        private JSONObject stats;
        public Stats(JSONObject stats) {
            this.stats = stats;
        }

        public int points() {
            try {
                return stats.getInt("points");
            } catch (JSONException e) {
                // Probably a goalie
            }

            return 0;
        }

        public int assists() {
            try {
                return stats.getInt("assists");
            } catch (JSONException e) {
                // Probably a goalie
            }

            return 0;
        }

        public int goals() {
            try {
                return stats.getInt("goals");
            } catch (JSONException e) {
                // Probably a goalie
            }

            return 0;
        }

        public int games() {
            try {
                return stats.getInt("games");
            } catch (JSONException e) {
                // Probably a goalie
            }

            return 0;
        }

        public int plusMinus() {
            try {
                return stats.getInt("plusMinus");
            } catch (JSONException e) {
                // Probably a goalie
            }

            return 0;
        }
    }
}
