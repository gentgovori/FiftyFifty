package fiek.fiftyfifty.Klasat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Utils
{
    private static FirebaseDatabase mDatabase;
    private static DatabaseReference mRef;

    public static FirebaseDatabase getDatabase()
    {
        if(mDatabase == null)
        {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true); // saves data in disk
        }
        return mDatabase;
    }

    public static Date getDate(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        Date date = calendar.getTime();
        return date;
    }

    public static String getData(long timestamp)
    {
        Date kohaRaportimit = getDate(timestamp);
        SimpleDateFormat koha = new SimpleDateFormat("HH:mm, dd-MM-yyyy", Locale.getDefault());
        return koha.format(kohaRaportimit);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
