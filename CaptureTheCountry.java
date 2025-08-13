import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CaptureTheCountry extends Server 
{
    public String country1;
    public String country2;
    public CountriesGraph graph;
    private final Player player1;
    private final Player player2;
    private final String goldenCountry;
    private boolean isPlayer1Turn = true;
    private boolean gameOver = false;

    private int isPlayer1Hint = 0;
    private int isPlayer2Hint = 0;
    private String recentCountry1 = "";        //last clicked country for player 1
    private String recentCountry2 = "";        //last clicked country for player 2

    private final Map<String, String> powerupMap = new HashMap<>();        //holds country name and corresponding powerup
    private final ArrayList<String> usedCountries = new ArrayList<>();
    private final Set<String> neighbors;
    private int hintCount;
    @SuppressWarnings("unused")
    private boolean oneColorPath = false;
    private String playerToSkip = "";


    public CaptureTheCountry() 
    {
        graph = new CountriesGraph();
        graph.loadData("CountryBorders.CSV");

        goldenCountry = graph.getRandomCountry();        //choose golden country randomly

        String startC1, startC2;
        List<String> pathC1, pathC2;

        do 
        {
            startC1 = graph.getRandomCountry();
            pathC1 = graph.bfsExclude(startC1, goldenCountry, null);
        } 
        while (startC1.equals(goldenCountry) || pathC1.size() < 2);

        do 
        {
            startC2 = graph.getRandomCountry();
            pathC2 = graph.bfsExclude(startC2, goldenCountry, startC1);
        } 
        while (startC2.equals(goldenCountry) || startC2.equals(startC1) || pathC2.size() < 2);

        //TODO ^^: there's a usedCountries arraylist - add to it here and remove while loops.

        country1 = startC1;
        country2 = startC2;

        player1 = new Player("player1", "yellow", startC1);
        player2 = new Player("player2", "red", startC2);

        System.out.println("Player1 starts at: " + player1.getStart() + " (" + player1.getColor() + ")");
        System.out.println("Player2 starts at: " + player2.getStart() + " (" + player2.getColor() + ")");
        System.out.println("Golden Country: " + goldenCountry);

        System.out.println("Path for Player 1: " + pathC1);
        System.out.println("Path for Player 2: " + pathC2);

        addCountryColor(player1.getStart(), player1.getColor());
        addCountryColor(player2.getStart(), player2.getColor());

        player1.getConqueredCountries().add(country1);
        player2.getConqueredCountries().add(country2);

        usedCountries.add(goldenCountry);
        usedCountries.add(country1);
        usedCountries.add(country2);
        String[] powerups = {"skipturn", "onecolorpath", "neighborhint", "removecountry", "countryhint"};

        for (int i = 0; i < 15; i++) 
        {
            String country = graph.getRandomCountry();

            while (usedCountries.contains(country))
                country = graph.getRandomCountry();

            usedCountries.add(country);
            String powerup = powerups[i % 5];
            powerupMap.put(country, powerup);

            /*if ("skipturn".equals(powerup))
                addCountryColor(country, "#FFA500"); // Orange

            else if ("onecolorpath".equals(powerup))
                addCountryColor(country, "#008000"); // Green

            else if ("neighborhint".equals(powerup))
                addCountryColor(country, "#00FFFF"); // Cyan

            else if ("removecountry".equals(powerup))
                addCountryColor(country, "#800080"); // Purple

            else if ("countryhint".equals(powerup))
                addCountryColor(country, "#FF00FF"); // Magenta*/
        }                  //set the random countries

        neighbors = new HashSet<>(CountriesGraph.getNeighbors(goldenCountry));            //get the neighbhors of golden country
        updateTurnDisplay();
    }

    @Override
    public void getColorPath() 
    {
        for (String s : player1.getPath()) 
            addCountryColor(s, player1.getColor());
        for (String s : player2.getPath()) 
            addCountryColor(s, player2.getColor());
    }

    @Override
    public void getInputCountries(String c1, String c2) 
    {
        this.country1 = c1;
        this.country2 = c2;
        updateTurnDisplay();
        getColorPath();
    }

    private void updateTurnDisplay() 
    {
        String playerTurn = isPlayer1Turn ? player1.getName() : player2.getName();
        //if isPlayer1Turn, playerTurn is player1 else it is player2
        addCountryColor("distance", playerTurn + "'s turn");
    }

    @Override
    public void handleClick(String country) 
    {
        if (gameOver) 
        {
            addCountryColor("distance", "Game Over! Rerun program to play again.");
            return;
        }
        if (country.equals(player1.getStart()) || country.equals(player2.getStart())) 
        {
            addCountryColor("distance", "You cannot conquer a starting country!");
            return;
        }

        Player currentPlayer, opponentPlayer;

        if(isPlayer1Turn)
        {
            currentPlayer = player1;
            opponentPlayer = player2;
        }
        else
        {
            currentPlayer = player2;
            opponentPlayer = player1;
        }

        if (playerToSkip.equals(currentPlayer.getName())) 
        {
            addCountryColor("distance", "Your turn is skipped!");
            playerToSkip = "";
            isPlayer1Turn = !isPlayer1Turn;
            return;
        }

        if(powerupMap.containsKey(country)) //country is clicked country.
        {
            List<String> bestPath = null;
            int minDistance = Integer.MAX_VALUE;
            for (String s : currentPlayer.getConqueredCountries()) 
            {
                List<String> path = graph.bfsExclude(s, country, opponentPlayer.getStart());
                if (!path.isEmpty() && path.size() < minDistance) 
                {
                    minDistance = path.size();
                    bestPath = path;
                }
            }


            if (bestPath == null || bestPath.isEmpty() || bestPath.size() < 2)         //TODO: check do i really need both isEmpty and size<2 (was it being weird or was i)
            {
                addCountryColor("distance", "No path to selected country! Try again " + currentPlayer.getName());
                return;
            }

            for (String s : bestPath) 
            {
                opponentPlayer.getConqueredCountries().remove(s);
                currentPlayer.getConqueredCountries().add(s);
                if (currentPlayer == player1) recentCountry1 = s;
                else recentCountry2 = s;
                addCountryColor(s, currentPlayer.getColor());
            }

            String powerup = powerupMap.remove(country);
            powerUpEnabled(powerup, currentPlayer, opponentPlayer);

            if (currentPlayer.getConqueredCountries().contains(goldenCountry)) 
            {
                gameOver = true;
                addCountryColor("distance", "🏆 " + currentPlayer.getName() + " has won by conquering the Golden Country! It was " + goldenCountry);
                return;
            }

            updateConqueredCountryColors(currentPlayer);
            isPlayer1Turn = !isPlayer1Turn;
            updateTurnDisplay();
            return;
        }

        boolean conquerable = currentPlayer.getConqueredCountries().stream()
                .anyMatch(s -> graph.getDistance(s, country) <= currentPlayer.getMaxDistance());

        if (!conquerable && currentPlayer.getFarPowerUp() > 0) 
        {
            currentPlayer.useFarPowerUp();
            addCountryColor("distance", "You used a far power up! You can now conquer this country.");
        } 
        else if (!conquerable) 
        {
            addCountryColor("distance", "You cannot conquer this country! Pick a country within your distance.");
            return;
        }

        List<String> bestPath = null;
        int minDistance = Integer.MAX_VALUE;
        for (String s : currentPlayer.getConqueredCountries()) 
        {
            List<String> path = graph.bfsExclude(s, country, opponentPlayer.getStart());
            if (!path.isEmpty() && path.size() < minDistance) 
            {
                minDistance = path.size();
                bestPath = path;
            }
        }

        System.out.println("bestPath: " + bestPath);

        if (bestPath == null || bestPath.isEmpty() || bestPath.size() < 2) 
        {
            addCountryColor("distance", "No path to selected country! Try again " + currentPlayer.getName());
            return;
        }

        for (String s : bestPath) 
        {
            opponentPlayer.getConqueredCountries().remove(s);
            currentPlayer.getConqueredCountries().add(s);

            if (currentPlayer == player1) 
                recentCountry1 = s;
            else recentCountry2 = s;

            addCountryColor(s, currentPlayer.getColor());
        }

        if (currentPlayer.getConqueredCountries().contains(goldenCountry)) 
        {
            gameOver = true;
            addCountryColor("distance", "🏆 " + currentPlayer.getName() + " has won by conquering the Golden Country! It was " + goldenCountry);
            return;
        }

        updateConqueredCountryColors(currentPlayer);

        if (currentPlayer == player1 && isPlayer1Hint == 0 && currentPlayer.getConqueredCountries().size() >= 15) 
        {
            isPlayer1Hint = 1;
            generateHint1();
        } 
        else if (currentPlayer == player2 && isPlayer2Hint == 0 && currentPlayer.getConqueredCountries().size() >= 15) 
        {
            isPlayer2Hint = 1;
            generateHint2();
        }

        isPlayer1Turn = !isPlayer1Turn;
        updateTurnDisplay();
    }

    private void updateConqueredCountryColors(Player currentPlayer) 
    {
        List<String> conquered = new ArrayList<>(currentPlayer.getConqueredCountries());
        int minDistance = Integer.MAX_VALUE;
        int maxDistance = Integer.MIN_VALUE;
        Map<String, Integer> distances = new HashMap<>();

        for (String c : conquered)
        {
            int distance = graph.getDistance(goldenCountry, c);
            distances.put(c, distance);
            minDistance = Math.min(minDistance, distance);
            maxDistance = Math.max(maxDistance, distance);
        }

        for (String c : conquered) 
        {
            int distance = distances.get(c);
            float normalized = (maxDistance == minDistance) ? 0f : (float) (distance - minDistance) / (maxDistance - minDistance);
            float saturation = 1.0f - normalized;
            Color color = currentPlayer == player1 ? Color.getHSBColor(0.15f, saturation, 1.0f) : Color.getHSBColor(0.0f, saturation, 1.0f);
            String hexColor = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            addCountryColor(c, hexColor);
        }
    }

    public void powerUpEnabled(String powerup, Player currentPlayer, Player opponentPlayer) 
    {
        switch (powerup) 
        {
            case "skipturn" -> {
                showHintDialog("Power Up: You skipped your opponent's turn!");
                playerToSkip = opponentPlayer.getName();
            }

            case "onecolorpath" -> {
                addCountryColor("distance", "Penalty: Both player's path will now be a single color!");
                oneColorPath = true;
            }
            case "neighborhint" -> {
                if (!neighbors.isEmpty()) 
                {
                    Iterator<String> it = neighbors.iterator();
                    String c = it.next();
                    it.remove();
                    showHintDialog("Hint: Neighboring country of golden is: " + c);
                } 
                else showHintDialog("No more neighboring hints available.");
            }
            case "countryhint" -> {
                hintCount++;
                if (hintCount % 2 == 1)
                    showHintDialog("Hint: Golden country starts with: " + goldenCountry.charAt(0));
                else showHintDialog( "Hint: Golden country ends with: " + goldenCountry.charAt(goldenCountry.length() - 1));
                getColorPath();
            }
            case "removecountry" -> {
                if (!opponentPlayer.getConqueredCountries().isEmpty()) 
                {
                    String removed = opponentPlayer.getConqueredCountries().remove(opponentPlayer.getConqueredCountries().size() - 1);
                    showHintDialog( "Powerup: Removed " + removed + " from opponent's path!");
                    addCountryColor(removed, "#808080");
                    getColorPath();
                }
            }
        }
    }

    public void generateHint1() 
    {
        showHintDialog("Hint for player 1: The golden country is " + graph.getDistance(goldenCountry, recentCountry1) + " countries away from " + recentCountry1);
    }

    public void generateHint2() 
    {
        showHintDialog("Hint for player 2: The golden country is " + graph.getDistance(goldenCountry, recentCountry2) + " countries away from " + recentCountry2);
    }

    private void showHintDialog(String message) 
    {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, message, "POWER UP", JOptionPane.INFORMATION_MESSAGE);
        frame.dispose();
    }

    public static void main(String[] args) 
    {
        Server server = new CaptureTheCountry();
        server.run();
        server.openURL();
    }
}
