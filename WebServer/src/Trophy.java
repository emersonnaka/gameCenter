
public class Trophy {
	
	private int id;
    private String name;
    private int xp;
    private String title;
    private String description;
	
	public Trophy(int id, String name, int xp, String title, String description) {
		this.id = id;
        this.name = name;
        this.xp = xp;
        this.title = title;
        this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getXp() {
		return xp;
	}

	public void setXp(int xp) {
		this.xp = xp;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
