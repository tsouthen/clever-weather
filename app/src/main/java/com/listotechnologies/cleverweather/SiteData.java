package com.listotechnologies.cleverweather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.TreeStrategy;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Root(strict=false)
public class SiteData {
    private static void addForecast(String CityCode, Date UTCIssueTime, String Name, String Summary, Integer IconCode, String LowTemp, String HighTemp, Context c) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CleverWeatherProvider.FORECAST_CITYCODE_COLUMN, CityCode);
        if (UTCIssueTime != null)
            contentValues.put(CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN, UTCIssueTime.getTime());
        if (Name != null)
            contentValues.put(CleverWeatherProvider.FORECAST_NAME_COLUMN, Name);
        if (Summary != null)
            contentValues.put(CleverWeatherProvider.FORECAST_SUMMARY_COLUMN, Summary);
        if (IconCode != null)
            contentValues.put(CleverWeatherProvider.FORECAST_ICONCODE_COLUMN, IconCode);
        if (LowTemp != null)
            contentValues.put(CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN, LowTemp);
        if (HighTemp != null)
            contentValues.put(CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN, HighTemp);

        ContentResolver cr = c.getContentResolver();
        cr.insert(CleverWeatherProvider.FORECAST_URI, contentValues);
    }

    public static void test(Context context, InputStream in) {
        try {
            TreeStrategy strategy = new TreeStrategy("clazz", "len"); //avoids errors with class= attribute in xml
            Persister persister = new Persister(strategy);
            SiteData siteData = persister.read(SiteData.class, in);

            CleverWeatherProviderClient.removeAllForecast(context);

            String cityCode = "s0000656";
            addForecast(cityCode, null, null, siteData.currentConditions.condition, siteData.currentConditions.iconCode, null, siteData.currentConditions.temperature, context);
            for (Forecast f : siteData.forecastGroup.forecasts) {
                String low = null;
                String high = null;
                for (Temperature t : f.temperatures.temperatures) {
                    if (t.tempClass.equals("low"))
                        low = Integer.toString(t.temperature);
                    else if (t.tempClass.equals("high"))
                        high = Integer.toString(t.temperature);
                }
                addForecast(cityCode, null, f.textForecastName, f.textSummary, f.abbreviatedForecast.iconCode, low, high, context);
            }
            in.close();
        } catch (Exception ex) {
        }
    }

    //@Element
    //@Path("location")
    //Name name;
    @Element
    ForecastGroup forecastGroup;
    @Element
    CurrentConditions currentConditions;
    
    @Root(strict=false)
    static class CurrentConditions {
        //@Element(required=false)
        //String station;
        @ElementList(inline=true, required=false)
        List<DateTimeData> dateTimes;
        @Element(required=false)
        String condition;
        @Element(required=false)
        int iconCode;
        @Element(required=false)
        String temperature;
        //@Element(required=false)
        //String dewpoint;
        //@Element(required=false)
        //String pressure;
        //@Element(required=false)
        //String visibility;
        //@Element(required=false)
        //String relativeHumidity;
    }
    
    @Root(strict=false)
    static class Name {
        @Attribute
        String code;
        @Attribute
        String lat;
        @Attribute
        String lon;
        @Text
        String label;
    }
    
    @Root(strict=false, name="dateTime")
    static class DateTimeData {
        @Attribute
        String name;
        @Element
        int year;
        @Element
        int month;
        @Element
        int day;
        @Element
        int hour;
        @Element
        int minute;
        @Element
        String timeStamp;
        @Attribute
        String zone;
        @Attribute
        int UTCOffset;
    }
    
    @Root(strict=false)
    static class ForecastGroup {
        @ElementList(inline=true, required=false)
        List<DateTimeData> dateTimes;
        
        @ElementList(inline=true)
        List<Forecast> forecasts;
    }
    
    @Root(strict=false)
    static class Forecast {
        @Attribute
        @Path("period")
        String textForecastName;

        @Element
        String textSummary;

        //@Element(required=false,name="textSummary")
        //@Path("cloudPrecip")
        //String cloudPrecip;

        @Element
        AbbreviatedForecast abbreviatedForecast;

        @Element
        Temperatures temperatures;

        //@Element(required=false)
        //Winds winds;

        //@Element(required=false)
        //Precipitation precipitation;

        //@Element(required=false)
        //String relativeHumidity;

        //@Element(required=false, name="textSummary")
        //@Path("comfort")
        //String comfort;
    }
    
    @Root(strict=false)
    static class AbbreviatedForecast {
        @Element
        int iconCode;

        @Element(required=false)
        int pop;

        @Element
        String textSummary;
    }
    
    @Root(strict=false)
    static class Temperatures {
        @Element(required=false)
        String textSummary;

        @ElementList(inline=true, required=false)
        List<Temperature> temperatures;
    }
    
    @Root(strict=false)
    static class Temperature {
        @Attribute
        String units;

        @Attribute(name="class")
        String tempClass;

        @Text
        int temperature;
    }
    
    /*
    @Root(strict=false)
    static class Winds {
        @Element(required=false)
        String textSummary;
        @ElementList(inline=true, required=false)
        List<Wind> winds;
    }

    @Root(strict=false)
    static class Wind {
        @Element(required=false)
        String speed;
        @Element(required=false)
        String gust;
        @Element(required=false)
        String direction;
        @Element(required=false)
        String bearing;
    }
    
    @Root(strict=false)
    static class Precipitation {
        @Element(required=false)
        String textSummary;
        @ElementList(inline=true,required=false)
        List<String> precipType;
    }
    */
}
