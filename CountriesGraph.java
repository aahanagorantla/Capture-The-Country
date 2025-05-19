import java.io.*;
import java.util.*;

public class CountriesGraph
{
	private static Map<String, Set<String>> g;

	public CountriesGraph()
	{
		g = new HashMap<>();
	}

	public String getRandomCountry()
	{
		int rando = (int)(Math.random()*g.size());
		int i = 0;
		for (Map.Entry<String, Set<String>> entry : g.entrySet())
		{
			if(i == rando)
				return entry.getKey();
			i++;
		}
		return null;
	}

	public void add(String v1, String v2)
	{
		if(v2.length() > 0)
		{
			g.putIfAbsent(v1, new HashSet<>());
			g.putIfAbsent(v2, new HashSet<>());

			g.get(v1).add(v2);
			g.get(v2).add(v1);
		}

	}

	public void loadData(String file)
	{
		try
		{
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			br.readLine();
			int f, s, t;
			while((line = br.readLine()) != null)
			{
				f = line.indexOf(',');
				s = line.indexOf(',', f + 1);
				t = line.indexOf(',', s + 1);

				add(line.substring(3, s), line.substring(t+1));
			}
		}
		catch (IOException io)
		{
			System.err.println("File error: "+io);
		}
	}

	public static Set<String> getNeighbors(String node)
	{
		return g.get(node);
	}

	public void dft(String node)
	{
		HashSet<String> visited = new HashSet<>();

		dftRecur(node, visited);
	}

	private void dftRecur(String node, HashSet<String> visited)
	{
		if(visited.contains(node)) return;

		visited.add(node);

		for (String n :g.get(node))
			if(!visited.contains(n))
				dftRecur(n, visited);
	}

	public String dfs(String start, String end)
	{
		Stack<String> stack = new Stack<>();
		HashSet<String> visited = new HashSet<>();
		ArrayList<String> nodeOrder = new ArrayList<>();

		stack.add(start);
		while(!stack.isEmpty())		
		{
			String node = stack.pop();
			if(visited.contains(node)) continue;

			visited.add(node);
			nodeOrder.add(node);

			if(node.equals(end)) break;
			else
			{
				Set<String> n = getNeighbors(node);
				for (String s : n)
					stack.push(s);
			}
		}

		if(nodeOrder.size() > 1)
			return process(nodeOrder);

		return "No Connection";
	}

	public void print()
	{
		for (Map.Entry<String, Set<String>> entry : g.entrySet())
			System.out.println(entry.getKey()+" => "+entry.getValue());
	}

	private String process(ArrayList<String> arr)
	{
		for (int i = arr.size()-1; i>0; i--)
			if(!getNeighbors(arr.get(i)).contains(arr.get(i-1)))
				arr.remove(arr.get(i-1));

		String ans = "";

		for (String s : arr)
			ans += s+", ";

		return ans.substring(0, ans.length()-2);
	}

	public void bft(String start)
	{
		if(!g.containsKey(start))
		{
			System.out.println("start not found");
			return;
		}
		HashSet<String> visited = new HashSet<>();
		Queue<String> q = new LinkedList<>();

		q.add(start);

		while(!q.isEmpty())
		{
			System.out.println(visited);
			String node = q.poll();

			if(visited.contains(node)) continue;

			System.out.print(node+", ");
			visited.add(node);

			Set<String> n = getNeighbors(node);

			for(String s : n)
				if(!visited.contains(s))
					q.add(s);
		}
		System.out.println("\nBFT Complete!: visited "+visited.size()+" nodes.");
	}

	public static List<String> bfs(List<String> countries) 
	{
		List<String> finalPath = new ArrayList<>();

		if (countries == null || countries.size() < 2) 
		{
			finalPath.add("invalid input");
			return finalPath;
		}

		for (String country : countries) 
			if (!g.containsKey(country)) 
			{
				finalPath.add("country not found: " + country);
				return finalPath;
			}

		for (int i = 0; i < countries.size() - 1; i++) 
		{
			String start = countries.get(i);
			String end = countries.get(i + 1);
			List<String> segment = bfs(start, end);

			if (segment.size() == 1 && segment.get(0).equals("no path available")) 
				return segment; 

			if (i > 0) segment.remove(0);
				finalPath.addAll(segment);
		}

		return finalPath;
	}

	public static List<String> bfs(String start, String end)
	{
		List<String> finalPath = new ArrayList<>();
		if(!g.containsKey(start))
		{
			finalPath.add("start not found");
			return finalPath;
		}
		HashSet<String> visited = new HashSet<>();
		Queue<String> q = new LinkedList<>();
		Map<String, String> nodeBefore = new HashMap<>();
		q.add(start);
		while(!q.isEmpty())
		{
			String node = q.poll();
			if(node.equals(end))
				return bfsPath(nodeBefore, start, end);

			Set<String> n = getNeighbors(node);


			for(String s : n)
				if(!visited.contains(s))
				{
					q.add(s);
					visited.add(s);
					nodeBefore.put(s, node);
				}
		}

		finalPath.add("no path available");
		return finalPath;
	}

	private static List<String> bfsPath(Map<String, String> nodeBefore, String start, String end)
	{
		StringBuilder path = new StringBuilder("");

		String node = end;

		path.insert(0, node+", ");

		while(!node.equals(start))
		{
			node = nodeBefore.get(node);
			path.insert(0, node+", ");
		}

		String temp = path.substring(0, path.length()-2);

		String[] answer = temp.split(",");

		List<String> finalPath = new ArrayList<>();

		for (String s : answer)
			finalPath.add(s.trim());

		return finalPath;
	}

	public List<String> bfsExclude(String start, String end, String exclude)
	{
		List<String> finalPath = new ArrayList<>();
		if (!g.containsKey(start) || !g.containsKey(end)) 
		{
			finalPath.add("start or end country not found");
			return finalPath;
		}

		HashSet<String> visited = new HashSet<>();
		Queue<String> q = new LinkedList<>();
		Map<String, String> nodeBefore = new HashMap<>();

		if (start.equals(exclude) || end.equals(exclude)) 
		{
			finalPath.add("start or end country is excluded");
			return finalPath;
		}

		q.add(start);
		visited.add(start);

		while (!q.isEmpty())
		{
			String node = q.poll();

			if (node.equals(end)) 
				return bfsPath(nodeBefore, start, end); 

			Set<String> neighbors = getNeighbors(node);

			for (String neighbor : neighbors)
				if (!visited.contains(neighbor) && !neighbor.equals(exclude)) 
				{
					q.add(neighbor);
					visited.add(neighbor);
					nodeBefore.put(neighbor, node);
				}
		}

		finalPath.add("no path available");
		return finalPath;
	}

	public void remove(String country)
	{
		if (!g.containsKey(country))
			return;

		Set<String> connections = g.get(country);
		for (String c : connections)
			g.get(c).remove(country);
		
		g.remove(country);
	}

	public int getDistance (String country1, String country2)
	{
		return bfs(country1, country2).size()-1;
	}
}