package fiek.fiftyfifty.Klasat;



public class Shpenzimi
{
    private String arsyeja, emriMbiemri, email;
    private double vlera;
    private long dtRegjistrimit;

    public Shpenzimi() {}

    public Shpenzimi(String arsyeja, double vlera, String emriMbiemri, String email, long dtRegjistrimit)
    {
        this.arsyeja = arsyeja;
        this.vlera = vlera;
        this.emriMbiemri = emriMbiemri;
        this.email = email;
        this.dtRegjistrimit = dtRegjistrimit;
    }

    public String getArsyeja()
    {
        return arsyeja;
    }

    public String getEmriMbiemri()
    {
        return emriMbiemri;
    }

    public String getEmail()
    {
        return email;
    }

    public double getVlera()
    {
        return vlera;
    }

    public long getDtRegjistrimit()
    {
        return dtRegjistrimit;
    }
}
