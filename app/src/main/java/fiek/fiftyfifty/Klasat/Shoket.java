package fiek.fiftyfifty.Klasat;


public class Shoket
{
    private String perdoruesiID, emri, email;
    private double shpenzimet;

    public Shoket() {}

    public Shoket(String perdoruesiID, String emri, String email, double shpenzimet)
    {
        this.perdoruesiID = perdoruesiID;
        this.emri = emri;
        this.email = email;
        this.shpenzimet = shpenzimet;
    }

    public String getEmri()
    {
        return emri;
    }

    public String getEmail()
    {
        return email;
    }

    public double getShpenzimet()
    {
        return shpenzimet;
    }

    public String getPerdoruesiID()
    {
        return perdoruesiID;
    }
}
