package com.listotechnologies.cleverweather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ForecastParser {
    public static void streamTest(final Context context) {
        AsyncTask<InputStream, Integer, Long> task = new AsyncTask<InputStream, Integer, Long>() {
            @Override
            protected Long doInBackground(InputStream... streams) {
                parseXml(context, streams[0]);
                //SiteData.test(context, streams[0]);
                try {
                    streams[0].close();
                } catch (Exception ex) {
                }
                return null;
            }
        };

        InputStream in = null;
        try {
            task.execute(context.getAssets().open("s0000656_e.xml"));
        } catch (Exception ex) {
        }
    }

    public static void uriTest(final Context context) {
        AsyncTask<String, Integer, Long> task = new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... strings) {
                parseXml(context, strings[0]);
                return null;
            }
        };
        task.execute("http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/BC/s0000656_e.xml");
    }

    private static Element getFirstElementByTagName(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList == null || nodeList.getLength() == 0)
            return null;

        return (Element) nodeList.item(0);
    }

    private static String getText(Element element) {
        StringBuilder buf = new StringBuilder();
        NodeList list = element.getChildNodes();
        boolean found = false;
        for (int ii = 0; ii < list.getLength(); ii++) {
            Node node = list.item(ii);
            if (node.getNodeType() == Node.TEXT_NODE) {
                buf.append(node.getNodeValue());
                found = true;
            }
        }
        return found ? buf.toString() : null;
    }

    public static void parseXml(Context context, InputStream in) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            String cityCode = "s0000656";
            parseDoc(context, doc, cityCode);
        } catch (Exception ex) {
        }
    }

    public static void parseXml(Context context, String uri) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(uri);
            String cityCode = "s0000656";
            parseDoc(context, doc, cityCode);
        } catch (Exception ex) {
        }
    }

    public static void parseDoc(Context context, Document doc, String cityCode) {
        try {

            CleverWeatherProviderClient.removeAllForecast(context);

            //get current conditions
            Element currentConds = getFirstElementByTagName(doc.getDocumentElement(), "currentConditions");
            Date currCondDate = getDate(currentConds);
            Element cond = getFirstElementByTagName(currentConds, "condition");
            String summary = getText(cond);
            int iconCode = Integer.parseInt(getText(getFirstElementByTagName(currentConds, "iconCode")));
            double temperature = Double.parseDouble(getText(getFirstElementByTagName(currentConds, "temperature")));
            addForecast(cityCode, currCondDate, null, summary, iconCode, null, (int) Math.round(temperature), context);

            //get the forecasts
            Element forecastGroup = getFirstElementByTagName(doc.getDocumentElement(), "forecastGroup");
            Date forecastDate = getDate(forecastGroup);
            NodeList forecastList = forecastGroup.getElementsByTagName("forecast");
            if (forecastList != null) {
                for (int ii=0; ii < forecastList.getLength(); ii++) {
                    Element forecast = (Element) forecastList.item(ii);
                    String name = getFirstElementByTagName(forecast, "period").getAttribute("textForecastName");
                    summary = null;
                    Element cloudPrecip = getFirstElementByTagName(forecast, "cloudPrecip");
                    if (cloudPrecip != null) {
                        summary = getText(getFirstElementByTagName(cloudPrecip, "textSummary"));
                    }
                    if (summary == null)
                        summary = getText(getFirstElementByTagName(forecast, "textSummary"));

                    Element abbrevForecast = getFirstElementByTagName(forecast, "abbreviatedForecast");
                    iconCode = Integer.parseInt(getText(getFirstElementByTagName(abbrevForecast, "iconCode")));
                    NodeList temps = getFirstElementByTagName(forecast, "temperatures").getElementsByTagName("temperature");
                    Integer low = null;
                    Integer high = null;
                    if (temps != null) {
                        for (int jj=0; jj < temps.getLength(); jj++) {
                            Element temp = (Element) temps.item(jj);
                            String tempClass = temp.getAttribute("class");
                            int tempValue = Integer.parseInt(getText(temp));
                            if ("low".equals(tempClass))
                                low = tempValue;
                            else if ("high".equals(tempClass))
                                high = tempValue;
                        }
                    }
                    addForecast(cityCode, forecastDate, name, summary, iconCode, low, high, context);
                }
            }
        } catch (Exception ex) {
            Log.e("ForecastParser", "Exception", ex);
        }
    }

    private static Date getDate(Element forecasts) throws ParseException {
        Date utcDate = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        NodeList dateTimes = forecasts.getElementsByTagName("dateTime");
        for (int ii=0; ii < dateTimes.getLength(); ii++) {
            Element dateTime = (Element) dateTimes.item(ii);
            String zone = dateTime.getAttribute("zone");
            if ("UTC".equals(zone)) {
                Element timeStamp = getFirstElementByTagName(dateTime, "timeStamp");
                String text = getText(timeStamp);
                if (text != null)
                    utcDate = fmt.parse(text);
                break;
            }
        }
        return utcDate;
    }

    private static void addForecast(String cityCode, Date utcDate, String name, String summary, Integer iconCode, Integer low, Integer high, Context c) {
        ContentValues value = new ContentValues();
        if (cityCode != null)
            value.put(CleverWeatherProvider.FORECAST_CITYCODE_COLUMN, cityCode);
        if (name != null)
            value.put(CleverWeatherProvider.FORECAST_NAME_COLUMN, name);
        if (summary != null)
            value.put(CleverWeatherProvider.FORECAST_SUMMARY_COLUMN, summary);
        if (iconCode != null)
            value.put(CleverWeatherProvider.FORECAST_ICONCODE_COLUMN, iconCode);
        if (utcDate != null)
            value.put(CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN, utcDate.getTime());
        if (low != null)
            value.put(CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN, low);
        if (high != null)
            value.put(CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN, high);
        ContentResolver cr = c.getContentResolver();
        cr.insert(CleverWeatherProvider.FORECAST_URI, value);
    }
}
