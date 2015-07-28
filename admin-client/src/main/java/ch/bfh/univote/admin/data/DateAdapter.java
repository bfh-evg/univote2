package ch.bfh.univote.admin.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public DateAdapter() {
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String marshal(Date date) throws Exception {
	return dateFormat.format(date);
    }

    @Override
    public Date unmarshal(String dateString) throws Exception {
	return dateFormat.parse(dateString);
    }
}
