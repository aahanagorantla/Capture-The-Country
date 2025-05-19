import java.util.ArrayList;
import java.util.List;


public class Player
{
    private final String name;
    private String color;
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> conqueredCountries;
    private final String startCountry;
    private int maxDistance;
    private int farPowerUp = 2;

    public Player(String name, String color, String country)
    {
        this.name = name;
        this.color = color;
        this.conqueredCountries = new ArrayList<>();
        startCountry  = country;
        maxDistance = 2;
    }

    public boolean checkCountry(String country)
    {
        for(String s : conqueredCountries)
            if(CountriesGraph.bfs(s, country).size() <= maxDistance)
                return true;

        return false;
    }

    public void useFarPowerUp()
    {
        farPowerUp--;
    }

    public int getFarPowerUp()
    {
        return farPowerUp;
    }

    public void incrementMaxDistance()
    {
        maxDistance++;
    }

    public int getMaxDistance()
    {
        return maxDistance;
    }

    public String getStart()
    {
        return startCountry;
    }

    public String getName()
    {
        return name;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public List<String> getPath()
    {
        return CountriesGraph.bfs(conqueredCountries);
    }

    public List<String> getConqueredCountries()
    {
        return conqueredCountries;
    }
}