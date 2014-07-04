package com.listotechnologies.cleverweather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ForecastParser {
    private static Element getFirstElementByTagName(Element element, String tagName) {
        if (element == null)
            return null;

        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList == null || nodeList.getLength() == 0)
            return null;

        return (Element) nodeList.item(0);
    }

    private static String getText(Element element) {
        if (element == null)
            return null;

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

    public static void parseXml(Context context, String provinceAbbr, String cityCode) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String uri = "http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/" + provinceAbbr + "/" + cityCode + "_e.xml";
            Document doc = builder.parse(uri);
            parseDoc(context, doc, cityCode);
        } catch (Exception ex) {
            Log.e("ForecastParser", "Exception", ex);
        }
    }

    public static void parseDoc(Context context, Document doc, String cityCode) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            fmt.setTimeZone(TimeZone.getTimeZone("GMT"));

            //get current conditions
            Element currentConds = getFirstElementByTagName(doc.getDocumentElement(), "currentConditions");
            String tempStr = getText(getFirstElementByTagName(currentConds, "temperature"));
            if (tempStr != null && !tempStr.isEmpty()) {
                String summary = getText(getFirstElementByTagName(currentConds, "condition"));
                Date currCondDate = getDate(fmt, currentConds);
                String iconCodeStr = getText(getFirstElementByTagName(currentConds, "iconCode"));
                Integer iconCode = null;
                if (iconCodeStr != null && !iconCodeStr.isEmpty())
                    iconCode = Integer.parseInt(iconCodeStr);
                //TODO: allow for string temperature instead of int?
                addForecast(cityCode, currCondDate, null, summary, iconCode, null, (int) Math.round(Double.parseDouble(tempStr)), context);
            }

            //get the forecasts
            Element forecastGroup = getFirstElementByTagName(doc.getDocumentElement(), "forecastGroup");
            Date forecastDate = getDate(fmt, forecastGroup);
            NodeList forecastList = forecastGroup.getElementsByTagName("forecast");
            if (forecastList != null) {
                for (int ii=0; ii < forecastList.getLength(); ii++) {
                    Element forecast = (Element) forecastList.item(ii);
                    String name = getFirstElementByTagName(forecast, "period").getAttribute("textForecastName");
                    String summary = getSummary(forecast);

                    Element abbrevForecast = getFirstElementByTagName(forecast, "abbreviatedForecast");
                    int iconCode = Integer.parseInt(getText(getFirstElementByTagName(abbrevForecast, "iconCode")));
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
                    //just save the forecast date once
                    if (ii == 0)
                        forecastDate = null;
                }
            }
        } catch (Exception ex) {
            Log.e("ForecastParser", "Exception", ex);
        }
    }

    private static String getSummary(Element forecast) {
        String cloudPrecip = getTextSummary(forecast, "cloudPrecip");
        String winds = getTextSummary(forecast, "winds");
        String uv = getTextSummary(forecast, "uv");
        StringBuilder builder = new StringBuilder();
        if (cloudPrecip != null && !cloudPrecip.isEmpty())
            builder.append(cloudPrecip);
        if (builder.length() > 0)
            builder.append(" ");
        if (winds != null && !winds.isEmpty())
            builder.append(winds);
        if (builder.length() > 0)
            builder.append(" ");
        if (uv != null && !uv.isEmpty())
            builder.append(uv);
        if (builder.length() > 0)
            return builder.toString();

        return getTextSummary(forecast);
    }

    private static String getTextSummary(Element element, String childTagName) {
        return getTextSummary(getFirstElementByTagName(element, childTagName));
    }

    private static String getTextSummary(Element element) {
        if (element != null) {
            Element textSummary = getFirstElementByTagName(element, "textSummary");
            if (textSummary != null)
                return getText(textSummary);
        }
        return null;
    }

    private static Date getDate(SimpleDateFormat fmt, Element forecasts) throws ParseException {
        Date utcDate = new Date();
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
