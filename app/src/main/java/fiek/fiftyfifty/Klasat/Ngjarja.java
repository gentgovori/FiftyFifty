package fiek.fiftyfifty.Klasat;


public class Ngjarja
{
    private String titulli, lokacioni, valuta, regEmriMbiemri, perdoruesiID;
    private long dtRegjistrimit;
    private double shpenzimet;

    public Ngjarja() {}

    public Ngjarja(String titulli, String lokacioni, String valuta, double shpenzimet, String regEmriMbiemri, String perdoruesiID, long dtRegjistrimit)
    {
        this.titulli = titulli;
        this.lokacioni = lokacioni;
        this.valuta = valuta;
        this.shpenzimet = shpenzimet;
        this.regEmriMbiemri = regEmriMbiemri;
        this.perdoruesiID = perdoruesiID;
        this.dtRegjistrimit = dtRegjistrimit;
    }


    public String getTitulli()
    {
        return titulli;
    }

    public String getLokacioni()
    {
        return lokacioni;
    }

    public String getValuta()
    {
        return valuta;
    }

    public String getRegEmriMbiemri()
    {
        return regEmriMbiemri;
    }

    public String getPerdoruesiID()
    {
        return perdoruesiID;
    }

    public long getDtRegjistrimit()
    {
        return dtRegjistrimit;
    }

    public double getShpenzimet()
    {
        return shpenzimet;
    }
}
