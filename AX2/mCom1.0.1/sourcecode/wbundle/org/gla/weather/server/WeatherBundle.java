package org.gla.weather.server;

import StateAnnotations.mState;
import StateAnnotations.mStateType;
import mcom.bundle.ContractType;
import mcom.bundle.annotations.mController;
import mcom.bundle.annotations.mControllerInit;
import mcom.bundle.annotations.mEntity;
import mcom.bundle.annotations.mEntityContract;
import net.aksingh.java.api.owm.CurrentWeatherData;
import net.aksingh.java.api.owm.OpenWeatherMap;
import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;


@mController
@mEntity
@mState(stateType = mStateType.STATELESS)
public class WeatherBundle {
    @mEntityContract(description = "Any city.", contractType = ContractType.GET)
    @mControllerInit
    public static String getWeather(String city) {
        String result = "";

        // declaring object of "OpenWeatherMap" class
        OpenWeatherMap owm = new OpenWeatherMap("");

        try {
            CurrentWeatherData cwd = owm.currentWeatherByCityName(city);
            //printing city name from the retrieved data
            result = "| " + result + "City: " + cwd.getCityName() + " | ";

            // printing the max./min. temperature
            double c_temp = (cwd.getMainData_Object().getTemperature() - 32) / 1.8000;
            double c_temp_ = (double) Math.round(c_temp * 100) / 100;
            result = result + "Temperature: " + c_temp_ + "\'C | ";

            if (cwd.getRain_Object().hasRain3Hours()) {
                result = result + "Rain in 3hrs: " + cwd.getRain_Object().getRain3Hours() + " | ";
            } else {
                result = result + "Rain in 3hrs: - | ";
            }
            if (cwd.getWind_Object().hasWindSpeed()) {
                result = result + "Current wind speed: " + cwd.getWind_Object().getWindSpeed() + " | ";
            } else {
                result = result + "Current wind speed: - | ";

            }
            if (cwd.getClouds_Object().hasPercentageOfClouds()) {
                result = result + "Cloudy: " + cwd.getClouds_Object().getPercentageOfClouds() + "% | ";
            } else {
                result = result + "Cloudy: -% | ";
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {

    }
}
